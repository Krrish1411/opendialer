package com.opendialer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_sim_preferences")
data class ContactSimPreferenceEntity(
    @PrimaryKey
    val normalizedNumber: String,
    val preferredSlotIndex: Int,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
