package com.opendialer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val contactName: String?,
    val phoneNumber: String,
    val createdAt: Long,
    val durationSeconds: Long,
    val simLabel: String,
    val sizeBytes: Long
)
