package com.opendialer.app.ui.screens.incall

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.common.DateTimeUtils
import com.opendialer.app.core.designsystem.components.ContactAvatar
import com.opendialer.app.core.designsystem.components.SimBadge
import com.opendialer.app.core.designsystem.components.WaveformVisualizer
import com.opendialer.app.core.telephony.DialerCallState

@Composable
fun InCallScreen(
    viewModel: InCallViewModel,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callState.collectAsState()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0B0F19) // Deep Midnight Navy
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            if (callState.state == DialerCallState.INCOMING) Modifier.scale(pulseScale)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    ContactAvatar(
                        name = callState.displayName.ifEmpty { "Unknown" },
                        photoUri = null,
                        size = 100.dp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callState.displayName.ifEmpty { callState.phoneNumber },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (callState.displayName != callState.phoneNumber && callState.phoneNumber.isNotBlank()) {
                    Text(
                        text = callState.phoneNumber,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SimBadge(
                        simLabel = "SIM ${callState.simSlotIndex + 1}",
                        slotIndex = callState.simSlotIndex
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = when (callState.state) {
                            DialerCallState.INCOMING -> "Incoming Call..."
                            DialerCallState.OUTGOING -> "Calling..."
                            DialerCallState.ACTIVE -> DateTimeUtils.formatDuration(callState.durationSeconds)
                            DialerCallState.ON_HOLD -> "Call on Hold"
                            DialerCallState.DISCONNECTED -> "Call Ended"
                            else -> ""
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (callState.state == DialerCallState.ACTIVE) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Middle Section: Waveform or In-Call Controls
            if (callState.state == DialerCallState.ACTIVE || callState.state == DialerCallState.ON_HOLD) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaveformVisualizer(
                        isRecording = callState.isRecording,
                        barColor = if (callState.isRecording) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // In-Call Controls Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallControlButton(
                            icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (callState.isMuted) "Unmute" else "Mute",
                            isActive = callState.isMuted,
                            onClick = { viewModel.toggleMute() }
                        )
                        InCallControlButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            isActive = false,
                            onClick = {}
                        )
                        InCallControlButton(
                            icon = Icons.Default.VolumeUp,
                            label = "Speaker",
                            isActive = callState.isSpeakerOn,
                            onClick = { viewModel.toggleSpeaker() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallControlButton(
                            icon = Icons.Default.Pause,
                            label = if (callState.isHeld) "Resume" else "Hold",
                            isActive = callState.isHeld,
                            onClick = { viewModel.toggleHold() }
                        )
                        InCallControlButton(
                            icon = Icons.Default.FiberManualRecord,
                            label = if (callState.isRecording) "Recording..." else "Record",
                            isActive = callState.isRecording,
                            activeColor = Color(0xFFEF4444),
                            onClick = { viewModel.toggleRecording(context) }
                        )
                    }
                }
            }

            // Bottom Section: Answer / Reject / Disconnect
            if (callState.state == DialerCallState.INCOMING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reject Button (Red)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626))
                            .clickable { viewModel.reject() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Reject Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Answer Button (Green)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                            .clickable { viewModel.answer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Answer Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                // End Call Button (Red)
                Box(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .clickable { viewModel.disconnect() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InCallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color(0xFF8B5CF6),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
