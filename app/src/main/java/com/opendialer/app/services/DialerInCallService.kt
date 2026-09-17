package com.opendialer.app.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import com.opendialer.app.DialerApplication
import com.opendialer.app.R
import com.opendialer.app.core.common.Constants
import com.opendialer.app.core.telephony.CallManager
import com.opendialer.app.ui.screens.incall.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DialerInCallService : InCallService() {

    @Inject
    lateinit var callManager: CallManager

    private val powerManager by lazy {
        getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callManager.inCallService = this
        callManager.onCallAdded(call)

        val number = call.details?.handle?.schemeSpecificPart ?: "Unknown"
        val callerName = call.details?.callerDisplayName ?: number

        val isOutgoing = call.state == Call.STATE_DIALING || call.state == Call.STATE_CONNECTING
        val isIncoming = call.state == Call.STATE_RINGING

        // Prepare full-screen intent for IncomingCallActivity
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFullScreen = PendingIntent.getActivity(
            this,
            100,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (isIncoming) {
            // PendingIntents for Notification Action Buttons
            val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = CallActionReceiver.ACTION_ANSWER
            }
            val pendingAnswer = PendingIntent.getBroadcast(
                this, 101, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = CallActionReceiver.ACTION_DECLINE
            }
            val pendingDecline = PendingIntent.getBroadcast(
                this, 102, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, DialerApplication.CHANNEL_INCALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(callerName)
                .setContentText("Incoming call ($number)")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingFullScreen, true)
                .setContentIntent(pendingFullScreen)
                .setOngoing(true)
                .setAutoCancel(false)
                .addAction(android.R.drawable.ic_menu_call, "Answer", pendingAnswer)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", pendingDecline)
                .build()

            startInCallForeground(Constants.NOTIFICATION_ID_INCALL, notification)

            // If phone screen is off / locked, wake device immediately to show full screen
            val isInteractive = powerManager?.isInteractive ?: false
            if (!isInteractive) {
                startActivity(fullScreenIntent)
            }
        } else {
            // Outgoing / Active Call Notification
            val disconnectIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = CallActionReceiver.ACTION_DISCONNECT
            }
            val pendingDisconnect = PendingIntent.getBroadcast(
                this, 103, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val toggleMuteIntent = Intent(this, CallActionReceiver::class.java).apply {
                action = CallActionReceiver.ACTION_TOGGLE_MUTE
            }
            val pendingMute = PendingIntent.getBroadcast(
                this, 104, toggleMuteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, DialerApplication.CHANNEL_INCALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(callerName)
                .setContentText(if (isOutgoing) "Calling..." else "Ongoing Call ($number)")
                .setContentIntent(pendingFullScreen)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Call", pendingDisconnect)
                .addAction(android.R.drawable.ic_lock_silent_mode, "Mute", pendingMute)
                .build()

            startInCallForeground(Constants.NOTIFICATION_ID_INCALL, notification)

            // For outgoing calls, open in-call screen immediately
            startActivity(fullScreenIntent)
        }
    }

    private fun startInCallForeground(notificationId: Int, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(notificationId, notification)
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        callManager.onCallAudioStateChanged(audioState)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callManager.onCallRemoved(call)
        if (callManager.getPrimaryCall() == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callManager.inCallService = null
    }
}
