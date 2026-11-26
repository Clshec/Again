package com.example.handycam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.handycam.service.CameraControlPersistence
import javax.inject.Inject

/**
 * Application class with Hilt integration.
 * This enables dependency injection throughout the app.
 */
@HiltAndroidApp
class HandyCamApplication : Application() {
    @Inject lateinit var cameraControlPersistence: CameraControlPersistence

    override fun onCreate() {
        super.onCreate()
        cameraControlPersistence.start()
    }
}
