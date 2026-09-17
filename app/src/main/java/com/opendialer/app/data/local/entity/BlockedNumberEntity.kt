package com.opendialer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey
    val normalizedNumber: String,
    val rawNumber: String,
    val contactName: String? = null,
    val blockedAt: Long = System.currentTimeMillis()
)
