package com.opendialer.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    KEYPAD("Keypad", Icons.Default.Dialpad, "tab_keypad"),
    RECENTS("Recents", Icons.Default.History, "tab_recents"),
    CONTACTS("Contacts", Icons.Default.Contacts, "tab_contacts"),
    FAVORITES("Favorites", Icons.Default.Star, "tab_favorites")
}
