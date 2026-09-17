package com.opendialer.app.ui.screens.keypad

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.data.model.ContactUiModel
import com.opendialer.app.data.model.SimInfo
import com.opendialer.app.data.repository.ContactsRepository
import com.opendialer.app.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeypadViewModel @Inject constructor(
    private val simManager: SimManager,
    private val contactsRepository: ContactsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _inputNumber = MutableStateFlow("")
    val inputNumber: StateFlow<String> = _inputNumber.asStateFlow()

    private val _matchedContacts = MutableStateFlow<List<ContactUiModel>>(emptyList())
    val matchedContacts: StateFlow<List<ContactUiModel>> = _matchedContacts.asStateFlow()

    private val _activeSims = MutableStateFlow<List<SimInfo>>(emptyList())
    val activeSims: StateFlow<List<SimInfo>> = _activeSims.asStateFlow()

    private val _selectedSimIndex = MutableStateFlow(0)
    val selectedSimIndex: StateFlow<Int> = _selectedSimIndex.asStateFlow()

    init {
        loadSims()
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

    fun appendDigit(digit: Char) {
        _inputNumber.value += digit
        searchT9(_inputNumber.value)
    }

    fun deleteLastDigit() {
        if (_inputNumber.value.isNotEmpty()) {
            _inputNumber.value = _inputNumber.value.dropLast(1)
            searchT9(_inputNumber.value)
        }
    }

    fun clear() {
        _inputNumber.value = ""
        _matchedContacts.value = emptyList()
    }

    fun pasteNumber(number: String) {
        _inputNumber.value = PhoneNumberUtils.normalize(number)
        searchT9(_inputNumber.value)
    }

    fun selectSim(index: Int) {
        _selectedSimIndex.value = index
    }

    private fun searchT9(query: String) {
        if (query.isEmpty()) {
            _matchedContacts.value = emptyList()
            return
        }
        viewModelScope.launch {
            val all = contactsRepository.getContacts()
            val matched = all.filter { contact ->
                contact.phoneNumber.contains(query) || PhoneNumberUtils.matchesT9(query, contact.displayName)
            }.take(5)
            _matchedContacts.value = matched
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

    fun placeCall(context: Context, targetNumber: String? = null) {
        val numberToCall = targetNumber ?: _inputNumber.value
        if (numberToCall.isBlank()) return

        viewModelScope.launch {
            val sims = _activeSims.value
            val selectedSim = sims.getOrNull(_selectedSimIndex.value) ?: sims.firstOrNull()

            if (selectedSim != null) {
                simManager.recordSimChoiceForNumber(numberToCall, selectedSim.slotIndex)
            }

            try {
                val uri = Uri.parse("tel:" + Uri.encode(numberToCall))
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (selectedSim?.phoneAccountHandle != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, selectedSim.phoneAccountHandle)
                    }
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to DIAL intent if CALL_PHONE permission not granted
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numberToCall))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        }
    }
}
