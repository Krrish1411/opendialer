package com.opendialer.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 1. AURA NEBULA MODERN (Flagship New Sticky Theme)
val AuraDarkColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),        // Electric Violet
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = Color(0xFF06B6D4),      // Magnetic Cyan
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = Color(0xFFF59E0B),       // Radiant Amber
    background = Color(0xFF0B0F19),     // Deep Midnight
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131B2E),        // Glassmorphic Surface
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444),
    onError = Color.White
)

val AuraLightColors = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = Color(0xFFD97706),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626),
    onError = Color.White
)

// 2. PIXEL THEME (Clean Google Material You)
val PixelDarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF0842A0),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD2E4FF),
    secondary = Color(0xFFBCC7DB),
    onSecondary = Color(0xFF263140),
    background = Color(0xFF1B1B1F),
    surface = Color(0xFF202124),
    surfaceVariant = Color(0xFF303030)
)

val PixelLightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF535F70),
    background = Color(0xFFFEF7FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE1E2EC)
)

// 3. SAMSUNG ONE UI THEME
val OneUiDarkColors = darkColorScheme(
    primary = Color(0xFF388E3C),
    onPrimary = Color.White,
    secondary = Color(0xFF1976D2),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C)
)

val OneUiLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    secondary = Color(0xFF1565C0),
    background = Color(0xFFF4F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8ECEF)
)

// 4. IOS THEME
val IosDarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    secondary = Color(0xFF30D158),
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF2C2C2E)
)

val IosLightColors = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    secondary = Color(0xFF34C759),
    background = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE5E5EA)
)

// 5. CLASSIC ANDROID THEME
val ClassicDarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF004D40),
    secondary = Color(0xFFFFB74D),
    background = Color(0xFF212121),
    surface = Color(0xFF2E2E2E),
    surfaceVariant = Color(0xFF424242)
)

val ClassicLightColors = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    secondary = Color(0xFFF57C00),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEEE)
)

// 6. AMOLED DARK THEME
val AmoledDarkColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF76FF03),
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF121212)
)

// Shape definitions per theme
val AuraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

val OneUiShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

val IosShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
)

val ClassicShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)
