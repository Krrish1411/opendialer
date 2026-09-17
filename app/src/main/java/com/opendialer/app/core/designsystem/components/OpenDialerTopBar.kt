package com.opendialer.app.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.designsystem.theme.LocalThemePack
import com.opendialer.app.core.designsystem.theme.ThemeId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenDialerTopBar(
    title: String,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecordings: () -> Unit,
    onNavigateToBlockedNumbers: (() -> Unit)? = null,
    onNavigateToSpeedDial: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themePack = LocalThemePack.current
    var menuExpanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    val isOneUi = themePack.id == ThemeId.ONE_UI

    TopAppBar(
        title = {
            if (isSearching && onSearchQueryChange != null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search name or number...", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            onSearchQueryChange("")
                            isSearching = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                )
            } else {
                Text(
                    text = title,
                    fontWeight = if (isOneUi) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = if (isOneUi) 26.sp else 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            if (!isSearching && onSearchQueryChange != null) {
                IconButton(onClick = { isSearching = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Call Recordings") },
                    leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onNavigateToRecordings()
                    }
                )

                if (onNavigateToSpeedDial != null) {
                    DropdownMenuItem(
                        text = { Text("Speed Dial") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onNavigateToSpeedDial()
                        }
                    )
                }

                if (onNavigateToBlockedNumbers != null) {
                    DropdownMenuItem(
                        text = { Text("Blocked Numbers") },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onNavigateToBlockedNumbers()
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Settings") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onNavigateToSettings()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}
