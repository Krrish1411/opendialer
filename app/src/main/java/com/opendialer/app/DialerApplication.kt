package com.opendialer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DialerApplication : Application() {

    companion object {
        const val CHANNEL_INCALL = "channel_incall"
        const val CHANNEL_RECORDER = "channel_recorder"
        const val CHANNEL_UPDATES = "channel_updates"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // High Priority In-Call Notification Channel
            val callChannel = NotificationChannel(
                CHANNEL_INCALL,
                "Phone Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ongoing and incoming phone calls"
                setSound(null, null)
                enableVibration(true)
            }

            // Foreground Audio Recorder Channel
            val recorderChannel = NotificationChannel(
                CHANNEL_RECORDER,
                "Call Recorder",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active call recording indicator"
                setShowBadge(false)
            }

            // App Updates Channel
            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New release notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(callChannel, recorderChannel, updatesChannel)
            )
        }
    }
}
