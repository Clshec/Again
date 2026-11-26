package com.example.handycam.service

import com.example.handycam.data.model.CameraControlSettings
import com.example.handycam.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Save native and web camera choices for the entire application process,
 * not just for the Activity. A committed slider/text change is saved immediately. */
@Singleton
class CameraControlPersistence @Inject constructor(
    private val streamStateHolder: StreamStateHolder,
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun start() {
        scope.launch {
            val saved = settingsRepository.cameraControls.first()
            restoreCameraControls(streamStateHolder, saved)
            _ready.value = true
            observeCameraControls(streamStateHolder, saved, settingsRepository::updateCameraControls)
        }
    }
}

internal fun restoreCameraControls(streamStateHolder: StreamStateHolder, saved: CameraControlSettings) {
    streamStateHolder.setIso(saved.iso)
    streamStateHolder.setShutterSpeedNs(saved.shutterSpeedNs)
    streamStateHolder.setAutoExposure(saved.autoExposure)
    streamStateHolder.setAutoFocus(saved.autoFocus)
    streamStateHolder.setFocus(saved.focus)
    streamStateHolder.setWhiteBalance(saved.whiteBalance)
    streamStateHolder.setWhiteBalanceLocked(saved.whiteBalanceLocked && saved.whiteBalance == 1)
    streamStateHolder.setAntiBanding(saved.antiBanding)
    streamStateHolder.setZoom(saved.zoom)
    streamStateHolder.setExposure(saved.exposure)
    streamStateHolder.setIsoLocked(saved.isoLocked)
    streamStateHolder.setShutterLocked(saved.shutterLocked)
    streamStateHolder.setZoomLocked(saved.zoomLocked)
    streamStateHolder.setGridEnabled(saved.gridEnabled)
    streamStateHolder.setManualExposureEnabled(saved.manualExposureEnabled)
}

internal suspend fun observeCameraControls(
    streamStateHolder: StreamStateHolder,
    initial: CameraControlSettings,
    save: suspend (CameraControlSettings) -> Unit,
) {
    var lastSaved = initial
    val flows: List<Flow<Any>> = listOf(
        streamStateHolder.manualExposureEnabled,
        streamStateHolder.iso,
        streamStateHolder.shutterSpeedNs,
        streamStateHolder.autoExposure,
        streamStateHolder.autoFocus,
        streamStateHolder.focus,
        streamStateHolder.whiteBalance,
        streamStateHolder.whiteBalanceLocked,
        streamStateHolder.antiBanding,
        streamStateHolder.zoom,
        streamStateHolder.exposure,
        streamStateHolder.isoLocked,
        streamStateHolder.shutterLocked,
        streamStateHolder.zoomLocked,
        streamStateHolder.gridEnabled
    )
    combine(flows) { states: Array<Any> ->
        CameraControlSettings(
            manualExposureEnabled = states[0] as Boolean,
            iso = states[1] as Int,
            shutterSpeedNs = states[2] as Long,
            autoExposure = states[3] as Boolean,
            autoFocus = states[4] as Boolean,
            focus = states[5] as Int,
            whiteBalance = states[6] as Int,
            whiteBalanceLocked = states[7] as Boolean,
            antiBanding = states[8] as Int,
            zoom = states[9] as Float,
            exposure = states[10] as Int,
            isoLocked = states[11] as Boolean,
            shutterLocked = states[12] as Boolean,
            zoomLocked = states[13] as Boolean,
            gridEnabled = states[14] as Boolean
        )
    }.distinctUntilChanged().collect { current ->
        if (current != lastSaved) {
            save(current)
            lastSaved = current
        }
    }
}
