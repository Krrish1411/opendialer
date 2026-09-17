package com.opendialer.app.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.data.model.CallLogUiModel
import com.opendialer.app.data.model.CallType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class CallLogFilter {
    ALL,
    MISSED,
    INCOMING,
    OUTGOING
}

@Singleton
class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun hasReadCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCallLogs(filter: CallLogFilter = CallLogFilter.ALL): List<CallLogUiModel> = withContext(Dispatchers.IO) {
        if (!hasReadCallLogPermission()) return@withContext emptyList()

        val logs = mutableListOf<CallLogUiModel>()

        val projection = mutableListOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.IS_READ
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            projection.add(CallLog.Calls.PHONE_ACCOUNT_ID)
        }

        val selection = when (filter) {
            CallLogFilter.ALL -> null
            CallLogFilter.MISSED -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.MISSED_TYPE}"
            CallLogFilter.INCOMING -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}"
            CallLogFilter.OUTGOING -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}"
        }

        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT 150"

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection.toTypedArray(),
            selection,
            null,
            sortOrder
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndex(CallLog.Calls._ID)
            val numCol = c.getColumnIndex(CallLog.Calls.NUMBER)
            val nameCol = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeCol = c.getColumnIndex(CallLog.Calls.TYPE)
            val dateCol = c.getColumnIndex(CallLog.Calls.DATE)
            val durCol = c.getColumnIndex(CallLog.Calls.DURATION)
            val readCol = c.getColumnIndex(CallLog.Calls.IS_READ)
            val simIdCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                c.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
            } else -1

            while (c.moveToNext()) {
                val id = if (idCol >= 0) c.getLong(idCol) else 0L
                val rawNumber = if (numCol >= 0) c.getString(numCol) ?: "" else ""
                val cachedName = if (nameCol >= 0) c.getString(nameCol) else null
                val typeInt = if (typeCol >= 0) c.getInt(typeCol) else CallLog.Calls.INCOMING_TYPE
                val date = if (dateCol >= 0) c.getLong(dateCol) else 0L
                val duration = if (durCol >= 0) c.getLong(durCol) else 0L
                val isRead = if (readCol >= 0) c.getInt(readCol) == 1 else true

                val simSlot = if (simIdCol >= 0) {
                    val simId = c.getString(simIdCol) ?: "0"
                    if (simId.contains("1") || simId.endsWith("1")) 1 else 0
                } else 0

                val simDisplayName = if (simSlot == 0) "SIM 1" else "SIM 2"

                // Group repeated consecutive calls
                if (logs.isNotEmpty() && logs.last().number == rawNumber && logs.last().callType == CallType.fromCallLogType(typeInt)) {
                    val last = logs.removeAt(logs.size - 1)
                    logs.add(last.copy(callCount = last.callCount + 1))
                } else {
                    logs.add(
                        CallLogUiModel(
                            id = id,
                            contactName = cachedName,
                            number = rawNumber,
                            callType = CallType.fromCallLogType(typeInt),
                            timestamp = date,
                            durationSeconds = duration,
                            simSlotIndex = simSlot,
                            simDisplayName = simDisplayName,
                            isRead = isRead
                        )
                    )
                }
            }
        }

        logs
    }

    suspend fun deleteCall(id: Long): Boolean = withContext(Dispatchers.IO) {
        if (!hasReadCallLogPermission()) return@withContext false
        try {
            val uri = CallLog.Calls.CONTENT_URI
            val rows = context.contentResolver.delete(uri, "${CallLog.Calls._ID} = ?", arrayOf(id.toString()))
            rows > 0
        } catch (e: Exception) {
            false
        }
    }
}
