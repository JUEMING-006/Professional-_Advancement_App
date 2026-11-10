package com.liquidglass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.liquidglass.internal.InteractiveHighlight
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * A liquid-glass button.
 *
 * Ported from upstream catalog `components/LiquidButton.kt` (Apache-2.0).
 * Numbers, all traceable in `analysis/upstream-defaults.tsv`:
 *  - shape `Capsule()`                                   [LiquidButton.kt:57]
 *  - effects `vibrancy(); blur(2.dp); lens(12.dp, 24.dp)` [LiquidButton.kt:59-61]
 *  - press scale `lerp(1f, 1f + 4.dp/size.height, p)`    [LiquidButton.kt:69]
 *  - follow derivative 0.05 (tanh)                       [LiquidButton.kt:72]
 *  - max drag scale `4.dp / size.height`                 [LiquidButton.kt:77]
 *  - tint: `BlendMode.Hue` then `tint.copy(0.75f)`       [LiquidButton.kt:93-94]
 *  - height 48dp, padding 16dp, spacing 8dp              [LiquidButton.kt:116-118]
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current,
    optics: LiquidGlassOptics = LiquidGlassOptics.Default,
    content: @Composable RowScope.() -> Unit
) {
    val resolved = overrides.resolve(capability)
    val tokens = liquidGlassTokens()
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }

    Row(
        modifier
            .then(
                if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        if (resolved.vibrancy) vibrancy()
                        if (resolved.blur) blur(tokens.buttonBlurRadius.toPx())
                        if (resolved.lens) lens(
                            tokens.buttonRefractionHeight.toPx(),
                            tokens.buttonRefractionAmount.toPx()
                        )
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow.Default },
                    innerShadow = if (resolved.innerShadow) ({ InnerShadow.Default }) else null,
                    layerBlock = if (isInteractive) {
                        {
                            val width = size.width
                            val height = size.height

                            val progress = interactiveHighlight.pressProgress
                            val scale = androidx.compose.ui.util.lerp(
                                1f,
                                1f + tokens.buttonPressGrowth.toPx() / size.height,
                                progress
                            )

                            val maxOffset = size.minDimension
                            val initialDerivative = tokens.buttonFollowDerivative
                            val offset = interactiveHighlight.offset
                            translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                            translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                            val maxDragScale = tokens.buttonDragScale.toPx() / size.height
                            val offsetAngle = atan2(offset.y, offset.x)
                            scaleX =
                                scale +
                                        maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                        (width / height).coerceAtMost(1f)
                            scaleY =
                                scale +
                                        maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                        (height / width).coerceAtMost(1f)
                        }
                    } else {
                        null
                    },
                    onDrawSurface = {
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = androidx.compose.ui.graphics.BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.75f))
                        }
                        if (surfaceColor.isSpecified) {
                            drawRect(surfaceColor)
                        }
                    }
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (isInteractive) {
                    Modifier
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier)
                } else {
                    Modifier
                }
            )
            .defaultMinSize(minHeight = tokens.buttonHeight)
            .padding(horizontal = tokens.buttonPadding),
        horizontalArrangement = Arrangement.spacedBy(tokens.buttonContentSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * A circular icon button.
 *
 * The optical values are the same as [LiquidGlassButton]; only the shape and
 * sizing differ. Circle sizing is not present upstream, so it is expressed as
 * an explicit parameter (no invented default is applied to the optics).
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current
) {
    val resolved = overrides.resolve(capability)
    val tokens = liquidGlassTokens()
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }
    Box(
        modifier
            .then(
                if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        if (resolved.vibrancy) vibrancy()
                        if (resolved.blur) blur(tokens.buttonBlurRadius.toPx())
                        if (resolved.lens) lens(
                            tokens.buttonRefractionHeight.toPx(),
                            tokens.buttonRefractionAmount.toPx()
                        )
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow.Default },
                    innerShadow = if (resolved.innerShadow) ({ InnerShadow.Default }) else null,
                    layerBlock = if (enabled) {
                        {
                            val progress = interactiveHighlight.pressProgress
                            val scale = androidx.compose.ui.util.lerp(
                                1f,
                                1f + tokens.buttonPressGrowth.toPx() / size.height,
                                progress
                            )
                            scaleX = scale
                            scaleY = scale
                        }
                    } else {
                        null
                    },
                    onDrawSurface = {
                        // The icon colour is applied to the Image itself, NOT as a
                        // hue wash on the surface (the hue wash is the upstream
                        // LiquidButton `tint` semantic and would blacken the
                        // surface when the tint is a normal content colour).
                    }
                )
            )
            .size(buttonSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .then(interactiveHighlight.modifier)
            .then(interactiveHighlight.gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        // Draw the vector without androidx.compose.material: rememberVectorPainter
        // lives in ui-graphics, which we already depend on.
        val painter = rememberVectorPainter(image = icon)
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            colorFilter = if (tint.isSpecified) {
                androidx.compose.ui.graphics.ColorFilter.tint(tint)
            } else {
                null
            },
            modifier = Modifier.size(buttonSize * 0.5f)
        )
    }
}
