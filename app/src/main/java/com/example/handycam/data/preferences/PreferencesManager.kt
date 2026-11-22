package com.example.handycam.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.handycam.data.model.AppSettings
import com.example.handycam.data.model.CameraControlSettings
import com.example.handycam.data.model.StreamConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "handycam_prefs")

/**
 * Centralized preferences manager using DataStore.
 * Provides type-safe, reactive access to all app settings.
 * This replaces scattered SharedPreferences calls throughout the app.
 */
class PreferencesManager(private val context: Context) {

    private val dataStore = context.dataStore

    // Preference keys
    private object PreferenceKeys {
        val HOST = stringPreferencesKey("host")
        val STREAMING_PORT = intPreferencesKey("streaming_port")
        val HTTP_PORT = intPreferencesKey("http_port")
        val WIDTH = intPreferencesKey("width")
        val HEIGHT = intPreferencesKey("height")
        val CAMERA = stringPreferencesKey("camera")
        val JPEG_QUALITY = intPreferencesKey("jpeg_quality")
        val FPS = intPreferencesKey("fps")
        val USE_AVC = booleanPreferencesKey("use_avc")
        val AVC_BITRATE = intPreferencesKey("avc_bitrate")
        val USE_SCREEN_CAPTURE = booleanPreferencesKey("use_screen_capture")
        val IS_STREAMING = booleanPreferencesKey("is_streaming")
        val HTTPS_RUNNING = booleanPreferencesKey("https_running")
        val MANUAL_EXPOSURE = booleanPreferencesKey("camera_manual_exposure")
        val MANUAL_ISO = intPreferencesKey("camera_manual_iso")
        val MANUAL_SHUTTER_NS = longPreferencesKey("camera_manual_shutter_ns")
        val CAMERA_AUTO_EXPOSURE = booleanPreferencesKey("camera_auto_exposure")
        val CAMERA_AUTO_FOCUS = booleanPreferencesKey("camera_auto_focus")
        val CAMERA_FOCUS = intPreferencesKey("camera_focus")
        val CAMERA_WHITE_BALANCE = intPreferencesKey("camera_white_balance")
        val CAMERA_AWB_LOCK = booleanPreferencesKey("camera_awb_lock")
        val CAMERA_ANTI_BANDING = intPreferencesKey("camera_anti_banding")
        val CAMERA_ZOOM = floatPreferencesKey("camera_zoom")
    }

    /**
     * Flow of app settings that emits whenever any setting changes.
     */
    val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                host = preferences[PreferenceKeys.HOST] ?: "0.0.0.0",
                streamingPort = preferences[PreferenceKeys.STREAMING_PORT] ?: 4747,
                camera = preferences[PreferenceKeys.CAMERA] ?: "back",
                isStreaming = preferences[PreferenceKeys.IS_STREAMING] ?: false,
                useScreenCapture = preferences[PreferenceKeys.USE_SCREEN_CAPTURE] ?: false
            )
        }

    /**
     * Flow of stream configuration that emits whenever any stream setting changes.
     */
    val streamConfigFlow: Flow<StreamConfig> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            StreamConfig(
                width = preferences[PreferenceKeys.WIDTH] ?: 1920,
                height = preferences[PreferenceKeys.HEIGHT] ?: 1080,
                jpegQuality = preferences[PreferenceKeys.JPEG_QUALITY] ?: 85,
                fps = preferences[PreferenceKeys.FPS] ?: 30,
                useAvc = preferences[PreferenceKeys.USE_AVC] ?: false,
                avcBitrate = preferences[PreferenceKeys.AVC_BITRATE]
            )
        }

    val cameraControlsFlow: Flow<CameraControlSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            CameraControlSettings(
                manualExposureEnabled = preferences[PreferenceKeys.MANUAL_EXPOSURE] ?: false,
                iso = preferences[PreferenceKeys.MANUAL_ISO] ?: 100,
                shutterSpeedNs = preferences[PreferenceKeys.MANUAL_SHUTTER_NS] ?: 16_666_667L,
                autoExposure = preferences[PreferenceKeys.CAMERA_AUTO_EXPOSURE] ?: true,
                autoFocus = preferences[PreferenceKeys.CAMERA_AUTO_FOCUS] ?: true,
                focus = preferences[PreferenceKeys.CAMERA_FOCUS] ?: 0,
                whiteBalance = preferences[PreferenceKeys.CAMERA_WHITE_BALANCE] ?: 1,
                whiteBalanceLocked = preferences[PreferenceKeys.CAMERA_AWB_LOCK] ?: false,
                antiBanding = preferences[PreferenceKeys.CAMERA_ANTI_BANDING] ?: 3,
                zoom = preferences[PreferenceKeys.CAMERA_ZOOM] ?: 0f
            )
        }

    suspend fun updateCameraControls(value: CameraControlSettings) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.MANUAL_EXPOSURE] = value.manualExposureEnabled
            preferences[PreferenceKeys.MANUAL_ISO] = value.iso
            preferences[PreferenceKeys.MANUAL_SHUTTER_NS] = value.shutterSpeedNs
            preferences[PreferenceKeys.CAMERA_AUTO_EXPOSURE] = value.autoExposure
            preferences[PreferenceKeys.CAMERA_AUTO_FOCUS] = value.autoFocus
            preferences[PreferenceKeys.CAMERA_FOCUS] = value.focus
            preferences[PreferenceKeys.CAMERA_WHITE_BALANCE] = value.whiteBalance
            preferences[PreferenceKeys.CAMERA_AWB_LOCK] = value.whiteBalanceLocked
            preferences[PreferenceKeys.CAMERA_ANTI_BANDING] = value.antiBanding
            preferences[PreferenceKeys.CAMERA_ZOOM] = value.zoom
        }
    }

    suspend fun updateHost(host: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HOST] = host
        }
    }

    suspend fun updateStreamingPort(port: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.STREAMING_PORT] = port
        }
    }

    suspend fun updateCamera(camera: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CAMERA] = camera
        }
    }

    suspend fun updateStreamingState(isStreaming: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_STREAMING] = isStreaming
        }
    }
    suspend fun updateHttpsRunningState(isRunning: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HTTPS_RUNNING] = isRunning
        }
    }
    suspend fun updateStreamConfig(config: StreamConfig) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.WIDTH] = config.width
            preferences[PreferenceKeys.HEIGHT] = config.height
            preferences[PreferenceKeys.JPEG_QUALITY] = config.jpegQuality
            preferences[PreferenceKeys.FPS] = config.fps
            preferences[PreferenceKeys.USE_AVC] = config.useAvc
            config.avcBitrate?.let { preferences[PreferenceKeys.AVC_BITRATE] = it }
        }
    }

    suspend fun updateResolution(width: Int, height: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.WIDTH] = width
            preferences[PreferenceKeys.HEIGHT] = height
        }
    }

    suspend fun updateJpegQuality(quality: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.JPEG_QUALITY] = quality
        }
    }

    suspend fun updateFps(fps: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.FPS] = fps
        }
    }

    suspend fun updateUseAvc(useAvc: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_AVC] = useAvc
        }
    }

    suspend fun updateAvcBitrate(bitrate: Int?) {
        dataStore.edit { preferences ->
            if (bitrate != null) {
                preferences[PreferenceKeys.AVC_BITRATE] = bitrate
            } else {
                preferences.remove(PreferenceKeys.AVC_BITRATE)
            }
        }
    }

    suspend fun updateUseScreenCapture(useScreenCapture: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_SCREEN_CAPTURE] = useScreenCapture
        }
    }
}
