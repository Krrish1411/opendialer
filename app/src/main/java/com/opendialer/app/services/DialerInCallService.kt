package com.opendialer.app.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import com.opendialer.app.DialerApplication
import com.opendialer.app.MainActivity
import com.opendialer.app.core.common.Constants
import com.opendialer.app.core.telephony.CallManager
import com.opendialer.app.ui.screens.incall.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DialerInCallService : InCallService() {

    @Inject
    lateinit var callManager: CallManager

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callManager.onCallAdded(call)

        val number = call.details?.handle?.schemeSpecificPart ?: "Unknown"

        if (call.state == Call.STATE_RINGING) {
            // Full Screen Incoming Call Activity for Lock Screen
            val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingFullScreen = PendingIntent.getActivity(
                this,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, DialerApplication.CHANNEL_INCALL)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setContentTitle("Incoming Call")
                .setContentText(number)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingFullScreen, true)
                .setOngoing(true)
                .setAutoCancel(false)
                .build()

            startForeground(Constants.NOTIFICATION_ID_INCALL, notification)

            startActivity(fullScreenIntent)
        } else {
            // Outgoing / Active Call notification
            val contentIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingContent = PendingIntent.getActivity(
                this,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, DialerApplication.CHANNEL_INCALL)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setContentTitle("Ongoing Call")
                .setContentText(number)
                .setContentIntent(pendingContent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            startForeground(Constants.NOTIFICATION_ID_INCALL, notification)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callManager.onCallRemoved(call)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
