package com.opendialer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val contactId: Long,
    val displayName: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val sortOrder: Int = 0,
    val speedDialSlot: Int? = null
)
