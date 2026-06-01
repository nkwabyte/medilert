package com.nkwabyte.medilert.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalFontScale = compositionLocalOf { 1f }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Surface,
    primaryContainer = LightGreen,
    onPrimaryContainer = Surface,
    secondary = GhanaYellow,
    onSecondary = TextPrimary,
    secondaryContainer = GhanaYellowDark,
    onSecondaryContainer = TextPrimary,
    tertiary = GhanaRed,
    onTertiary = Surface,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = GhanaRed,
    onError = Surface,
    outline = BorderMedium,
    outlineVariant = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = DarkSurface,
    primaryContainer = DarkGreen,
    onPrimaryContainer = DarkTextPrimary,
    secondary = GhanaYellow,
    onSecondary = DarkBackground,
    secondaryContainer = GhanaYellowDark,
    onSecondaryContainer = DarkBackground,
    tertiary = GhanaRed,
    onTertiary = DarkSurface,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVar,
    onSurfaceVariant = DarkTextSecond,
    error = GhanaRed,
    onError = DarkSurface,
    outline = DarkBorderMed,
    outlineVariant = DarkBorderLight
)

@Composable
fun MedilertTheme(
    darkTheme: Boolean = false,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val currentDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalFontScale provides fontScale,
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
