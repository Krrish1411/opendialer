package com.opendialer.app.data.model

import android.telecom.PhoneAccountHandle

data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String? = null,
    val phoneAccountHandle: PhoneAccountHandle? = null,
    val isDefaultData: Boolean = false,
    val isDefaultVoice: Boolean = false
) {
    val displayLabel: String
        get() = when {
            displayName.isNotBlank() && !displayName.startsWith("SIM") -> "$displayName (SIM ${slotIndex + 1})"
            carrierName.isNotBlank() -> "$carrierName ${slotIndex + 1}"
            else -> "SIM ${slotIndex + 1}"
        }
}
