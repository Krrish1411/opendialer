package com.opendialer.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

object ThemeEngine {

    val availableThemes = listOf(
        ThemePack(
            id = ThemeId.AURA_MODERN,
            name = "Aura Nebula",
            description = "Signature sticky modern UI with electric violet and cyan accents",
            lightColorScheme = AuraLightColors,
            darkColorScheme = AuraDarkColors,
            shapes = AuraShapes,
            cornerRadius = 20.dp,
            callScreenStyle = CallScreenStyle.AURA_CARD,
            isGlassmorphic = true
        ),
        ThemePack(
            id = ThemeId.PIXEL,
            name = "Pixel Material",
            description = "Google Material You clean layout with subtle dynamic accents",
            lightColorScheme = PixelLightColors,
            darkColorScheme = PixelDarkColors,
            shapes = Shapes(),
            cornerRadius = 16.dp,
            callScreenStyle = CallScreenStyle.GOOGLE_CENTERED
        ),
        ThemePack(
            id = ThemeId.ONE_UI,
            name = "OneUI Modern",
            description = "Samsung One UI-inspired layout with generous reachability curves",
            lightColorScheme = OneUiLightColors,
            darkColorScheme = OneUiDarkColors,
            shapes = OneUiShapes,
            cornerRadius = 24.dp,
            callScreenStyle = CallScreenStyle.SAMSUNG_ONEUI
        ),
        ThemePack(
            id = ThemeId.IOS,
            name = "Cupertino Glass",
            description = "iOS-inspired clean cards, circular buttons and grouped styling",
            lightColorScheme = IosLightColors,
            darkColorScheme = IosDarkColors,
            shapes = IosShapes,
            cornerRadius = 14.dp,
            callScreenStyle = CallScreenStyle.IOS_CIRCULAR
        ),
        ThemePack(
            id = ThemeId.CLASSIC,
            name = "Android Classic",
            description = "Compact teal and high-contrast dense information design",
            lightColorScheme = ClassicLightColors,
            darkColorScheme = ClassicDarkColors,
            shapes = ClassicShapes,
            cornerRadius = 8.dp,
            callScreenStyle = CallScreenStyle.GOOGLE_CENTERED
        ),
        ThemePack(
            id = ThemeId.AMOLED_DARK,
            name = "AMOLED Pitch Black",
            description = "Deep black #000000 with neon electric cyan for ultimate battery life",
            lightColorScheme = AmoledDarkColors,
            darkColorScheme = AmoledDarkColors,
            shapes = AuraShapes,
            cornerRadius = 18.dp,
            callScreenStyle = CallScreenStyle.AURA_CARD
        )
    )

    fun getTheme(id: ThemeId): ThemePack {
        return availableThemes.find { it.id == id } ?: availableThemes.first()
    }
}

val LocalThemePack = staticCompositionLocalOf {
    ThemeEngine.getTheme(ThemeId.AURA_MODERN)
}

@Composable
fun OpenDialerTheme(
    themeId: ThemeId = ThemeId.AURA_MODERN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themePack = ThemeEngine.getTheme(themeId)
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme || themeId == ThemeId.AMOLED_DARK -> themePack.darkColorScheme
        else -> themePack.lightColorScheme
    }

    CompositionLocalProvider(LocalThemePack provides themePack) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = themePack.shapes,
            typography = Typography(),
            content = content
        )
    }
}
