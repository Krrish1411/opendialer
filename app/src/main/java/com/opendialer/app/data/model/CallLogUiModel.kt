package com.opendialer.app.data.model

import android.provider.CallLog

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED;

    companion object {
        fun fromCallLogType(type: Int): CallType {
            return when (type) {
                CallLog.Calls.INCOMING_TYPE -> INCOMING
                CallLog.Calls.OUTGOING_TYPE -> OUTGOING
                CallLog.Calls.MISSED_TYPE -> MISSED
                CallLog.Calls.REJECTED_TYPE -> REJECTED
                CallLog.Calls.BLOCKED_TYPE -> BLOCKED
                else -> INCOMING
            }
        }
    }
}

data class CallLogUiModel(
    val id: Long,
    val contactName: String?,
    val number: String,
    val callType: CallType,
    val timestamp: Long,
    val durationSeconds: Long,
    val simSlotIndex: Int = 0,
    val simDisplayName: String = "SIM 1",
    val photoUri: String? = null,
    val isRead: Boolean = true,
    val recordingId: Long? = null,
    val callCount: Int = 1 // Grouped repeated calls
)
