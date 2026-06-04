package com.nkwabyte.medilert.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density

val LocalFontScale = compositionLocalOf { 1f }

@Composable
internal expect fun loadPoppins(): FontFamily

private val LightColorScheme = lightColorScheme(
    primary               = PrimaryGreen,
    onPrimary             = LightSurface,
    primaryContainer      = LightGreen,
    onPrimaryContainer    = LightSurface,
    secondary             = GhanaYellow,
    onSecondary           = LightTextPrimary,
    secondaryContainer    = GhanaYellowDark,
    onSecondaryContainer  = LightTextPrimary,
    tertiary              = GhanaRed,
    onTertiary            = LightSurface,
    background            = LightBackground,
    onBackground          = LightTextPrimary,
    surface               = LightSurface,
    onSurface             = LightTextPrimary,
    surfaceVariant        = LightSurfaceVar,
    onSurfaceVariant      = LightTextSecondary,
    error                 = GhanaRed,
    onError               = LightSurface,
    outline               = LightBorderMedium,
    outlineVariant        = LightBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary               = PrimaryGreen,
    onPrimary             = DarkSurface,
    primaryContainer      = DarkGreen,
    onPrimaryContainer    = DarkTextPrimary,
    secondary             = GhanaYellow,
    onSecondary           = DarkBackground,
    secondaryContainer    = GhanaYellowDark,
    onSecondaryContainer  = DarkBackground,
    tertiary              = GhanaRed,
    onTertiary            = DarkSurface,
    background            = DarkBackground,
    onBackground          = DarkTextPrimary,
    surface               = DarkSurface,
    onSurface             = DarkTextPrimary,
    surfaceVariant        = DarkSurfaceVar,
    onSurfaceVariant      = DarkTextSecond,
    error                 = GhanaRed,
    onError               = DarkSurface,
    outline               = DarkBorderMed,
    outlineVariant        = DarkBorderLight
)

@Composable
fun MedilertTheme(
    darkTheme: Boolean = false,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val medilertColors = if (darkTheme) darkMedilertColors() else lightMedilertColors()
    val font = loadPoppins()
    poppinsFont = font
    val currentDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalFontScale provides fontScale,
        LocalMedilertColors provides medilertColors,
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = createTypography(font),
            content = content
        )
    }
}
