package com.opendialer.app.data.repository

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.opendialer.app.data.model.BarringType
import com.opendialer.app.data.model.ForwardingReason
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallFeaturesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    /**
     * Executes an MMI code (e.g. *21*123456#, ##21#, *43#) using the targeted SIM's PhoneAccountHandle.
     */
    suspend fun executeMmiCode(code: String, phoneAccountHandle: PhoneAccountHandle? = null): Boolean = withContext(Dispatchers.Main) {
        try {
            val encodedCode = Uri.encode(code)
            val uri = Uri.parse("tel:$encodedCode")
            val intent = Intent(Intent.ACTION_CALL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (phoneAccountHandle != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
                }
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Call Forwarding MMI Helpers
    suspend fun enableCallForwarding(reason: ForwardingReason, targetNumber: String, handle: PhoneAccountHandle? = null): Boolean {
        val code = "*${reason.mmiPrefix}*$targetNumber#"
        return executeMmiCode(code, handle)
    }

    suspend fun disableCallForwarding(reason: ForwardingReason, handle: PhoneAccountHandle? = null): Boolean {
        val code = "##${reason.mmiPrefix}#"
        return executeMmiCode(code, handle)
    }

    suspend fun queryCallForwardingStatus(reason: ForwardingReason, handle: PhoneAccountHandle? = null): Boolean {
        val code = "*#${reason.mmiPrefix}#"
        return executeMmiCode(code, handle)
    }

    // Call Barring MMI Helpers
    suspend fun enableCallBarring(type: BarringType, pin: String = "0000", handle: PhoneAccountHandle? = null): Boolean {
        val code = "*${type.mmiPrefix}*$pin#"
        return executeMmiCode(code, handle)
    }

    suspend fun disableCallBarring(type: BarringType, pin: String = "0000", handle: PhoneAccountHandle? = null): Boolean {
        val code = "#${type.mmiPrefix}*$pin#"
        return executeMmiCode(code, handle)
    }

    suspend fun queryCallBarringStatus(type: BarringType, handle: PhoneAccountHandle? = null): Boolean {
        val code = "*#${type.mmiPrefix}#"
        return executeMmiCode(code, handle)
    }

    // Call Waiting MMI Helpers
    suspend fun enableCallWaiting(handle: PhoneAccountHandle? = null): Boolean {
        return executeMmiCode("*43#", handle)
    }

    suspend fun disableCallWaiting(handle: PhoneAccountHandle? = null): Boolean {
        return executeMmiCode("#43#", handle)
    }

    suspend fun queryCallWaitingStatus(handle: PhoneAccountHandle? = null): Boolean {
        return executeMmiCode("*#43#", handle)
    }

    // Do Not Disturb System Integration
    fun hasDndPolicyAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.isNotificationPolicyAccessGranted ?: false
        } else true
    }

    fun setDndFilter(enablePriorityOnly: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasDndPolicyAccess()) {
            val filter = if (enablePriorityOnly) {
                NotificationManager.INTERRUPTION_FILTER_PRIORITY
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
            notificationManager?.setInterruptionFilter(filter)
        }
    }
}
