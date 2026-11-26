package com.example.handycam.data.model

/**
 * Saved user choices for native and web camera controls. Hardware-dependent
 * ranges are enforced against the active camera before applying them.
 * Torch is deliberately not persisted (never auto-enable the flashlight).
 */
data class CameraControlSettings(
    val manualExposureEnabled: Boolean = false,
    val iso: Int = 100,
    val shutterSpeedNs: Long = 16_666_667L,
    val autoExposure: Boolean = true,
    val autoFocus: Boolean = true,
    val focus: Int = 0,
    val whiteBalance: Int = 1,
    val whiteBalanceLocked: Boolean = false,
    val antiBanding: Int = 3,
    val zoom: Float = 0f,
    val exposure: Int = 0,
    val isoLocked: Boolean = false,
    val shutterLocked: Boolean = false,
    val zoomLocked: Boolean = false,
    val gridEnabled: Boolean = false,
)
