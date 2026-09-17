package com.opendialer.app.ui.screens.favorites

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.data.local.entity.FavoriteEntity
import com.opendialer.app.data.local.entity.SpeedDialEntity
import com.opendialer.app.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val simManager: SimManager
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = favoritesRepository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speedDials: StateFlow<List<SpeedDialEntity>> = favoritesRepository.allSpeedDials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(contactId: Long) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(contactId)
        }
    }

    fun setSpeedDial(slot: Int, name: String, number: String) {
        viewModelScope.launch {
            favoritesRepository.setSpeedDial(slot, null, name, number)
        }
    }

    fun clearSpeedDial(slot: Int) {
        viewModelScope.launch {
            favoritesRepository.clearSpeedDial(slot)
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
