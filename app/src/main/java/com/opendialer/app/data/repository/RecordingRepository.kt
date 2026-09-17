package com.opendialer.app.data.repository

import com.opendialer.app.data.local.dao.RecordingDao
import com.opendialer.app.data.local.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val recordingDao: RecordingDao
) {

    val allRecordings: Flow<List<RecordingEntity>> = recordingDao.getAllRecordings()

    fun searchRecordings(query: String): Flow<List<RecordingEntity>> = recordingDao.searchRecordings(query)

    suspend fun getRecordingById(id: Long): RecordingEntity? = recordingDao.getRecordingById(id)

    suspend fun saveRecording(
        filePath: String,
        contactName: String?,
        phoneNumber: String,
        durationSeconds: Long,
        simLabel: String,
        sizeBytes: Long
    ): Long {
        return recordingDao.insertRecording(
            RecordingEntity(
                filePath = filePath,
                contactName = contactName,
                phoneNumber = phoneNumber,
                createdAt = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                simLabel = simLabel,
                sizeBytes = sizeBytes
            )
        )
    }

    suspend fun deleteRecording(recording: RecordingEntity): Boolean {
        return try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
            recordingDao.deleteRecording(recording)
            true
        } catch (e: Exception) {
            false
        }
    }
}
