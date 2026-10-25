package com.example.handycam.data.model.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(val success: Boolean, val message: String)

@Serializable
data class ServerStatus(
    val status: String,
    val streaming: Boolean,
    val port: Int,
    val uptime: Long
)

@Serializable
data class ServerInfo(val name: String, val version: String, val protocol: String)

@Serializable
data class CameraStatusResponse(
    val streaming: Boolean,
    val port: Int,
    val camera: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val jpegQuality: Int,
    val useAvc: Boolean,
    val zoom: Float,
    val zoomLocked: Boolean,
    val torchEnabled: Boolean,
    val autoFocus: Boolean,
    val focus: Int,
    val minFocusDistance: Float,
    val autoExposure: Boolean,
    val whiteBalance: Int,
    val whiteBalanceLocked: Boolean,
    val availableWhiteBalanceModes: List<Int>,
    val antiBanding: Int,
    val availableAntiBandingModes: List<Int>,
    val exposure: Int,
    val avcBitrate: Int,
    val manualExposure: Boolean,
    val manualExposureSupported: Boolean,
    val iso: Int,
    val isoMin: Int,
    val isoMax: Int,
    val shutterSpeedNs: Long,
    val shutterMinNs: Long,
    val shutterMaxNs: Long
)

@Serializable
data class CameraListItem(
    val id: String,
    val label: String,
    val facing: String,
    val focalDesc: String
)
