package com.opendialer.app.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SoundsSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dialpadTonesEnabled by remember { mutableStateOf(true) }
    var dialpadHapticsEnabled by remember { mutableStateOf(true) }
    var vibrateOnConnect by remember { mutableStateOf(true) }
    var vibrateOnDisconnect by remember { mutableStateOf(true) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Ringtones & Alerts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsRowItem(
                    icon = Icons.Default.MusicNote,
                    title = "Phone Ringtone",
                    subtitle = "Configure incoming call ringtone for SIM cards",
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        }
                        ringtonePickerLauncher.launch(intent)
                    }
                )
            }
        }

        item {
            Text(
                text = "Dialpad Feedback",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Dialpad Tones",
                    subtitle = "Play DTMF audible sound when tapping dialpad numbers",
                    checked = dialpadTonesEnabled,
                    onCheckedChange = { dialpadTonesEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Default.TouchApp,
                    title = "Dialpad Haptic Vibration",
                    subtitle = "Gentle haptic feedback when pressing keypad buttons",
                    checked = dialpadHapticsEnabled,
                    onCheckedChange = { dialpadHapticsEnabled = it }
                )
            }
        }

        item {
            Text(
                text = "Call Vibrations",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Vibrate on Call Answered",
                    subtitle = "Brief vibration pulse when caller picks up",
                    checked = vibrateOnConnect,
                    onCheckedChange = { vibrateOnConnect = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Vibrate on Call Ended",
                    subtitle = "Brief vibration pulse when call disconnects",
                    checked = vibrateOnDisconnect,
                    onCheckedChange = { vibrateOnDisconnect = it }
                )
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
