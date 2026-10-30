package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = OnPrimaryGreenDark,
    primaryContainer = PrimaryGreenContainerDark,
    onPrimaryContainer = OnPrimaryGreenContainerDark,
    secondary = AccentGoldDark,
    onSecondary = OnGoldContainerDark,
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = AccentGoldDark,
    error = TamperRedDark,
    errorContainer = TamperRedContainerDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    onSurfaceVariant = TextLightSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryGreenContainer,
    onPrimaryContainer = OnPrimaryGreenContainer,
    secondary = AccentGold,
    onSecondary = OnGoldContainer,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    error = TamperRed,
    errorContainer = TamperRedContainer,
    background = ParchmentBgLight,
    surface = ParchmentSurfaceLight,
    surfaceVariant = ParchmentSurfaceVariantLight,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    onSurfaceVariant = TextDarkSecondary
)

@Composable
fun LandAuditTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded legal aesthetic consistent
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
