package com.opendialer.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    FAVORITES("Favorites", Icons.Default.Star, "tab_favorites"),
    RECENTS("Recents", Icons.Default.History, "tab_recents"),
    CONTACTS("Contacts", Icons.Default.Contacts, "tab_contacts"),
    KEYPAD("Keypad", Icons.Default.Dialpad, "tab_keypad"),
    RECORDINGS("Recordings", Icons.Default.Mic, "tab_recordings"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}
