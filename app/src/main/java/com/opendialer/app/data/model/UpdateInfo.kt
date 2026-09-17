package com.opendialer.app.data.model

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionName: String,
    val downloadUrl: String?,
    val changelog: String?,
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)
