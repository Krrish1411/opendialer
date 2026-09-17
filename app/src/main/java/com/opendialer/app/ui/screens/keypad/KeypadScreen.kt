package com.opendialer.app.ui.screens.keypad

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.core.designsystem.components.ContactAvatar
import com.opendialer.app.core.designsystem.theme.LocalThemePack
import com.opendialer.app.core.designsystem.theme.ThemeId
import com.opendialer.app.core.telephony.SocialActionHelper
import com.opendialer.app.data.model.ContactUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(
    viewModel: KeypadViewModel,
    modifier: Modifier = Modifier
) {
    val inputNumber by viewModel.inputNumber.collectAsState()
    val matchedContacts by viewModel.matchedContacts.collectAsState()
    val recentCalls by viewModel.recentCalls.collectAsState()
    val activeSims by viewModel.activeSims.collectAsState()
    val selectedSimIndex by viewModel.selectedSimIndex.collectAsState()

    val context = LocalContext.current
    val view = LocalView.current
    val themePack = LocalThemePack.current
    val isOneUi = themePack.id == ThemeId.ONE_UI
    val isIos = themePack.id == ThemeId.IOS

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Live Matching Contacts OR Recent Calls (Never a blank void)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            if (inputNumber.isNotEmpty()) {
                if (matchedContacts.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(matchedContacts) { contact ->
                            MatchingContactCard(
                                contact = contact,
                                onCall = { viewModel.placeCall(context, contact.phoneNumber) },
                                onWhatsApp = { SocialActionHelper.openWhatsAppChat(context, contact.phoneNumber) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching contacts found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // When dialer is empty: show quick recent contacts
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Recent Calls",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentCalls) { call ->
                            RecentCallQuickRow(
                                name = call.contactName?.ifEmpty { call.number } ?: call.number,
                                number = call.number,
                                onClick = { viewModel.placeCall(context, call.number) }
                            )
                        }
                    }
                }
            }
        }

        // Middle Section: Dialed Number Display + Add Contact
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (inputNumber.isNotEmpty()) PhoneNumberUtils.format(inputNumber) else " ",
                    fontSize = if (inputNumber.length > 12) 28.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (inputNumber.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .combinedClickable(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.deleteLastDigit()
                                },
                                onLongClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    viewModel.clear()
                                }
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (inputNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = { viewModel.openCreateContact(context) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Contact",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Add to Contacts", fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Dual SIM Selector Pills (if not in One UI dual-button mode)
        if (!isOneUi && activeSims.size > 1) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                activeSims.forEachIndexed { index, sim ->
                    val isSelected = index == selectedSimIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewModel.selectSim(index) }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${sim.displayName} (${sim.carrierName})",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Dialpad Grid (1-9, *, 0, #)
        DialpadGrid(
            onDigitClick = { digit ->
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.appendDigit(digit)
            },
            onDigitLongClick = { digit ->
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                when (digit) {
                    '0' -> viewModel.appendDigit('+')
                    '1' -> viewModel.handleSpeedDial(1, context)
                    in '2'..'9' -> viewModel.handleSpeedDial(digit - '0', context)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Call Action Buttons (Adapts to Samsung One UI vs standard)
        if (isOneUi && activeSims.size > 1) {
            // Samsung One UI style: Side-by-Side SIM 1 and SIM 2 Call Buttons!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activeSims.take(2).forEachIndexed { idx, sim ->
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.placeCall(context, simSlotIndex = sim.slotIndex)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (idx == 0) Color(0xFF16A34A) else Color(0xFF2563EB)
                        )
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sim.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // Standard / iOS / Pixel / Aura Call Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ViLTE Carrier Video Call Button (if typed digits)
                if (inputNumber.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.placeCarrierVideoCall(context)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Carrier Video Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }

                // Primary Voice Call Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.placeCall(context)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MatchingContactCard(
    contact: ContactUiModel,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCall() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = contact.phoneNumber,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onWhatsApp) {
                Text("💬", fontSize = 18.sp)
            }
            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF16A34A)
                )
            }
        }
    }
}

@Composable
fun RecentCallQuickRow(
    name: String,
    number: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(name = name, photoUri = null, size = 32.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = number,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DialpadGrid(
    onDigitClick: (Char) -> Unit,
    onDigitLongClick: (Char) -> Unit
) {
    val rows = listOf(
        listOf(DialpadKey('1', ""), DialpadKey('2', "ABC"), DialpadKey('3', "DEF")),
        listOf(DialpadKey('4', "GHI"), DialpadKey('5', "JKL"), DialpadKey('6', "MNO")),
        listOf(DialpadKey('7', "PQRS"), DialpadKey('8', "TUV"), DialpadKey('9', "WXYZ")),
        listOf(DialpadKey('*', ""), DialpadKey('0', "+"), DialpadKey('#', ""))
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    DialpadButton(
                        digit = key.digit,
                        subText = key.subText,
                        onClick = { onDigitClick(key.digit) },
                        onLongClick = { onDigitLongClick(key.digit) }
                    )
                }
            }
        }
    }
}

data class DialpadKey(val digit: Char, val subText: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadButton(
    digit: Char,
    subText: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val themePack = LocalThemePack.current
    val isIos = themePack.id == ThemeId.IOS

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                if (isIos) Color(0xFFE5E5EA).copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
