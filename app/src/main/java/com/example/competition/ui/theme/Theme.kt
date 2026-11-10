package com.example.competition.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.liquidglass.LiquidGlassCapability
import com.liquidglass.LiquidGlassTheme

/**
 * 应用主题。
 *
 * 两层结构：Material3 负责色彩与字体语义，LiquidGlassTheme 负责玻璃光学效果。
 * 液态玻璃默认开启全档（模糊 + 折射 + 色散 + 高光 + 内阴影），
 * 引擎内建 API 门控：API 33+ 全效果，31–32 仅模糊，24–30 仅表面着色，不会崩。
 */

/** 液态玻璃档位。生产用 FULL；调试面板可切到 BLUR_ONLY / FALLBACK 对比效果。 */
var liquidGlassCapability: LiquidGlassCapability = LiquidGlassCapability.FULL

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceBackground,
    primaryContainer = BrandSurfaceTint,
    onPrimaryContainer = TextPrimary,
    secondary = TextSecondary,
    onSecondary = SurfaceBackground,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = BrandSurfaceTint,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = StatusDanger,
)

private val DarkColors = darkColorScheme(
    primary = BrandSurfaceTint,
    onPrimary = TextPrimary,
    primaryContainer = BrandPrimary,
    onPrimaryContainer = SurfaceBackground,
    secondary = TextMuted,
    onSecondary = TextPrimary,
    background = Color(0xFF121614),
    onBackground = TextPrimary,
    surface = Color(0xFF1A201D),
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF232B27),
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF323B36),
    error = Color(0xFFF2B8B5),
)

@Composable
fun CompetitionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // 玻璃主题包在内层，Material 主题在外层：Material 提供语义色与字体，
    // 玻璃通过 CompositionLocal 下发光学参数，两层各司其职。
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
    ) {
        LiquidGlassTheme(
            capability = liquidGlassCapability,
            isDarkTheme = darkTheme,
            content = content,
        )
    }
}
