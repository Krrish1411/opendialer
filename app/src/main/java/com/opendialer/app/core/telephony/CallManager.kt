package com.opendialer.app.core.telephony

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList
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
    val audioRoute: Int = CallAudioState.ROUTE_EARPIECE,
    val isHeld: Boolean = false,
    val isRecording: Boolean = false,
    val simSlotIndex: Int = 0,
    val isVideoCapable: Boolean = false,
    val isVideoCall: Boolean = false,
    // Multi-call & Call Waiting
    val hasSecondaryCall: Boolean = false,
    val secondaryPhoneNumber: String = "",
    val secondaryDisplayName: String = "",
    val secondaryIsHeld: Boolean = false,
    val waitingCallNumber: String = "",
    val waitingCallName: String = "",
    val isConference: Boolean = false
)

@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    var inCallService: InCallService? = null
    private val calls = CopyOnWriteArrayList<Call>()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _callState = MutableStateFlow(CurrentCallInfo())
    val callState: StateFlow<CurrentCallInfo> = _callState.asStateFlow()

    private var callStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            val primary = getPrimaryCall()
            if (primary != null && primary.state == Call.STATE_ACTIVE) {
                val duration = if (callStartTime > 0) (System.currentTimeMillis() - callStartTime) / 1000 else 0
                _callState.value = _callState.value.copy(durationSeconds = duration)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            updateCallsState()
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            updateCallsState()
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: MutableList<Call>) {
            updateCallsState()
        }
    }

    fun onCallAdded(call: Call) {
        if (!calls.contains(call)) {
            calls.add(call)
            call.registerCallback(callCallback)
        }
        updateCallsState()
    }

    fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        calls.remove(call)
        if (calls.isEmpty()) {
            handler.removeCallbacks(timerRunnable)
            callStartTime = 0

            // Ensure any active recording is properly stopped and saved
            if (_callState.value.isRecording) {
                try {
                    val stopIntent = android.content.Intent(context, com.opendialer.app.services.CallRecorderService::class.java).apply {
                        action = com.opendialer.app.services.CallRecorderService.ACTION_STOP
                    }
                    context.startService(stopIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _callState.value = CurrentCallInfo(state = DialerCallState.DISCONNECTED)
            handler.postDelayed({
                if (calls.isEmpty()) {
                    _callState.value = CurrentCallInfo(state = DialerCallState.IDLE)
                }
            }, 1500)
        } else {
            updateCallsState()
        }
    }

    fun onCallAudioStateChanged(audioState: CallAudioState) {
        val isSpeaker = audioState.route == CallAudioState.ROUTE_SPEAKER
        _callState.value = _callState.value.copy(
            isMuted = audioState.isMuted,
            isSpeakerOn = isSpeaker,
            audioRoute = audioState.route
        )
    }

    fun getPrimaryCall(): Call? {
        return calls.firstOrNull { it.state == Call.STATE_ACTIVE }
            ?: calls.firstOrNull { it.state == Call.STATE_RINGING }
            ?: calls.firstOrNull { it.state == Call.STATE_DIALING || it.state == Call.STATE_CONNECTING }
            ?: calls.firstOrNull { it.state == Call.STATE_HOLDING }
            ?: calls.firstOrNull()
    }

    fun getHoldingCall(): Call? {
        val primary = getPrimaryCall()
        return calls.firstOrNull { it != primary && it.state == Call.STATE_HOLDING }
    }

    fun getWaitingCall(): Call? {
        val primary = getPrimaryCall()
        return calls.firstOrNull { it != primary && it.state == Call.STATE_RINGING }
    }

    private fun updateCallsState() {
        val primary = getPrimaryCall()
        if (primary == null) {
            if (_callState.value.state != DialerCallState.DISCONNECTED) {
                _callState.value = CurrentCallInfo(state = DialerCallState.IDLE)
            }
            return
        }

        val number = primary.details?.handle?.schemeSpecificPart ?: ""
        val resolvedCaller = ContactLookupHelper.resolveCaller(context, number, primary.details?.callerDisplayName)
        val callerName = resolvedCaller.displayName
        val isConference = primary.details?.hasProperty(Call.Details.PROPERTY_CONFERENCE) == true

        val newState = when (primary.state) {
            Call.STATE_RINGING -> DialerCallState.INCOMING
            Call.STATE_DIALING, Call.STATE_CONNECTING -> DialerCallState.OUTGOING
            Call.STATE_ACTIVE -> {
                if (callStartTime == 0L) {
                    val connectTime = primary.details?.connectTimeMillis ?: 0L
                    callStartTime = if (connectTime > 0) connectTime else System.currentTimeMillis()
                    handler.post(timerRunnable)
                }
                DialerCallState.ACTIVE
            }
            Call.STATE_HOLDING -> DialerCallState.ON_HOLD
            Call.STATE_DISCONNECTED -> DialerCallState.DISCONNECTED
            else -> DialerCallState.IDLE
        }

        // Check carrier ViLTE capability
        val canVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            primary.details?.can(Call.Details.CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL) == true
        } else false

        val isVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            VideoProfile.isVideo(primary.details?.videoState ?: VideoProfile.STATE_AUDIO_ONLY)
        } else false

        // Check secondary holding call & waiting call
        val holding = getHoldingCall()
        val waiting = getWaitingCall()

        val secondaryNum = holding?.details?.handle?.schemeSpecificPart ?: ""
        val secondaryName = if (holding != null) {
            ContactLookupHelper.resolveCaller(context, secondaryNum, holding.details?.callerDisplayName).displayName
        } else secondaryNum

        val waitingNum = waiting?.details?.handle?.schemeSpecificPart ?: ""
        val waitingName = if (waiting != null) {
            ContactLookupHelper.resolveCaller(context, waitingNum, waiting.details?.callerDisplayName).displayName
        } else waitingNum

        _callState.value = _callState.value.copy(
            state = newState,
            phoneNumber = number,
            displayName = callerName,
            isHeld = primary.state == Call.STATE_HOLDING,
            isVideoCapable = canVideo,
            isVideoCall = isVideo,
            isConference = isConference,
            hasSecondaryCall = holding != null,
            secondaryPhoneNumber = secondaryNum,
            secondaryDisplayName = secondaryName,
            secondaryIsHeld = true,
            waitingCallNumber = waitingNum,
            waitingCallName = waitingName
        )
    }

    // Call Actions
    fun answer() {
        getPrimaryCall()?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun answerWithVideo() {
        getPrimaryCall()?.answer(VideoProfile.STATE_BIDIRECTIONAL)
    }

    fun reject(rejectWithMessage: Boolean = false, textMessage: String? = null) {
        val ringing = calls.firstOrNull { it.state == Call.STATE_RINGING } ?: getPrimaryCall()
        if (ringing != null) {
            if (rejectWithMessage && textMessage != null) {
                ringing.reject(true, textMessage)
            } else {
                ringing.reject(false, null)
            }
        }
    }

    fun disconnect() {
        val active = getPrimaryCall()
        active?.disconnect() ?: run {
            calls.forEach { it.disconnect() }
        }
    }

    fun toggleMute() {
        val newMute = !_callState.value.isMuted
        inCallService?.setMuted(newMute)
        audioManager?.isMicrophoneMute = newMute
        _callState.value = _callState.value.copy(isMuted = newMute)
    }

    fun toggleSpeaker() {
        val targetRoute = if (_callState.value.isSpeakerOn) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        setAudioRoute(targetRoute)
    }

    fun setAudioRoute(route: Int) {
        inCallService?.setAudioRoute(route)
        val isSpeaker = route == CallAudioState.ROUTE_SPEAKER
        audioManager?.isSpeakerphoneOn = isSpeaker
        _callState.value = _callState.value.copy(
            isSpeakerOn = isSpeaker,
            audioRoute = route
        )
    }

    fun toggleHold() {
        val call = getPrimaryCall() ?: return
        if (call.state == Call.STATE_HOLDING) {
            call.unhold()
        } else if (call.state == Call.STATE_ACTIVE) {
            call.hold()
        }
    }

    // Multi-Call / Call Waiting Actions
    fun holdAndAnswer() {
        val waiting = getWaitingCall() ?: return
        val active = calls.firstOrNull { it.state == Call.STATE_ACTIVE }
        active?.hold()
        waiting.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun endAndAnswer() {
        val waiting = getWaitingCall() ?: return
        val active = calls.firstOrNull { it.state == Call.STATE_ACTIVE }
        active?.disconnect()
        waiting.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun declineSecondCall() {
        val waiting = getWaitingCall() ?: return
        waiting.reject(false, null)
    }

    fun swap() {
        val holding = getHoldingCall()
        val active = calls.firstOrNull { it.state == Call.STATE_ACTIVE }
        active?.hold()
        holding?.unhold()
    }

    fun mergeConference() {
        val primary = getPrimaryCall() ?: return
        val confCalls = primary.conferenceableCalls
        if (confCalls.isNotEmpty()) {
            primary.conference(confCalls.first())
        } else if (primary.details?.can(Call.Details.CAPABILITY_MERGE_CONFERENCE) == true) {
            primary.mergeConference()
        } else {
            val other = calls.firstOrNull { it != primary }
            if (other != null) {
                primary.conference(other)
            }
        }
    }

    fun playDtmf(digit: Char) {
        val primary = getPrimaryCall()
        primary?.playDtmfTone(digit)
        handler.postDelayed({
            primary?.stopDtmfTone()
        }, 150)
    }

    fun setRecording(isRecording: Boolean) {
        _callState.value = _callState.value.copy(isRecording = isRecording)
    }
}
