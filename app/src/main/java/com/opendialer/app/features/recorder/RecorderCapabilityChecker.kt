package com.opendialer.app.features.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class RecorderEngineType {
    ACOUSTIC_MIC_ENHANCED, // Primary working engine: MIC + in-call acoustic routing boost
    ACCESSIBILITY_STREAM,  // Fallback on supported devices
    SYSTEM_VOICE_CALL      // Direct voice call stream if privileged / system app
}

enum class RecorderStatus {
    READY,
    PERMISSION_MISSING,
    CONSENT_REQUIRED,
    RECORDING_ACTIVE,
    DEVICE_UNSUPPORTED
}

@Singleton
class RecorderCapabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRecommendedEngine(): RecorderEngineType {
        // Test if direct hardware voice call is allowed without SecurityException
        return if (canRecordSource(MediaRecorder.AudioSource.VOICE_CALL)) {
            RecorderEngineType.SYSTEM_VOICE_CALL
        } else {
            // Enhanced Microphone + In-Call acoustic route management (Guaranteed working on all Android versions)
            RecorderEngineType.ACOUSTIC_MIC_ENHANCED
        }
    }

    private fun canRecordSource(source: Int): Boolean {
        if (!hasRecordPermission()) return false
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (minBufferSize <= 0) return false

        return try {
            val record = AudioRecord(source, sampleRate, channelConfig, audioFormat, minBufferSize)
            val canInit = record.state == AudioRecord.STATE_INITIALIZED
            record.release()
            canInit
        } catch (e: Exception) {
            false
        }
    }
}
