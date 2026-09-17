package com.opendialer.app.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.BuildConfig

@Composable
fun SettingsScreen(
    onNavigateToThemes: () -> Unit,
    onNavigateToDualSim: () -> Unit,
    onNavigateToRecording: () -> Unit,
    onNavigateToForwarding: () -> Unit,
    onNavigateToDnd: () -> Unit,
    onNavigateToUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Preferences",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsRowItem(
                    icon = Icons.Default.ColorLens,
                    title = "Theme & Appearance",
                    subtitle = "Aura Nebula, Pixel, OneUI, iOS, AMOLED Dark",
                    onClick = onNavigateToThemes
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRowItem(
                    icon = Icons.Default.SimCard,
                    title = "Dual SIM Settings",
                    subtitle = "Default voice SIM, carrier tags, and slot rules",
                    onClick = onNavigateToDualSim
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRowItem(
                    icon = Icons.Default.Mic,
                    title = "Call Recording & Consent",
                    subtitle = "Acoustic recording, speaker boost, and legal notice",
                    onClick = onNavigateToRecording
                )
            }
        }

        item {
            Text(
                text = "Carrier & Telephony",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsRowItem(
                    icon = Icons.Default.PhoneForwarded,
                    title = "Call Forwarding & Barring",
                    subtitle = "Forward busy/unanswered, call barring, call waiting",
                    onClick = onNavigateToForwarding
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRowItem(
                    icon = Icons.Default.DoNotDisturb,
                    title = "Do Not Disturb & VIP Mode",
                    subtitle = "Silence unknown numbers, allow starred favorites",
                    onClick = onNavigateToDnd
                )
            }
        }

        item {
            Text(
                text = "Maintenance & Info",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            SettingsCategoryCard {
                SettingsRowItem(
                    icon = Icons.Default.SystemUpdate,
                    title = "Check for Updates (OTA)",
                    subtitle = "GitHub Releases direct in-place update without uninstall",
                    onClick = onNavigateToUpdates
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRowItem(
                    icon = Icons.Default.Info,
                    title = "About OpenDialer",
                    subtitle = "Version 1.0.0 • Open-Source Personal Dialer",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryCard(content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
