package com.opendialer.app.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ThemeId {
    AURA_MODERN, // Flagship Sticky Modern UI
    PIXEL,       // Material You Google Style
    ONE_UI,      // Samsung One UI Inspired
    IOS,         // iOS Inspired
    CLASSIC,     // Classic Android Compact
    AMOLED_DARK  // Pure Black AMOLED
}

enum class CallScreenStyle {
    AURA_CARD,
    GOOGLE_CENTERED,
    SAMSUNG_ONEUI,
    IOS_CIRCULAR
}

@Immutable
data class ThemePack(
    val id: ThemeId,
    val name: String,
    val description: String,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
    val shapes: Shapes,
    val cornerRadius: Dp = 16.dp,
    val callScreenStyle: CallScreenStyle = CallScreenStyle.AURA_CARD,
    val isGlassmorphic: Boolean = false,
    val accentHighlight: Color = Color(0xFF7C3AED)
)
