package com.opendialer.app.core.common

object Constants {
    const val GITHUB_OWNER = "Krrish1411"
    const val GITHUB_REPO = "opendialer"
    const val GITHUB_API_LATEST_RELEASE = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    const val DATASTORE_NAME = "opendialer_preferences"
    const val DATABASE_NAME = "opendialer.db"

    // Default SIM modes
    const val SIM_MODE_ASK = -1
    const val SIM_SLOT_1 = 0
    const val SIM_SLOT_2 = 1

    // Notification IDs
    const val NOTIFICATION_ID_INCALL = 1001
    const val NOTIFICATION_ID_RECORDER = 1002
    const val NOTIFICATION_ID_UPDATE = 1003
}
