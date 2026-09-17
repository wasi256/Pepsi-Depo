package com.example.pepsi.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val PepsiLightColorScheme = lightColorScheme(
    primary = PepsiElectricBlue,
    onPrimary = CardLight,
    primaryContainer = PepsiDarkBlue,
    onPrimaryContainer = CardLight,
    secondary = PepsiRed,
    onSecondary = CardLight,
    background = SurfaceLight,
    surface = CardLight,
    onBackground = PepsiDarkBlue,
    onSurface = PepsiDarkBlue,
)

private val PepsiDarkColorScheme = darkColorScheme(
    primary = PepsiBlueLight,
    onPrimary = SurfaceDark,
    secondary = PepsiRed,
    onSecondary = SurfaceDark,
    background = SurfaceDark,
    surface = CardDark,
    onBackground = SurfaceLight,
    onSurface = SurfaceLight,
)

@Composable
fun PepsiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PepsiDarkColorScheme
        else -> PepsiLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PepsiTypography,
        content = content,
    )
}
