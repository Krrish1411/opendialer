package com.opendialer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.opendialer.app.data.local.entity.ContactSimPreferenceEntity

@Dao
interface ContactSimPreferenceDao {
    @Query("SELECT preferredSlotIndex FROM contact_sim_preferences WHERE normalizedNumber = :normalizedNumber LIMIT 1")
    suspend fun getPreferredSlot(normalizedNumber: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreferredSlot(pref: ContactSimPreferenceEntity)
}
