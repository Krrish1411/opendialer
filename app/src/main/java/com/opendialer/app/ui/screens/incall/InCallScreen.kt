package com.opendialer.app.ui.screens.incall

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.common.DateTimeUtils
import com.opendialer.app.core.designsystem.animation.CallAnimationStyle
import com.opendialer.app.core.designsystem.animation.CallBackground
import com.opendialer.app.core.designsystem.components.ContactAvatar
import com.opendialer.app.core.designsystem.components.SimBadge
import com.opendialer.app.core.designsystem.components.WaveformVisualizer
import com.opendialer.app.core.designsystem.theme.LocalThemePack
import com.opendialer.app.core.designsystem.theme.ThemeId
import com.opendialer.app.core.telephony.DialerCallState
import com.opendialer.app.ui.screens.keypad.DialpadGrid

@Composable
fun InCallScreen(
    viewModel: InCallViewModel,
    modifier: Modifier = Modifier
) {
    val callState by viewModel.callState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val themePack = LocalThemePack.current

    var showDtmfKeypad by remember { mutableStateOf(false) }

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

    Box(modifier = modifier.fillMaxSize()) {
        // Lightweight Animated Call Background
        CallBackground(style = CallAnimationStyle.AURA_PARTICLES)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Call Waiting Banner (If second call is incoming while active)
            if (callState.waitingCallNumber.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Incoming Call Waiting",
                            fontSize = 12.sp,
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = callState.waitingCallName.ifEmpty { callState.waitingCallNumber },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { viewModel.holdAndAnswer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Hold & Answer", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.endAndAnswer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("End & Answer", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.declineSecondCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Decline", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Secondary Call Status (If call on hold exists)
            if (callState.hasSecondaryCall) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334155).copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "On Hold: ${callState.secondaryDisplayName}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = callState.secondaryPhoneNumber,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Row {
                            AssistChip(
                                onClick = { viewModel.swap() },
                                leadingIcon = { Icon(Icons.Default.SwapCalls, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text("Swap", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF475569))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AssistChip(
                                onClick = { viewModel.mergeConference() },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text("Merge", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF475569))
                            )
                        }
                    }
                }
            }

            // 3. Main Caller Info Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
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
                        size = if (showDtmfKeypad) 64.dp else 104.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callState.displayName.ifEmpty { callState.phoneNumber },
                    fontSize = if (showDtmfKeypad) 22.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (callState.displayName != callState.phoneNumber && callState.phoneNumber.isNotBlank()) {
                    Text(
                        text = callState.phoneNumber,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                    if (callState.isVideoCall) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "• ViLTE HD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4)
                        )
                    }
                }
            }

            // 4. Middle Section: In-Call Controls OR DTMF Keypad
            if (callState.state == DialerCallState.ACTIVE || callState.state == DialerCallState.ON_HOLD) {
                if (showDtmfKeypad) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DialpadGrid(
                            onDigitClick = { digit ->
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.playDtmf(digit)
                            },
                            onDigitLongClick = {}
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showDtmfKeypad = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                        ) {
                            Text("Hide Keypad")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WaveformVisualizer(
                            isRecording = callState.isRecording,
                            barColor = if (callState.isRecording) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // In-Call Controls Row 1
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
                                isActive = showDtmfKeypad,
                                onClick = { showDtmfKeypad = true }
                            )
                            InCallControlButton(
                                icon = Icons.Default.VolumeUp,
                                label = "Speaker",
                                isActive = callState.isSpeakerOn,
                                onClick = { viewModel.toggleSpeaker() }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // In-Call Controls Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallControlButton(
                                icon = if (callState.isHeld) Icons.Default.PlayArrow else Icons.Default.Pause,
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
                            if (callState.isVideoCapable) {
                                InCallControlButton(
                                    icon = Icons.Default.Videocam,
                                    label = "Video",
                                    isActive = callState.isVideoCall,
                                    onClick = { /* ViLTE switch */ }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Bottom Section: Answer / Reject / Disconnect
            if (callState.state == DialerCallState.INCOMING) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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

                    // Quick SMS Reject
                    AssistChip(
                        onClick = { viewModel.reject(rejectWithMessage = true, text = "Can't talk right now. I'll call you back.") },
                        label = { Text("Reply with SMS", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
                    )
                }
            } else {
                // End Call Button (Red)
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
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
                .background(if (isActive) activeColor else Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
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
