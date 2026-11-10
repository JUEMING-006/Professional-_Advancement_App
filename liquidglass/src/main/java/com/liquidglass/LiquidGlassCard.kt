package com.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

/**
 * A liquid-glass card / content container.
 *
 * The optical defaults follow the upstream scroll container
 * (`vibrancy(); lens(16.dp, 32.dp)`,
 *  `catalog/.../destinations/ScrollContainerContent.kt:42-43`) with the corner
 * radius of 32dp from the same file (:40).
 *
 * The blur radius is NOT specified upstream for this component, so it is an
 * explicit parameter (default: the kit's standard 8dp from
 * `LiquidBottomTabs.kt:171`) rather than an invented constant.
 */
@Composable
fun LiquidGlassCard(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    cornerRadius: Dp = liquidGlassTokens().cardCornerRadius,
    blurRadius: Dp = liquidGlassTokens().barBlurRadius,
    refractionHeight: Dp = 16.dp,
    refractionAmount: Dp = 32.dp,
    surfaceTint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = liquidGlassIsDarkTheme()
    val tokens = liquidGlassTokens()
    val tint = surfaceTint ?: if (isDark) tokens.containerTintDark else tokens.containerTintLight

    Column(
        modifier.liquidGlass(
            backdrop = backdrop,
            shape = RoundedCornerShape(cornerRadius),
            optics = LiquidGlassOptics(
                blurRadius = blurRadius,
                refractionHeight = refractionHeight,
                refractionAmount = refractionAmount,
                surfaceTint = tint
            ),
            capability = capability
        ).padding(contentPadding)
    ) {
        content()
    }
}
