package com.opendialer.app.data.model

data class ContactUiModel(
    val id: Long,
    val lookupKey: String = "",
    val displayName: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val accountType: String? = null,
    val preferredSimSlot: Int? = null // Remembers last used SIM
)
