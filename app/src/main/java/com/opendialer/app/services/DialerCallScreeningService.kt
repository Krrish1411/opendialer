package com.opendialer.app.services

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.data.local.dao.BlockedNumberDao
import com.opendialer.app.data.repository.ContactsRepository
import com.opendialer.app.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.N)
class DialerCallScreeningService : CallScreeningService() {

    @Inject
    lateinit var blockedNumberDao: BlockedNumberDao

    @Inject
    lateinit var contactsRepository: ContactsRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart ?: return
        val normalized = PhoneNumberUtils.normalize(rawNumber)

        serviceScope.launch {
            val isBlocked = blockedNumberDao.isBlocked(normalized)
            val silenceUnknown = settingsRepository.dndSilenceUnknown.first()

            val contact = contactsRepository.getContactByNumber(rawNumber)

            val shouldBlock = isBlocked || (silenceUnknown && contact == null)

            val response = CallResponse.Builder()
                .setDisallowCall(shouldBlock)
                .setRejectCall(shouldBlock)
                .setSilenceCall(shouldBlock)
                .setSkipCallLog(false)
                .setSkipNotification(shouldBlock)
                .build()

            respondToCall(callDetails, response)
        }
    }
}
