package com.opendialer.app.ui.screens.keypad

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.data.model.CallLogUiModel
import com.opendialer.app.data.model.ContactUiModel
import com.opendialer.app.data.model.SimInfo
import com.opendialer.app.data.repository.CallLogRepository
import com.opendialer.app.data.repository.ContactsRepository
import com.opendialer.app.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class KeypadViewModel @Inject constructor(
    private val simManager: SimManager,
    private val contactsRepository: ContactsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val _inputNumber = MutableStateFlow("")
    val inputNumber: StateFlow<String> = _inputNumber.asStateFlow()

    private val _matchedContacts = MutableStateFlow<List<ContactUiModel>>(emptyList())
    val matchedContacts: StateFlow<List<ContactUiModel>> = _matchedContacts.asStateFlow()

    private val _recentCalls = MutableStateFlow<List<CallLogUiModel>>(emptyList())
    val recentCalls: StateFlow<List<CallLogUiModel>> = _recentCalls.asStateFlow()

    private val _activeSims = MutableStateFlow<List<SimInfo>>(emptyList())
    val activeSims: StateFlow<List<SimInfo>> = _activeSims.asStateFlow()

    private val _selectedSimIndex = MutableStateFlow(0)
    val selectedSimIndex: StateFlow<Int> = _selectedSimIndex.asStateFlow()

    private var cachedContacts: List<ContactUiModel> = emptyList()

    init {
        loadSims()
        preloadData()
    }

    fun loadSims() {
        viewModelScope.launch {
            val sims = simManager.getActiveSims()
            _activeSims.value = sims
            if (sims.isNotEmpty() && _selectedSimIndex.value >= sims.size) {
                _selectedSimIndex.value = 0
            }
        }
    }

    private fun preloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            cachedContacts = contactsRepository.getContacts()
            val recents = callLogRepository.getCallLogs().take(6)
            _recentCalls.value = recents
        }
    }

    fun appendDigit(digit: Char) {
        _inputNumber.value += digit
        performT9Search(_inputNumber.value)
    }

    fun deleteLastDigit() {
        if (_inputNumber.value.isNotEmpty()) {
            _inputNumber.value = _inputNumber.value.dropLast(1)
            performT9Search(_inputNumber.value)
        }
    }

    fun clear() {
        _inputNumber.value = ""
        _matchedContacts.value = emptyList()
    }

    fun pasteNumber(number: String) {
        _inputNumber.value = PhoneNumberUtils.normalize(number)
        performT9Search(_inputNumber.value)
    }

    fun selectSim(index: Int) {
        _selectedSimIndex.value = index
    }

    private fun performT9Search(query: String) {
        if (query.isEmpty()) {
            _matchedContacts.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (cachedContacts.isEmpty()) {
                cachedContacts = withContext(Dispatchers.IO) { contactsRepository.getContacts() }
            }

            val scored = cachedContacts.mapNotNull { contact ->
                val score = PhoneNumberUtils.matchScoreT9(query, contact.displayName, contact.phoneNumber)
                if (score > 0) Pair(contact, score) else null
            }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(15)

            _matchedContacts.value = scored
        }
    }

    fun handleSpeedDial(slot: Int, context: Context) {
        viewModelScope.launch {
            val speedDial = favoritesRepository.getSpeedDial(slot)
            if (speedDial != null && speedDial.phoneNumber.isNotBlank()) {
                placeCall(context, speedDial.phoneNumber)
            }
        }
    }

    fun placeCall(context: Context, targetNumber: String? = null, simSlotIndex: Int? = null) {
        val numberToCall = targetNumber ?: _inputNumber.value
        if (numberToCall.isBlank()) return

        val chosenSlot = simSlotIndex ?: _selectedSimIndex.value
        simManager.placeCall(context, numberToCall, isVideoCall = false, slotIndex = chosenSlot)
    }

    fun placeCarrierVideoCall(context: Context, targetNumber: String? = null) {
        val numberToCall = targetNumber ?: _inputNumber.value
        if (numberToCall.isBlank()) return

        simManager.placeCall(context, numberToCall, isVideoCall = true, slotIndex = _selectedSimIndex.value)
    }

    fun openCreateContact(context: Context) {
        val number = _inputNumber.value.trim()
        if (number.isBlank()) return

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.PHONE, number)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
