package com.nkwabyte.medilert.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Brand / status palette — static, never change with theme ─────────────────
val PrimaryGreen    = Color(0xFF006B3F)
val DarkGreen       = Color(0xFF0D4E48)
val MediumGreen     = Color(0xFF0A8A51)
val LightGreen      = Color(0xFF44B551)

val GhanaRed        = Color(0xFFCE1126)
val GhanaRedDark    = Color(0xFFA81010)
val GhanaRedLight   = Color(0xFFE53935)

val GhanaYellow     = Color(0xFFFCD116)
val GhanaYellowDark = Color(0xFFE5BD14)
val GhanaYellowMedium = Color(0xFFFDBB11)

val StatusTaken     = Color(0xFF006B3F)
val StatusMissed    = Color(0xFFCE1126)
val StatusUpcoming  = Color(0xFF0DC0EC)

val TabActive       = Color(0xFF006B3F)
val TabInactive     = Color(0xFFADB1AD)

val Overlay         = Color(0x66000000)
val OverlayLight    = Color(0x1A000000)

// ── Raw light values (used by MedilertColors and LightColorScheme) ────────────
internal val LightBackground    = Color(0xFFFDFDFD)
internal val LightSurface       = Color(0xFFFFFFFF)
internal val LightSurfaceVar    = Color(0xFFF8F9FA)
internal val LightTextPrimary   = Color(0xFF000000)
internal val LightTextSecondary = Color(0xFF6B7280)
internal val LightTextTertiary  = Color(0xFFADB5BD)
internal val LightTextHint      = Color(0xFFD1D5DB)
internal val LightBorderLight   = Color(0xFFF3F4F6)
internal val LightBorderMedium  = Color(0xFFE5E7EB)
internal val LightDivider       = Color(0xFFE4E4E4)

// ── Raw dark values (used by MedilertColors and DarkColorScheme) ──────────────
val DarkBackground  = Color(0xFF0A120A)
val DarkSurface     = Color(0xFF131D13)
val DarkSurfaceVar  = Color(0xFF1A271A)
val DarkTextPrimary = Color(0xFFE6EEE6)
val DarkTextSecond  = Color(0xFF8A9E8A)
val DarkTextTertiary = Color(0xFF5A7A5A)
val DarkTextHint    = Color(0xFF3A553A)
val DarkBorderLight = Color(0xFF1E2E1E)
val DarkBorderMed   = Color(0xFF263826)
val DarkDivider     = Color(0xFF1E2E1E)

// ── MedilertColors — carries the current theme's token set ───────────────────
data class MedilertColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textHint: Color,
    val borderLight: Color,
    val borderMedium: Color,
    val divider: Color
)

internal fun lightMedilertColors() = MedilertColors(
    background    = LightBackground,
    surface       = LightSurface,
    surfaceVariant = LightSurfaceVar,
    textPrimary   = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary  = LightTextTertiary,
    textHint      = LightTextHint,
    borderLight   = LightBorderLight,
    borderMedium  = LightBorderMedium,
    divider       = LightDivider
)

internal fun darkMedilertColors() = MedilertColors(
    background    = DarkBackground,
    surface       = DarkSurface,
    surfaceVariant = DarkSurfaceVar,
    textPrimary   = DarkTextPrimary,
    textSecondary = DarkTextSecond,
    textTertiary  = DarkTextTertiary,
    textHint      = DarkTextHint,
    borderLight   = DarkBorderLight,
    borderMedium  = DarkBorderMed,
    divider       = DarkDivider
)

internal val LocalMedilertColors = compositionLocalOf { lightMedilertColors() }

// ── Adaptive color tokens — composable, react to light/dark mode ──────────────
// All screens import these by name; no screen changes needed.
val Background: Color    @Composable get() = LocalMedilertColors.current.background
val Surface: Color       @Composable get() = LocalMedilertColors.current.surface
val SurfaceVariant: Color @Composable get() = LocalMedilertColors.current.surfaceVariant
val TextPrimary: Color   @Composable get() = LocalMedilertColors.current.textPrimary
val TextSecondary: Color @Composable get() = LocalMedilertColors.current.textSecondary
val TextTertiary: Color  @Composable get() = LocalMedilertColors.current.textTertiary
val TextHint: Color      @Composable get() = LocalMedilertColors.current.textHint
val BorderLight: Color   @Composable get() = LocalMedilertColors.current.borderLight
val BorderMedium: Color  @Composable get() = LocalMedilertColors.current.borderMedium
val Divider: Color       @Composable get() = LocalMedilertColors.current.divider
