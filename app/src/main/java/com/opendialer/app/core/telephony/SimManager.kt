package com.opendialer.app.core.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.opendialer.app.core.common.Constants
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.data.local.dao.ContactSimPreferenceDao
import com.opendialer.app.data.local.entity.ContactSimPreferenceEntity
import com.opendialer.app.data.model.SimInfo
import com.opendialer.app.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val simPrefDao: ContactSimPreferenceDao,
    private val settingsRepository: SettingsRepository
) {

    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getActiveSims(): List<SimInfo> {
        if (!hasPhoneStatePermission() || subscriptionManager == null) {
            return emptyList()
        }

        val subs: List<SubscriptionInfo>? = subscriptionManager.activeSubscriptionInfoList
        if (subs.isNullOrEmpty()) return emptyList()

        val handles: List<PhoneAccountHandle> = if (telecomManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                telecomManager.callCapablePhoneAccounts
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        return subs.mapIndexed { index, sub ->
            val matchingHandle = handles.find { handle ->
                handle.id.contains(sub.subscriptionId.toString()) ||
                handle.id.contains(sub.iccId ?: "") ||
                handle.id == index.toString()
            } ?: handles.getOrNull(index)

            val number = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    subscriptionManager.getPhoneNumber(sub.subscriptionId)
                } catch (e: Exception) {
                    sub.number
                }
            } else {
                sub.number
            }

            SimInfo(
                subscriptionId = sub.subscriptionId,
                slotIndex = sub.simSlotIndex,
                displayName = sub.displayName?.toString() ?: "SIM ${sub.simSlotIndex + 1}",
                carrierName = sub.carrierName?.toString() ?: "",
                number = number,
                phoneAccountHandle = matchingHandle
            )
        }
    }

    suspend fun resolveSimForCall(targetNumber: String): SimInfo? {
        val activeSims = getActiveSims()
        if (activeSims.isEmpty()) return null
        if (activeSims.size == 1) return activeSims.first()

        val normalized = PhoneNumberUtils.normalize(targetNumber)

        // 1. Check contact-specific preference
        val preferredSlot = simPrefDao.getPreferredSlot(normalized)
        if (preferredSlot != null) {
            val contactSim = activeSims.find { it.slotIndex == preferredSlot }
            if (contactSim != null) return contactSim
        }

        // 2. Check global setting
        val defaultMode = settingsRepository.defaultSimMode.first()
        return when (defaultMode) {
            Constants.SIM_SLOT_1 -> activeSims.find { it.slotIndex == 0 } ?: activeSims.first()
            Constants.SIM_SLOT_2 -> activeSims.find { it.slotIndex == 1 } ?: activeSims.first()
            else -> null // Constants.SIM_MODE_ASK: Caller should show SIM picker sheet
        }
    }

    suspend fun recordSimChoiceForNumber(targetNumber: String, slotIndex: Int) {
        val normalized = PhoneNumberUtils.normalize(targetNumber)
        if (normalized.isNotEmpty()) {
            simPrefDao.setPreferredSlot(
                ContactSimPreferenceEntity(
                    normalizedNumber = normalized,
                    preferredSlotIndex = slotIndex
                )
            )
        }
    }

    fun placeCall(context: Context, targetNumber: String, isVideoCall: Boolean = false, slotIndex: Int? = null) {
        val cleanNumber = targetNumber.trim()
        if (cleanNumber.isBlank()) return

        val activeSims = getActiveSims()
        val chosenSim = if (slotIndex != null) {
            activeSims.find { it.slotIndex == slotIndex } ?: activeSims.firstOrNull()
        } else {
            activeSims.firstOrNull()
        }

        val uri = Uri.parse("tel:" + Uri.encode(cleanNumber))
        val extras = android.os.Bundle().apply {
            if (chosenSim?.phoneAccountHandle != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, chosenSim.phoneAccountHandle)
            }
            if (isVideoCall && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putInt(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_BIDIRECTIONAL)
            }
        }

        // 1. Direct Telecom API (guarantees ViLTE video state on Android 10-14 / Jio / Airtel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && telecomManager != null &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                telecomManager.placeCall(uri, extras)
                return
            } catch (_: Exception) {}
        }

        // 2. Intent ACTION_CALL fallback
        try {
            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtras(extras)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
