package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SignalCyan,
    onPrimary = SpaceNavyDark,
    primaryContainer = SignalCyanContainer,
    onPrimaryContainer = OnSignalCyanContainer,
    secondary = GoldLock,
    onSecondary = SpaceNavyDark,
    secondaryContainer = Color(0xFF4A3800),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = SignalGreen,
    onTertiary = SpaceNavyDark,
    background = SpaceNavy,
    onBackground = Color(0xFFEDF2F7),
    surface = SpaceNavySurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = SpaceNavySurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = SignalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC0E8FF),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary = Color(0xFF825500),
    onSecondary = Color.White,
    tertiary = Color(0xFF006C50),
    background = BackgroundLight,
    onBackground = Color(0xFF191C1E),
    surface = SurfaceLight,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF41484D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to high-contrast Dark theme for outdoors and satellite technician tools
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
