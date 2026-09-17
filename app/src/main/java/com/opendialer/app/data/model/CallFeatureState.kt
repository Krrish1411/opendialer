package com.opendialer.app.data.model

enum class ForwardingReason(val mmiPrefix: String, val title: String) {
    ALWAYS("21", "Always Forward"),
    BUSY("67", "When Busy"),
    UNANSWERED("61", "When Unanswered"),
    UNREACHABLE("62", "When Unreachable")
}

data class ForwardingRule(
    val reason: ForwardingReason,
    val isEnabled: Boolean = false,
    val targetNumber: String = ""
)

enum class BarringType(val mmiPrefix: String, val title: String) {
    ALL_OUTGOING("33", "All Outgoing Calls"),
    INTERNATIONAL_OUTGOING("331", "International Outgoing"),
    ALL_INCOMING("35", "All Incoming Calls"),
    ROAMING_INCOMING("351", "Incoming When Roaming")
}

data class DndSettings(
    val isEnabled: Boolean = false,
    val allowStarredContactsOnly: Boolean = true,
    val allowRepeatedCallers: Boolean = true,
    val silenceUnknownNumbers: Boolean = false
)
