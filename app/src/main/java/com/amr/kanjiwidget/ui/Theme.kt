package com.amr.kanjiwidget.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Warm dark palette — not pitch black, cozy like a lantern-lit room
val WarmBg = Color(0xFF171310)
val WarmSurface = Color(0xFF221D18)
val WarmSurfaceHigh = Color(0xFF2C2620)
val WarmSurfaceHigher = Color(0xFF362F27)
val WarmCream = Color(0xFFF2EBE2)
val WarmMuted = Color(0xFFA89F94)
val WarmAccent = Color(0xFFE07856)
val WarmAccentSoft = Color(0xFFF2C094)
val WarmSage = Color(0xFF7FB5A0)
val WarmBorder = Color(0xFF3A332C)
val WarmGold = Color(0xFFD9A441)

private val WarmDarkColors = darkColorScheme(
    primary = WarmAccent,
    onPrimary = Color(0xFF2A1408),
    primaryContainer = Color(0xFF4A2E1C),
    onPrimaryContainer = WarmAccentSoft,
    secondary = WarmSage,
    onSecondary = Color(0xFF10231C),
    secondaryContainer = Color(0xFF2C3B34),
    onSecondaryContainer = Color(0xFFB8DCC9),
    tertiary = WarmGold,
    onTertiary = Color(0xFF2A1F08),
    tertiaryContainer = Color(0xFF453517),
    onTertiaryContainer = Color(0xFFF2D9A0),
    background = WarmBg,
    onBackground = WarmCream,
    surface = WarmSurface,
    onSurface = WarmCream,
    surfaceVariant = WarmSurfaceHigh,
    onSurfaceVariant = WarmMuted,
    surfaceContainer = WarmSurfaceHigh,
    surfaceContainerHigh = WarmSurfaceHigher,
    surfaceContainerHighest = Color(0xFF40382F),
    surfaceContainerLow = Color(0xFF1E1915),
    surfaceContainerLowest = Color(0xFF120F0C),
    outline = WarmBorder,
    outlineVariant = Color(0xFF4A4239),
    inverseSurface = WarmCream,
    inverseOnSurface = WarmBg,
    scrim = Color(0xFF000000),
    error = Color(0xFFE57373),
    onError = Color(0xFF3B1210),
    errorContainer = Color(0xFF5C2320),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val WarmTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold, color = WarmCream),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = WarmCream),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = WarmCream),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = WarmCream),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = WarmCream),
    bodyLarge = TextStyle(fontSize = 16.sp, color = WarmCream, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, color = WarmCream, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, color = WarmMuted, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WarmCream),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = WarmMuted),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WarmMuted)
)

@Composable
fun KanjiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WarmDarkColors,
        typography = WarmTypography,
        content = content
    )
}
