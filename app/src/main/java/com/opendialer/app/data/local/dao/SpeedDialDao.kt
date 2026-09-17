package com.opendialer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.opendialer.app.data.local.entity.SpeedDialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedDialDao {
    @Query("SELECT * FROM speed_dial ORDER BY slot ASC")
    fun getAllSpeedDials(): Flow<List<SpeedDialEntity>>

    @Query("SELECT * FROM speed_dial WHERE slot = :slot LIMIT 1")
    suspend fun getSpeedDialForSlot(slot: Int): SpeedDialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSpeedDial(speedDial: SpeedDialEntity)

    @Query("DELETE FROM speed_dial WHERE slot = :slot")
    suspend fun clearSlot(slot: Int)
}
