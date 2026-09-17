package com.opendialer.app.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.opendialer.app.DialerApplication
import com.opendialer.app.MainActivity
import com.opendialer.app.core.common.Constants
import com.opendialer.app.core.telephony.CallManager
import com.opendialer.app.data.repository.RecordingRepository
import com.opendialer.app.data.repository.SettingsRepository
import com.opendialer.app.features.recorder.RecorderCapabilityChecker
import com.opendialer.app.features.recorder.RecorderEngineType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CallRecorderService : Service() {

    @Inject
    lateinit var recordingRepository: RecordingRepository

    @Inject
    lateinit var capabilityChecker: RecorderCapabilityChecker

    @Inject
    lateinit var callManager: CallManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordStartTime: Long = 0
    private var currentNumber: String = ""
    private var currentName: String = ""
    private var previousSpeakerState: Boolean = false

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ACTION_START = "com.opendialer.action.START_RECORD"
        const val ACTION_STOP = "com.opendialer.action.STOP_RECORD"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_NAME = "extra_name"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentNumber = intent.getStringExtra(EXTRA_NUMBER) ?: "Unknown"
                currentName = intent.getStringExtra(EXTRA_NAME) ?: currentNumber
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (!capabilityChecker.hasRecordPermission()) {
            stopSelf()
            return
        }

        try {
            val recDir = getExternalFilesDir("recordings") ?: filesDir
            if (!recDir.exists()) recDir.mkdirs()

            val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val cleanNum = currentNumber.replace("[^0-9+]".toRegex(), "")
            recordingFile = File(recDir, "Call_${cleanNum}_$timestampStr.m4a")

            // Auto-boost speakerphone if enabled in settings so both parties are cleanly captured
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            previousSpeakerState = audioManager?.isSpeakerphoneOn ?: false
            serviceScope.launch {
                val shouldBoostSpeaker = settingsRepository.recordSpeakerBoost.first()
                if (shouldBoostSpeaker) {
                    audioManager?.isSpeakerphoneOn = true
                }
            }

            val engine = capabilityChecker.getRecommendedEngine()
            val audioSource = if (engine == RecorderEngineType.SYSTEM_VOICE_CALL) {
                MediaRecorder.AudioSource.VOICE_CALL
            } else {
                MediaRecorder.AudioSource.MIC
            }

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(audioSource)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(recordingFile?.absolutePath)
                prepare()
                start()
            }

            recordStartTime = System.currentTimeMillis()
            callManager.setRecording(true)

            startForeground(Constants.NOTIFICATION_ID_RECORDER, buildRecordingNotification())
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }

        // Restore speakerphone state
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.isSpeakerphoneOn = previousSpeakerState

        callManager.setRecording(false)

        val durationSec = if (recordStartTime > 0) (System.currentTimeMillis() - recordStartTime) / 1000 else 0
        val recordedFile = recordingFile

        if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
            serviceScope.launch {
                recordingRepository.saveRecording(
                    filePath = recordedFile.absolutePath,
                    contactName = currentName,
                    phoneNumber = currentNumber,
                    durationSeconds = durationSec,
                    simLabel = "SIM 1",
                    sizeBytes = recordedFile.length()
                )
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildRecordingNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DialerApplication.CHANNEL_RECORDER)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Recording Call...")
            .setContentText("Recording conversation with $currentName")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
}
