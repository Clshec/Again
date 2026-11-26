package com.example.handycam

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

internal fun stableCameraHost(context: Context): String {
    val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        ?: android.os.Build.MODEL
    return stableCameraHostForId(id)
}

internal fun stableCameraHostForId(id: String): String {
    val suffix = MessageDigest.getInstance("SHA-256")
        .digest(id.toByteArray(Charsets.UTF_8))
        .take(4).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    return "handycam-$suffix.local"
}
