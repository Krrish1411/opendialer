package com.opendialer.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.core.designsystem.theme.ThemeId
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.core.updater.OtaUpdateManager
import com.opendialer.app.data.local.dao.BlockedNumberDao
import com.opendialer.app.data.local.entity.BlockedNumberEntity
import com.opendialer.app.data.model.BarringType
import com.opendialer.app.data.model.ForwardingReason
import com.opendialer.app.data.model.SimInfo
import com.opendialer.app.data.model.UpdateInfo
import com.opendialer.app.data.repository.CallFeaturesRepository
import com.opendialer.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val simManager: SimManager,
    private val callFeaturesRepository: CallFeaturesRepository,
    private val otaUpdateManager: OtaUpdateManager,
    private val blockedNumberDao: BlockedNumberDao
) : ViewModel() {

    val blockedNumbers: Flow<List<BlockedNumberEntity>> = blockedNumberDao.getAllBlocked()

    val selectedTheme: StateFlow<ThemeId> = settingsRepository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeId.AURA_MODERN)

    val defaultSimMode: StateFlow<Int> = settingsRepository.defaultSimMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val autoRecordEnabled: StateFlow<Boolean> = settingsRepository.autoRecordEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recordConsentAccepted: StateFlow<Boolean> = settingsRepository.recordConsentAccepted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recordSpeakerBoost: StateFlow<Boolean> = settingsRepository.recordSpeakerBoost
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dynamicColor: StateFlow<Boolean> = settingsRepository.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkMode: StateFlow<Boolean> = settingsRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val incomingAnimationEnabled: StateFlow<Boolean> = settingsRepository.incomingAnimationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dndSilenceUnknown: StateFlow<Boolean> = settingsRepository.dndSilenceUnknown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dndVipOnly: StateFlow<Boolean> = settingsRepository.dndVipOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _activeSims = MutableStateFlow<List<SimInfo>>(emptyList())
    val activeSims: StateFlow<List<SimInfo>> = _activeSims.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    init {
        loadSims()
    }

    fun loadSims() {
        viewModelScope.launch {
            _activeSims.value = simManager.getActiveSims()
        }
    }

    fun setTheme(themeId: ThemeId) {
        viewModelScope.launch { settingsRepository.setTheme(themeId) }
    }

    fun setDefaultSimMode(mode: Int) {
        viewModelScope.launch { settingsRepository.setDefaultSimMode(mode) }
    }

    fun setAutoRecord(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoRecordEnabled(enabled) }
    }

    fun setRecordConsent(accepted: Boolean) {
        viewModelScope.launch { settingsRepository.setRecordConsentAccepted(accepted) }
    }

    fun setRecordSpeakerBoost(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRecordSpeakerBoost(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkMode(enabled) }
    }

    fun setIncomingAnimation(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setIncomingAnimationEnabled(enabled) }
    }

    fun setDndSilenceUnknown(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDndSilenceUnknown(enabled) }
    }

    fun setDndVipOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDndVipOnly(enabled)
            callFeaturesRepository.setDndFilter(enabled)
        }
    }

    // Carrier Feature Actions (MMI)
    fun setCallForwarding(reason: ForwardingReason, number: String, enable: Boolean) {
        viewModelScope.launch {
            val handle = _activeSims.value.firstOrNull()?.phoneAccountHandle
            if (enable) {
                callFeaturesRepository.enableCallForwarding(reason, number, handle)
            } else {
                callFeaturesRepository.disableCallForwarding(reason, handle)
            }
        }
    }

    fun setCallBarring(type: BarringType, enable: Boolean, pin: String) {
        viewModelScope.launch {
            val handle = _activeSims.value.firstOrNull()?.phoneAccountHandle
            if (enable) {
                callFeaturesRepository.enableCallBarring(type, pin, handle)
            } else {
                callFeaturesRepository.disableCallBarring(type, pin, handle)
            }
        }
    }

    fun setCallWaiting(enable: Boolean) {
        viewModelScope.launch {
            val handle = _activeSims.value.firstOrNull()?.phoneAccountHandle
            if (enable) {
                callFeaturesRepository.enableCallWaiting(handle)
            } else {
                callFeaturesRepository.disableCallWaiting(handle)
            }
        }
    }

    // OTA Updates Check & Install
    fun checkForUpdates() {
        viewModelScope.launch {
            _updateInfo.value = UpdateInfo(
                hasUpdate = false,
                latestVersionName = "",
                downloadUrl = null,
                changelog = null,
                isChecking = true
            )
            val info = otaUpdateManager.checkForUpdates()
            _updateInfo.value = info
        }
    }

    fun downloadAndInstallUpdate() {
        val url = _updateInfo.value?.downloadUrl ?: return
        viewModelScope.launch {
            _downloadProgress.value = 0.01f
            otaUpdateManager.downloadAndInstallApk(url) { progress ->
                _downloadProgress.value = progress
            }
            _downloadProgress.value = null
        }
    }

    fun blockNumber(number: String, name: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalized = PhoneNumberUtils.normalize(number)
            if (normalized.isNotEmpty()) {
                blockedNumberDao.blockNumber(
                    BlockedNumberEntity(
                        normalizedNumber = normalized,
                        rawNumber = number,
                        contactName = name
                    )
                )
            }
        }
    }

    fun unblockNumber(number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalized = PhoneNumberUtils.normalize(number)
            if (normalized.isNotEmpty()) {
                blockedNumberDao.unblockByNumber(normalized)
            }
        }
    }
}
