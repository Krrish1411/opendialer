package com.opendialer.app.core.telephony

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import com.opendialer.app.core.common.PhoneNumberUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class DialerCallState {
    IDLE,
    INCOMING,
    OUTGOING,
    ACTIVE,
    ON_HOLD,
    DISCONNECTED
}

data class CurrentCallInfo(
    val state: DialerCallState = DialerCallState.IDLE,
    val phoneNumber: String = "",
    val displayName: String = "",
    val durationSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isHeld: Boolean = false,
    val isRecording: Boolean = false,
    val simSlotIndex: Int = 0
)

@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var currentCall: Call? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _callState = MutableStateFlow(CurrentCallInfo())
    val callState: StateFlow<CurrentCallInfo> = _callState.asStateFlow()

    private var callStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (currentCall != null && _callState.value.state == DialerCallState.ACTIVE) {
                val duration = (System.currentTimeMillis() - callStartTime) / 1000
                _callState.value = _callState.value.copy(durationSeconds = duration)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            updateFromCall(call)
        }

        override fun onCallAudioStateChanged(call: Call, audioState: CallAudioState) {
            val isSpeaker = audioState.route == CallAudioState.ROUTE_SPEAKER
            _callState.value = _callState.value.copy(
                isMuted = audioState.isMuted,
                isSpeakerOn = isSpeaker
            )
        }
    }

    fun onCallAdded(call: Call) {
        currentCall = call
        call.registerCallback(callCallback)
        updateFromCall(call)
    }

    fun onCallRemoved(call: Call) {
        if (currentCall == call) {
            call.unregisterCallback(callCallback)
            currentCall = null
            handler.removeCallbacks(timerRunnable)
            _callState.value = CurrentCallInfo(state = DialerCallState.DISCONNECTED)

            handler.postDelayed({
                _callState.value = CurrentCallInfo(state = DialerCallState.IDLE)
            }, 2000)
        }
    }

    private fun updateFromCall(call: Call) {
        val number = call.details?.handle?.schemeSpecificPart ?: ""
        val callerName = call.details?.callerDisplayName ?: ""

        val newState = when (call.state) {
            Call.STATE_RINGING -> DialerCallState.INCOMING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> DialerCallState.OUTGOING
            Call.STATE_ACTIVE -> {
                if (callStartTime == 0L) {
                    callStartTime = System.currentTimeMillis()
                    handler.post(timerRunnable)
                }
                DialerCallState.ACTIVE
            }
            Call.STATE_HOLDING -> DialerCallState.ON_HOLD
            Call.STATE_DISCONNECTED -> DialerCallState.DISCONNECTED
            else -> DialerCallState.IDLE
        }

        _callState.value = _callState.value.copy(
            state = newState,
            phoneNumber = number,
            displayName = callerName.ifEmpty { number }
        )
    }

    fun answer() {
        currentCall?.answer(0)
    }

    fun reject() {
        currentCall?.reject(false, null)
    }

    fun disconnect() {
        currentCall?.disconnect()
    }

    fun toggleMute() {
        val newMute = !_callState.value.isMuted
        audioManager?.isMicrophoneMute = newMute
        _callState.value = _callState.value.copy(isMuted = newMute)
    }

    fun toggleSpeaker() {
        val targetRoute = if (_callState.value.isSpeakerOn) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        audioManager?.isSpeakerphoneOn = !_callState.value.isSpeakerOn
        _callState.value = _callState.value.copy(isSpeakerOn = !_callState.value.isSpeakerOn)
    }

    fun toggleHold() {
        currentCall?.let {
            if (it.state == Call.STATE_HOLDING) {
                it.unhold()
            } else {
                it.hold()
            }
        }
    }

    fun playDtmf(digit: Char) {
        currentCall?.playDtmfTone(digit)
        handler.postDelayed({
            currentCall?.stopDtmfTone()
        }, 150)
    }

    fun setRecording(isRecording: Boolean) {
        _callState.value = _callState.value.copy(isRecording = isRecording)
    }
}
