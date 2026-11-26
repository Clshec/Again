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
    @Test
    fun restoresAllCommittedControlsAndDoesNotTurnOnTorch() {
        val state = StreamStateHolder()
        val saved = CameraControlSettings(
            manualExposureEnabled = true, iso = 640, shutterSpeedNs = 10_000_000L,
            autoExposure = false, autoFocus = false, focus = 35,
            whiteBalance = 5, antiBanding = 1, zoom = 0.4f,
            exposure = -2, isoLocked = true, shutterLocked = true,
            zoomLocked = true, gridEnabled = true
        )
        restoreCameraControls(state, saved)
        assertEquals(-2, state.exposure.value)
        assertTrue(state.isoLocked.value)
        assertTrue(state.shutterLocked.value)
        assertTrue(state.zoomLocked.value)
        assertTrue(state.gridEnabled.value)
        assertEquals(35, state.focus.value)
        assertEquals(5, state.whiteBalance.value)
        assertEquals(1, state.antiBanding.value)
        assertEquals(0.4f, state.zoom.value, 0.001f)
        assertFalse(state.torchEnabled.value)
    }

    @Test
    fun observesAdditionalSettingsWithoutActivity() = runBlocking {
        val state = StreamStateHolder()
        val initial = CameraControlSettings()
        val writes = Channel<CameraControlSettings>(Channel.UNLIMITED)
        val job = launch { observeCameraControls(state, initial) { writes.send(it) } }
        try {
            state.setExposure(2)
            state.setIsoLocked(true)
            state.setShutterLocked(true)
            state.setZoomLocked(true)
            state.setGridEnabled(true)
            val latest = withTimeout(5_000L) {
                var value = writes.receive()
                while (value.exposure != 2 || !value.isoLocked || !value.shutterLocked ||
                    !value.zoomLocked || !value.gridEnabled) value = writes.receive()
                value
            }
            val reopened = StreamStateHolder()
            restoreCameraControls(reopened, latest)
            assertEquals(2, reopened.exposure.value)
            assertTrue(reopened.isoLocked.value)
            assertTrue(reopened.shutterLocked.value)
            assertTrue(reopened.zoomLocked.value)
            assertTrue(reopened.gridEnabled.value)
        } finally {
            job.cancelAndJoin()
            writes.close()
        }
    }

    @Test
    fun stableHostDoesNotDependOnNetworkAddress() {
        val first = stableCameraHostForId("example-phone-one")
        assertEquals(first, stableCameraHostForId("example-phone-one"))
        assertNotEquals(first, stableCameraHostForId("example-phone-two"))
        assertTrue(first.matches(Regex("handycam-[0-9a-f]{8}[.]local")))
    }

}
