package com.opendialer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.opendialer.app.data.local.dao.BlockedNumberDao
import com.opendialer.app.data.local.dao.ContactSimPreferenceDao
import com.opendialer.app.data.local.dao.FavoritesDao
import com.opendialer.app.data.local.dao.RecordingDao
import com.opendialer.app.data.local.dao.SpeedDialDao
import com.opendialer.app.data.local.entity.BlockedNumberEntity
import com.opendialer.app.data.local.entity.ContactSimPreferenceEntity
import com.opendialer.app.data.local.entity.FavoriteEntity
import com.opendialer.app.data.local.entity.RecordingEntity
import com.opendialer.app.data.local.entity.SpeedDialEntity

@Database(
    entities = [
        FavoriteEntity::class,
        SpeedDialEntity::class,
        BlockedNumberEntity::class,
        RecordingEntity::class,
        ContactSimPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DialerDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun speedDialDao(): SpeedDialDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun recordingDao(): RecordingDao
    abstract fun contactSimPreferenceDao(): ContactSimPreferenceDao
}
