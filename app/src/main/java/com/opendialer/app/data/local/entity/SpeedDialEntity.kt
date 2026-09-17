package com.opendialer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_dial")
data class SpeedDialEntity(
    @PrimaryKey
    val slot: Int, // 1 through 9
    val contactId: Long?,
    val displayName: String,
    val phoneNumber: String
)
