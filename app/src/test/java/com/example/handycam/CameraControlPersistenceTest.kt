package com.example.handycam

import com.example.handycam.data.model.CameraControlSettings
import com.example.handycam.service.StreamStateHolder
import com.example.handycam.service.observeCameraControls
import com.example.handycam.service.restoreCameraControls
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class CameraControlPersistenceTest {
    @Test
    fun restoresManualExposureAndRetainsValuesAcrossStopStart() {
        val state = StreamStateHolder()
        val saved = CameraControlSettings(manualExposureEnabled = true, iso = 320, shutterSpeedNs = 20_000_000L)
        restoreCameraControls(state, saved)
        state.setStreaming(true)
        state.setStreaming(false)
        state.setStreaming(true)
        assertTrue(state.manualExposureEnabled.value)
        assertEquals(320, state.iso.value)
        assertEquals(20_000_000L, state.shutterSpeedNs.value)
    }

    @Test
    fun savesChangesAndRestoresLatestValuesInFreshState() = runBlocking {
        val state = StreamStateHolder()
        val initial = CameraControlSettings(manualExposureEnabled = true, iso = 100, shutterSpeedNs = 10_000_000L)
        restoreCameraControls(state, initial)
        val writes = Channel<CameraControlSettings>(Channel.UNLIMITED)
        val job = launch { observeCameraControls(state, initial) { writes.send(it) } }
        try {
            state.setIso(400)
            state.setShutterSpeedNs(20_000_000L)
            val latest = withTimeout(5_000L) {
                var value = writes.receive()
                while (value.iso != 400 || value.shutterSpeedNs != 20_000_000L) value = writes.receive()
                value
            }
            assertTrue(latest.manualExposureEnabled)
            val reopened = StreamStateHolder()
            restoreCameraControls(reopened, latest)
            assertEquals(400, reopened.iso.value)
            assertEquals(20_000_000L, reopened.shutterSpeedNs.value)
            state.setManualExposureEnabled(false)
            val auto = withTimeout(5_000L) {
                var value = writes.receive()
                while (value.manualExposureEnabled) value = writes.receive()
                value
            }
            assertEquals(400, auto.iso)
            assertEquals(20_000_000L, auto.shutterSpeedNs)
            restoreCameraControls(reopened, auto)
            assertFalse(reopened.manualExposureEnabled.value)
            assertEquals(400, reopened.iso.value)
        } finally {
            job.cancelAndJoin()
            writes.close()
        }
    }
}
