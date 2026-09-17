package com.opendialer.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.opendialer.app.core.telephony.CallManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callManager: CallManager

    companion object {
        const val ACTION_ANSWER = "com.opendialer.app.ACTION_ANSWER"
        const val ACTION_DECLINE = "com.opendialer.app.ACTION_DECLINE"
        const val ACTION_DISCONNECT = "com.opendialer.app.ACTION_DISCONNECT"
        const val ACTION_TOGGLE_MUTE = "com.opendialer.app.ACTION_TOGGLE_MUTE"
        const val ACTION_TOGGLE_SPEAKER = "com.opendialer.app.ACTION_TOGGLE_SPEAKER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ANSWER -> callManager.answer()
            ACTION_DECLINE -> callManager.reject()
            ACTION_DISCONNECT -> callManager.disconnect()
            ACTION_TOGGLE_MUTE -> callManager.toggleMute()
            ACTION_TOGGLE_SPEAKER -> callManager.toggleSpeaker()
        }
    }
}
