package com.opendialer.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.opendialer.app.core.designsystem.theme.OpenDialerTheme
import com.opendialer.app.core.designsystem.theme.ThemeId
import com.opendialer.app.core.telephony.DialerCallState
import com.opendialer.app.data.repository.SettingsRepository
import com.opendialer.app.ui.navigation.AppNavigation
import com.opendialer.app.ui.screens.incall.InCallScreen
import com.opendialer.app.ui.screens.incall.InCallViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val inCallViewModel: InCallViewModel by viewModels()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        setContent {
            val selectedTheme by settingsRepository.selectedTheme.collectAsState(initial = ThemeId.AURA_MODERN)
            val isDark by settingsRepository.darkMode.collectAsState(initial = isSystemInDarkTheme())
            val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = false)
            val callState by inCallViewModel.callState.collectAsState()

            OpenDialerTheme(
                themeId = selectedTheme,
                darkTheme = isDark,
                dynamicColor = dynamicColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (callState.state == DialerCallState.ACTIVE || callState.state == DialerCallState.OUTGOING || callState.state == DialerCallState.ON_HOLD) {
                        InCallScreen(viewModel = inCallViewModel)
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(android.Manifest.permission.READ_PHONE_NUMBERS)
        }

        permissionsLauncher.launch(permissions.toTypedArray())
    }
}
