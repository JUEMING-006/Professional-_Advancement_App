package com.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

/**
 * A liquid-glass top bar (toolbar / status pill).
 *
 * The upstream library has no dedicated top bar, so this component reuses the
 * proven bottom-bar optics (`vibrancy(); blur(8.dp); lens(24.dp, 24.dp)` —
 * `LiquidBottomTabs.kt:169-173`) on a capsule shape. Every numeric value is
 * therefore traceable; the bar height is an explicit parameter.
 */
@Composable
fun LiquidGlassTopBar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    height: Dp = 56.dp,
    contentPadding: Dp = liquidGlassTokens().barPadding,
    content: @Composable RowScope.() -> Unit
) {
    val tokens = liquidGlassTokens()
    val isDark = liquidGlassIsDarkTheme()
    val tint = if (isDark) tokens.containerTintDark else tokens.containerTintLight

    Row(
        modifier
            .fillMaxWidth()
            .liquidGlass(
                backdrop = backdrop,
                shape = Capsule(),
                optics = LiquidGlassOptics(
                    blurRadius = tokens.barBlurRadius,
                    refractionHeight = tokens.barRefractionHeight,
                    refractionAmount = tokens.barRefractionAmount,
                    surfaceTint = tint
                ),
                capability = capability
            )
            .padding(horizontal = contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
