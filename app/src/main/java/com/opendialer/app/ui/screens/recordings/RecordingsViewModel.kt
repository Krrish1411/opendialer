package com.opendialer.app.ui.screens.recordings

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opendialer.app.data.local.entity.RecordingEntity
import com.opendialer.app.data.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PlayerState(
    val recording: RecordingEntity? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val totalDuration: Int = 0
)

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    private val recordingRepository: RecordingRepository
) : ViewModel() {

    val recordings: StateFlow<List<RecordingEntity>> = recordingRepository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    _playerState.value = _playerState.value.copy(currentPosition = player.currentPosition)
                    handler.postDelayed(this, 250)
                }
            }
        }
    }

    fun playRecording(recording: RecordingEntity) {
        stopPlayer()
        try {
            val file = File(recording.filePath)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    _playerState.value = _playerState.value.copy(isPlaying = false, currentPosition = 0)
                }
            }

            _playerState.value = PlayerState(
                recording = recording,
                isPlaying = true,
                currentPosition = 0,
                totalDuration = mediaPlayer?.duration ?: (recording.durationSeconds.toInt() * 1000)
            )

            handler.post(progressRunnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
            handler.removeCallbacks(progressRunnable)
        } else {
            player.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
            handler.post(progressRunnable)
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPosition = positionMs)
    }

    fun stopPlayer() {
        handler.removeCallbacks(progressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = PlayerState()
    }

    fun deleteRecording(recording: RecordingEntity) {
        if (_playerState.value.recording?.id == recording.id) {
            stopPlayer()
        }
        viewModelScope.launch {
            recordingRepository.deleteRecording(recording)
        }
    }

    fun shareRecording(context: Context, recording: RecordingEntity) {
        val file = File(recording.filePath)
        if (!file.exists()) return

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Call Recording"))
    }

    override fun onCleared() {
        stopPlayer()
        super.onCleared()
    }
}
