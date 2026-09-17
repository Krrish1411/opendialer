package com.opendialer.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.opendialer.app.core.designsystem.components.OpenDialerTopBar
import com.opendialer.app.ui.permissions.PermissionUtils
import com.opendialer.app.ui.screens.contacts.ContactsScreen
import com.opendialer.app.ui.screens.contacts.ContactsViewModel
import com.opendialer.app.ui.screens.favorites.FavoritesScreen
import com.opendialer.app.ui.screens.favorites.FavoritesViewModel
import com.opendialer.app.ui.screens.keypad.KeypadScreen
import com.opendialer.app.ui.screens.keypad.KeypadViewModel
import com.opendialer.app.ui.screens.recents.RecentsScreen
import com.opendialer.app.ui.screens.recents.RecentsViewModel
import com.opendialer.app.ui.screens.recordings.RecordingsScreen
import com.opendialer.app.ui.screens.recordings.RecordingsViewModel
import com.opendialer.app.ui.screens.settings.BlockedNumbersScreen
import com.opendialer.app.ui.screens.settings.CallForwardingScreen
import com.opendialer.app.ui.screens.settings.CheckUpdateScreen
import com.opendialer.app.ui.screens.settings.DndSettingsScreen
import com.opendialer.app.ui.screens.settings.DualSimSettingsScreen
import com.opendialer.app.ui.screens.settings.RecordingSettingsScreen
import com.opendialer.app.ui.screens.settings.SettingsScreen
import com.opendialer.app.ui.screens.settings.SettingsViewModel
import com.opendialer.app.ui.screens.settings.SoundsSettingsScreen
import com.opendialer.app.ui.screens.settings.ThemeSettingsScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    var isDefaultDialer by remember { mutableStateOf(PermissionUtils.isDefaultDialer(context)) }

    val tabs = listOf(
        MainTab.KEYPAD,
        MainTab.RECENTS,
        MainTab.CONTACTS,
        MainTab.FAVORITES
    )

    val isTopLevelDestination = tabs.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isTopLevelDestination) {
                val title = when (currentRoute) {
                    MainTab.KEYPAD.route -> "Phone"
                    MainTab.RECENTS.route -> "Recents"
                    MainTab.CONTACTS.route -> "Contacts"
                    MainTab.FAVORITES.route -> "Favorites"
                    else -> "OpenDialer"
                }
                OpenDialerTopBar(
                    title = title,
                    onNavigateToSettings = { navController.navigate("tab_settings") },
                    onNavigateToRecordings = { navController.navigate("tab_recordings") },
                    onNavigateToSpeedDial = { navController.navigate(MainTab.FAVORITES.route) }
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = MainTab.KEYPAD.route
            ) {
                composable(MainTab.KEYPAD.route) {
                    val viewModel = hiltViewModel<KeypadViewModel>()
                    KeypadScreen(viewModel = viewModel)
                }
                composable(MainTab.RECENTS.route) {
                    val viewModel = hiltViewModel<RecentsViewModel>()
                    RecentsScreen(viewModel = viewModel)
                }
                composable(MainTab.CONTACTS.route) {
                    val viewModel = hiltViewModel<ContactsViewModel>()
                    ContactsScreen(viewModel = viewModel)
                }
                composable(MainTab.FAVORITES.route) {
                    val viewModel = hiltViewModel<FavoritesViewModel>()
                    FavoritesScreen(viewModel = viewModel)
                }

                // Top-Level menu destinations
                composable("tab_recordings") {
                    val viewModel = hiltViewModel<RecordingsViewModel>()
                    RecordingsScreen(viewModel = viewModel)
                }
                composable("tab_settings") {
                    val settingsVm = hiltViewModel<SettingsViewModel>()
                    SettingsScreen(
                        onNavigateToThemes = { navController.navigate("settings_theme") },
                        onNavigateToDualSim = { navController.navigate("settings_dualsim") },
                        onNavigateToSounds = { navController.navigate("settings_sounds") },
                        onNavigateToBlockedNumbers = { navController.navigate("settings_blocked") },
                        onNavigateToRecording = { navController.navigate("settings_recording") },
                        onNavigateToForwarding = { navController.navigate("settings_forwarding") },
                        onNavigateToDnd = { navController.navigate("settings_dnd") },
                        onNavigateToUpdates = { navController.navigate("settings_updates") }
                    )
                }

                composable("settings_sounds") {
                    SoundsSettingsScreen()
                }
                composable("settings_blocked") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    BlockedNumbersScreen(viewModel = viewModel)
                }

                // Sub-Settings Routes
                composable("settings_theme") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    ThemeSettingsScreen(viewModel = viewModel)
                }
                composable("settings_dualsim") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    DualSimSettingsScreen(viewModel = viewModel)
                }
                composable("settings_recording") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    RecordingSettingsScreen(viewModel = viewModel)
                }
                composable("settings_forwarding") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    CallForwardingScreen(viewModel = viewModel)
                }
                composable("settings_dnd") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    DndSettingsScreen(viewModel = viewModel)
                }
                composable("settings_updates") {
                    val viewModel = hiltViewModel<SettingsViewModel>()
                    CheckUpdateScreen(viewModel = viewModel)
                }
            }
        }
    }
}
