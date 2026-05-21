package com.nkwabyte.medilert.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

private val MedilertColorScheme = lightColorScheme(
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

@Composable
internal expect fun loadPoppins(): FontFamily

@Composable
fun MedilertTheme(content: @Composable () -> Unit) {
    poppinsFont = loadPoppins()
    MaterialTheme(
        colorScheme = MedilertColorScheme,
        typography = createTypography(poppinsFont),
        content = content
    )
}
