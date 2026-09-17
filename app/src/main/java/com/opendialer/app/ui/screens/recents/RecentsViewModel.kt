package com.opendialer.app.ui.screens.recents

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.data.local.dao.BlockedNumberDao
import com.opendialer.app.data.local.entity.BlockedNumberEntity
import com.opendialer.app.data.model.CallLogUiModel
import com.opendialer.app.data.repository.CallLogFilter
import com.opendialer.app.data.repository.CallLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val blockedNumberDao: BlockedNumberDao,
    private val simManager: SimManager
) : ViewModel() {

    private val _callLogs = MutableStateFlow<List<CallLogUiModel>>(emptyList())
    val callLogs: StateFlow<List<CallLogUiModel>> = _callLogs.asStateFlow()

    private val _selectedFilter = MutableStateFlow(CallLogFilter.ALL)
    val selectedFilter: StateFlow<CallLogFilter> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLogs()
    }

    fun setFilter(filter: CallLogFilter) {
        _selectedFilter.value = filter
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            val logs = callLogRepository.getCallLogs(_selectedFilter.value)
            _callLogs.value = logs
            _isLoading.value = false
        }
    }

    fun deleteCall(id: Long) {
        viewModelScope.launch {
            if (callLogRepository.deleteCall(id)) {
                loadLogs()
            }
        }
    }

    fun blockNumber(number: String, contactName: String?) {
        viewModelScope.launch {
            val normalized = PhoneNumberUtils.normalize(number)
            blockedNumberDao.blockNumber(
                BlockedNumberEntity(
                    normalizedNumber = normalized,
                    rawNumber = number,
                    contactName = contactName
                )
            )
        }
    }

    fun placeCall(context: Context, number: String) {
        viewModelScope.launch {
            val resolvedSim = simManager.resolveSimForCall(number)
            try {
                val uri = Uri.parse("tel:" + Uri.encode(number))
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (resolvedSim?.phoneAccountHandle != null) {
                        putExtra(android.telecom.TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, resolvedSim.phoneAccountHandle)
                    }
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        }
    }
}
