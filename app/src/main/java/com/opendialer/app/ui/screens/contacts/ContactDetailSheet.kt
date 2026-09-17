package com.opendialer.app.ui.screens.contacts

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.designsystem.components.ContactAvatar
import com.opendialer.app.core.telephony.SocialActionHelper
import com.opendialer.app.data.model.ContactUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailSheet(
    contact: ContactUiModel,
    onDismiss: () -> Unit,
    onCallVoice: (slotIndex: Int?) -> Unit,
    onCallCarrierVideo: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBlockContact: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Ringtone picker launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val ringtoneUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (ringtoneUri != null) {
                try {
                    val values = ContentValues().apply {
                        put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString())
                    }
                    context.contentResolver.update(
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id),
                        values, null, null
                    )
                    Toast.makeText(context, "Custom ringtone set!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save ringtone", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Contact Avatar & Header
            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = contact.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = contact.phoneNumber,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Grid: Voice, ViLTE Video, WhatsApp Voice, WhatsApp Video, SMS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ContactActionButton(
                    icon = Icons.Default.Call,
                    label = "Call",
                    color = Color(0xFF16A34A),
                    onClick = {
                        onDismiss()
                        onCallVoice(null)
                    }
                )

                ContactActionButton(
                    icon = Icons.Default.Videocam,
                    label = "ViLTE",
                    color = Color(0xFF06B6D4),
                    onClick = {
                        onDismiss()
                        onCallCarrierVideo()
                    }
                )

                ContactActionButton(
                    icon = Icons.Default.Call,
                    label = "WA Call",
                    color = Color(0xFF25D366),
                    onClick = {
                        onDismiss()
                        SocialActionHelper.startWhatsAppVoiceCall(context, contact.phoneNumber)
                    }
                )

                ContactActionButton(
                    icon = Icons.Default.Videocam,
                    label = "WA Video",
                    color = Color(0xFF25D366),
                    onClick = {
                        onDismiss()
                        SocialActionHelper.startWhatsAppVideoCall(context, contact.phoneNumber)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        SocialActionHelper.openWhatsAppChat(context, contact.phoneNumber)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("WhatsApp Chat")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        SocialActionHelper.openTelegramChat(context, contact.phoneNumber)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Telegram")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        SocialActionHelper.sendSms(context, contact.phoneNumber)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("SMS")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Contact Management Options
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // 1. Edit Contact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onDismiss()
                                try {
                                    val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id)
                                    val intent = Intent(Intent.ACTION_EDIT, uri).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open contact editor", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Edit Contact Details", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }

                    // 2. Custom Ringtone for this person
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                }
                                ringtonePickerLauncher.launch(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Custom Ringtone for ${contact.displayName.split(" ").first()}", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }

                    // 3. Favorite Star Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleFavorite() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (contact.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (contact.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }

                    // 4. Block Number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onDismiss()
                                onBlockContact()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Block Number", color = Color(0xFFDC2626), fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ContactActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
