package com.liquidglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/**
 * Optical configuration shared by every glass surface in the kit.
 *
 * Defaults are the upstream 1.0.6 values
 * (`analysis/upstream-defaults.tsv`):
 *  - blur radius 8dp          [LiquidBottomTabs.kt:171]
 *  - refraction height 24dp   [LiquidBottomTabs.kt:172]
 *  - refraction amount 24dp   [LiquidBottomTabs.kt:172]
 *  - highlight angle 45 deg   [HighlightStyle.kt:52]
 *  - highlight falloff 1      [HighlightStyle.kt:53]
 *  - highlight colour white @ 0.5 [HighlightStyle.kt:50]
 *  - highlight blend Plus     [HighlightStyle.kt:51]
 *  - shadow radius 24dp, black @ 0.1, offset radius/6 [Shadow.kt:15-17]
 *  - inner shadow radius 24dp, black @ 0.15          [InnerShadow.kt:18-20]
 */
@androidx.compose.runtime.Immutable
data class LiquidGlassOptics(
    val blurRadius: Dp = 8.dp,
    val refractionHeight: Dp = 24.dp,
    val refractionAmount: Dp = 24.dp,
    val depthEffect: Boolean = false,
    val chromaticAberration: Boolean = false,
    val highlightColor: Color = Color.White.copy(alpha = 0.5f),
    val highlightAngle: Float = 45f,
    val highlightFalloff: Float = 1f,
    val highlightWidth: Dp = 0.5.dp,
    val shadowRadius: Dp = 24.dp,
    val shadowColor: Color = Color.Black.copy(alpha = 0.1f),
    val innerShadowRadius: Dp = 24.dp,
    val innerShadowColor: Color = Color.Black.copy(alpha = 0.15f),
    /** Surface tint drawn on top of the glass. */
    val surfaceTint: Color? = null
) {
    companion object {
        @JvmField
        val Default = LiquidGlassOptics()
    }
}

/**
 * Wraps the receiver in a liquid-glass surface.
 *
 * Pipeline (order matters — the LAST effect in the DSL is the OUTERMOST layer,
 * see `analysis/upstream-spec.md` §2.4):
 * ```
 * vibrancy() -> blur(radius) -> lens(height, amount) -> surface tint
 * ```
 * followed by the outer shadow, the specular highlight and the inner shadow,
 * exactly like the upstream `drawBackdrop` modifier chain.
 *
 * Degradation is graceful by construction:
 *  - API < 31: [LiquidGlassCapability.FALLBACK] behaviour, the engine silently
 *    skips blur (engine gate: `effects/Blur.kt:14`).
 *  - API < 33: no lens/dispersion (engine gate: `effects/Lens.kt:22`).
 *  - `LiquidGlassCapability.FALLBACK`: nothing is applied, only the tint.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: Shape = RoundedCornerShape(32.dp),
    optics: LiquidGlassOptics = LiquidGlassOptics.Default,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current,
    isDarkTheme: Boolean = liquidGlassIsDarkTheme(),
    onDrawSurface: (androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit)? = null
): Modifier {
    val resolved = overrides.resolve(capability)
    if (!resolved.backdrop || capability == LiquidGlassCapability.FALLBACK) {
        // FALLBACK: translucent surface only, no RenderEffect at all.
        return if (optics.surfaceTint != null) {
            this.drawBehind { drawRect(optics.surfaceTint) }
        } else {
            this
        }
    }
    val tokens = LiquidGlassTokens.Default
    val tint = optics.surfaceTint
        ?: if (isDarkTheme) tokens.containerTintDark else tokens.containerTintLight

    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            if (resolved.vibrancy) vibrancy()
            if (resolved.blur) blur(optics.blurRadius.toPx())
            if (resolved.lens) {
                lens(
                    optics.refractionHeight.toPx(),
                    optics.refractionAmount.toPx(),
                    depthEffect = optics.depthEffect,
                    chromaticAberration = resolved.chromaticAberration
                )
            }
        },
        highlight = {
            Highlight(
                style = HighlightStyle.Default(
                    color = optics.highlightColor,
                    angle = optics.highlightAngle,
                    falloff = optics.highlightFalloff
                ),
                width = optics.highlightWidth
            )
        },
        shadow = {
            Shadow(
                radius = optics.shadowRadius,
                color = optics.shadowColor
            )
        },
        innerShadow = if (resolved.innerShadow) {
            {
                InnerShadow(
                    radius = optics.innerShadowRadius,
                    color = optics.innerShadowColor
                )
            }
        } else {
            null
        },
        onDrawSurface = onDrawSurface ?: { drawRect(tint) }
    )
}

/**
 * Same as [liquidGlass] but without the engine's default highlight/shadow, for
 * surfaces that supply their own (engine API `drawPlainBackdrop`,
 * `DrawBackdropModifier.kt:45-55`).
 */
@Composable
fun Modifier.liquidGlassPlain(
    backdrop: Backdrop,
    shape: Shape = RoundedCornerShape(32.dp),
    optics: LiquidGlassOptics = LiquidGlassOptics.Default,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current,
    isDarkTheme: Boolean = liquidGlassIsDarkTheme(),
    onDrawSurface: (androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit)? = null
): Modifier {
    val resolved = overrides.resolve(capability)
    if (!resolved.backdrop || capability == LiquidGlassCapability.FALLBACK) {
        return if (optics.surfaceTint != null) {
            this.drawBehind { drawRect(optics.surfaceTint) }
        } else {
            this
        }
    }
    val tokens = LiquidGlassTokens.Default
    val tint = optics.surfaceTint
        ?: if (isDarkTheme) tokens.containerTintDark else tokens.containerTintLight
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            if (resolved.vibrancy) vibrancy()
            if (resolved.blur) blur(optics.blurRadius.toPx())
            if (resolved.lens) {
                lens(
                    optics.refractionHeight.toPx(),
                    optics.refractionAmount.toPx(),
                    depthEffect = optics.depthEffect,
                    chromaticAberration = resolved.chromaticAberration
                )
            }
        },
        onDrawSurface = onDrawSurface ?: { drawRect(tint) }
    )
}

/** Convenience: a [LiquidGlassOptics] built from the kit tokens. */
@Composable
@ReadOnlyComposable
fun liquidGlassOptics(
    blurRadius: Dp = liquidGlassTokens().barBlurRadius,
    refractionHeight: Dp = liquidGlassTokens().barRefractionHeight,
    refractionAmount: Dp = liquidGlassTokens().barRefractionAmount
): LiquidGlassOptics = LiquidGlassOptics(
    blurRadius = blurRadius,
    refractionHeight = refractionHeight,
    refractionAmount = refractionAmount
)
