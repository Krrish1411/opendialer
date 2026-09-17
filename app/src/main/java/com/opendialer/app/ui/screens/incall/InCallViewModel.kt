package com.opendialer.app.ui.screens.incall

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.telephony.CallManager
import com.opendialer.app.core.telephony.CurrentCallInfo
import com.opendialer.app.data.repository.SettingsRepository
import com.opendialer.app.services.CallRecorderService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InCallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val callState: StateFlow<CurrentCallInfo> = callManager.callState

    init {
        viewModelScope.launch {
            val autoRecord = settingsRepository.autoRecordEnabled.first()
            val consent = settingsRepository.recordConsentAccepted.first()
            if (autoRecord && consent) {
                // Auto recording can be initiated when call turns active
            }
        }
    }

    fun answer() = callManager.answer()
    fun answerWithVideo() = callManager.answerWithVideo()
    fun reject(rejectWithMessage: Boolean = false, text: String? = null) = callManager.reject(rejectWithMessage, text)
    fun disconnect() = callManager.disconnect()
    fun toggleMute() = callManager.toggleMute()
    fun toggleSpeaker() = callManager.toggleSpeaker()
    fun setAudioRoute(route: Int) = callManager.setAudioRoute(route)
    fun toggleHold() = callManager.toggleHold()
    fun playDtmf(digit: Char) = callManager.playDtmf(digit)

    // Call Waiting & Multi-Call
    fun holdAndAnswer() = callManager.holdAndAnswer()
    fun endAndAnswer() = callManager.endAndAnswer()
    fun declineSecondCall() = callManager.declineSecondCall()
    fun swap() = callManager.swap()
    fun mergeConference() = callManager.mergeConference()

    fun toggleRecording(context: Context) {
        val current = callState.value
        if (current.isRecording) {
            val intent = Intent(context, CallRecorderService::class.java).apply {
                action = CallRecorderService.ACTION_STOP
            }
            context.startService(intent)
        } else {
            val intent = Intent(context, CallRecorderService::class.java).apply {
                action = CallRecorderService.ACTION_START
                putExtra(CallRecorderService.EXTRA_NUMBER, current.phoneNumber)
                putExtra(CallRecorderService.EXTRA_NAME, current.displayName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
