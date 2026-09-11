package com.example.pepsi.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Brand palette: electric blue, red, white and black — no dynamic/wallpaper-based
// theming, so the app always reads as Pepsi regardless of device.
private val LightColors = lightColorScheme(
    primary = PepsiClassicBlue,
    onPrimary = PepsiWhite,
    primaryContainer = PepsiDarkBlue,
    onPrimaryContainer = PepsiWhite,
    secondary = PepsiRed,
    onSecondary = PepsiWhite,
    secondaryContainer = PepsiRed,
    onSecondaryContainer = PepsiWhite,
    background = PepsiSurfaceLight,
    onBackground = PepsiBlack,
    surface = PepsiWhite,
    onSurface = PepsiBlack,
    surfaceVariant = Color(0xFFE7ECF3),
    error = PepsiRed,
)

private val DarkColors = darkColorScheme(
    primary = PepsiClassicBlue,
    onPrimary = PepsiWhite,
    primaryContainer = PepsiDarkBlue,
    onPrimaryContainer = PepsiWhite,
    secondary = PepsiRed,
    onSecondary = PepsiWhite,
    secondaryContainer = PepsiRed,
    onSecondaryContainer = PepsiWhite,
    background = PepsiBlack,
    onBackground = PepsiWhite,
    surface = PepsiSurfaceDark,
    onSurface = PepsiWhite,
    surfaceVariant = Color(0xFF2A2A2A),
    error = PepsiRed,
)

@Composable
fun PepsiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PepsiTypography,
        content = content,
    )
}
