package com.opendialer.app.data.repository

import com.opendialer.app.data.local.dao.FavoritesDao
import com.opendialer.app.data.local.dao.SpeedDialDao
import com.opendialer.app.data.local.entity.FavoriteEntity
import com.opendialer.app.data.local.entity.SpeedDialEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao,
    private val speedDialDao: SpeedDialDao
) {

    val allFavorites: Flow<List<FavoriteEntity>> = favoritesDao.getAllFavorites()
    val allSpeedDials: Flow<List<SpeedDialEntity>> = speedDialDao.getAllSpeedDials()

    fun isFavorite(contactId: Long): Flow<Boolean> = favoritesDao.isFavorite(contactId)

    suspend fun addFavorite(contactId: Long, name: String, number: String, photoUri: String?, speedDialSlot: Int? = null) {
        favoritesDao.insertFavorite(
            FavoriteEntity(
                contactId = contactId,
                displayName = name,
                phoneNumber = number,
                photoUri = photoUri,
                speedDialSlot = speedDialSlot
            )
        )
    }

    suspend fun removeFavorite(contactId: Long) {
        favoritesDao.deleteByContactId(contactId)
    }

    suspend fun setSpeedDial(slot: Int, contactId: Long?, name: String, number: String) {
        speedDialDao.setSpeedDial(
            SpeedDialEntity(
                slot = slot,
                contactId = contactId,
                displayName = name,
                phoneNumber = number
            )
        )
    }

    suspend fun clearSpeedDial(slot: Int) {
        speedDialDao.clearSlot(slot)
    }

    suspend fun getSpeedDial(slot: Int): SpeedDialEntity? {
        return speedDialDao.getSpeedDialForSlot(slot)
    }
}
