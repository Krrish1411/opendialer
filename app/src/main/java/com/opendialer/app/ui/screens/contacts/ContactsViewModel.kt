package com.opendialer.app.ui.screens.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.data.model.ContactUiModel
import com.opendialer.app.data.repository.ContactsRepository
import com.opendialer.app.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val simManager: SimManager
) : ViewModel() {

    val repository: ContactsRepository get() = contactsRepository

    private val _contacts = MutableStateFlow<List<ContactUiModel>>(emptyList())
    val contacts: StateFlow<List<ContactUiModel>> = _contacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadContacts()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        loadContacts(query)
    }

    fun loadContacts(query: String = _searchQuery.value) {
        viewModelScope.launch {
            _isLoading.value = true
            val list = contactsRepository.getContacts(query)
            _contacts.value = list
            _isLoading.value = false
        }
    }

    fun toggleFavorite(contact: ContactUiModel) {
        viewModelScope.launch {
            if (contact.isFavorite) {
                favoritesRepository.removeFavorite(contact.id)
            } else {
                favoritesRepository.addFavorite(
                    contactId = contact.id,
                    name = contact.displayName,
                    number = contact.phoneNumber,
                    photoUri = contact.photoUri
                )
            }
            loadContacts()
        }
    }

    fun placeCall(context: Context, contact: ContactUiModel) {
        viewModelScope.launch {
            val resolvedSim = simManager.resolveSimForCall(contact.phoneNumber)
            try {
                val uri = Uri.parse("tel:" + Uri.encode(contact.phoneNumber))
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (resolvedSim?.phoneAccountHandle != null) {
                        putExtra(android.telecom.TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, resolvedSim.phoneAccountHandle)
                    }
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(contact.phoneNumber))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        }
    }

    fun placeVideoCall(context: Context, contact: ContactUiModel) {
        simManager.placeCall(context, contact.phoneNumber, isVideoCall = true)
    }
}
