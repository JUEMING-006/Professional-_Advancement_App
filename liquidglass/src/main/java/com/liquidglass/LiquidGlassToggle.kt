package com.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.liquidglass.internal.DampedDragAnimation
import kotlinx.coroutines.flow.collectLatest

/**
 * A liquid-glass switch.
 *
 * Ported from upstream catalog `components/LiquidToggle.kt` (Apache-2.0).
 * Numbers, all traceable in `analysis/upstream-defaults.tsv`:
 *  - accent light `0xFF34C759` / dark `0xFF30D158`   [LiquidToggle.kt:56-57]
 *  - track light `0xFF787878@0.2` / dark `0xFF787880@0.36` [LiquidToggle.kt:59-60]
 *  - drag width 20dp                                   [LiquidToggle.kt:64]
 *  - pressed scale 1.5                                 [LiquidToggle.kt:75]
 *  - track size 64x28dp                                [LiquidToggle.kt:129]
 *  - thumb inset 2dp                                   [LiquidToggle.kt:136]
 *  - thumb track-backdrop scaleX lerp(2/3, 0.75, p)    [LiquidToggle.kt:150]
 *  - thumb track-backdrop scaleY lerp(0, 0.75, p)      [LiquidToggle.kt:151]
 *  - effects `blur(8dp*(1-p)); lens(5dp*p, 10dp*p, ca)` [LiquidToggle.kt:160-165]
 *  - highlight `Highlight.Ambient` width/1.5, blurRadius/1.5, alpha=p
 *                                                          [LiquidToggle.kt:169-173]
 *  - shadow radius 4dp, black @ 0.05                   [LiquidToggle.kt:176-179]
 *  - velocity divisor 50                               [LiquidToggle.kt:191]
 *  - thumb size 40x24dp                                [LiquidToggle.kt:200]
 */
@Composable
fun LiquidGlassToggle(
    selected: () -> Boolean,
    onSelect: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current
) {
    val resolved = overrides.resolve(capability)
    val tokens = liquidGlassTokens()
    val isDark = liquidGlassIsDarkTheme()
    val accentColor =
        if (isDark) tokens.toggleAccentDark else tokens.toggleAccentLight
    val trackColor =
        if (isDark) tokens.trackDark else tokens.trackLight

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { tokens.toggleDragWidth.toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (selected()) 1f else 0f) }

    val dampedDragAnimation = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = tokens.togglePressedScale,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    fraction = if (targetValue >= 0.5f) 1f else 0f
                    onSelect(fraction == 1f)
                    didDrag = false
                } else {
                    fraction = if (selected()) 0f else 1f
                    onSelect(fraction == 1f)
                }
            },
            onDrag = { _, dragAmount ->
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                val delta = dragAmount.x / dragWidth
                fraction =
                    if (isLtr) (fraction + delta).fastCoerceIn(0f, 1f)
                    else (fraction - delta).fastCoerceIn(0f, 1f)
            }
        )
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction }
            .collectLatest { fraction ->
                dampedDragAnimation.updateValue(fraction)
            }
    }
    LaunchedEffect(selected) {
        snapshotFlow { selected() }
            .collectLatest { isSelected ->
                val target = if (isSelected) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .clip(Capsule())
                .background(trackColor)
                .size(tokens.toggleTrackSize.width, tokens.toggleTrackSize.height)
        )

        Box(
            Modifier
                .graphicsLayer {
                    val fraction = dampedDragAnimation.value
                    val padding = tokens.toggleThumbInset.toPx()
                    translationX =
                        if (isLtr) lerp(padding, padding + dragWidth, fraction)
                        else lerp(-padding, -(padding + dragWidth), fraction)
                }
                .semantics { role = Role.Switch }
                .then(dampedDragAnimation.modifier)
                .then(
                    if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                        backdrop = rememberCombinedBackdrop(
                            backdrop,
                            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                val progress = dampedDragAnimation.pressProgress
                                val scaleX = lerp(2f / 3f, 0.75f, progress)
                                val scaleY = lerp(0f, 0.75f, progress)
                                scale(scaleX, scaleY) {
                                    drawBackdrop()
                                }
                            }
                        ),
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            if (resolved.blur) blur(tokens.barBlurRadius.toPx() * (1f - progress))
                            if (resolved.lens) lens(
                                5f.dp.toPx() * progress,
                                10f.dp.toPx() * progress,
                                chromaticAberration = true
                            )
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Ambient.copy(
                                width = Highlight.Ambient.width / 1.5f,
                                blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                                alpha = progress
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 4f.dp,
                                color = Color.Black.copy(alpha = 0.05f)
                            )
                        },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            if (resolved.innerShadow) InnerShadow(
                                radius = 4f.dp * progress,
                                alpha = progress
                            ) else null
                        },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / tokens.toggleVelocityDivisor
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            val progress = dampedDragAnimation.pressProgress
                            drawRect(Color.White.copy(alpha = 1f - progress))
                        }
                    )
                )
                .size(tokens.thumbSize.width, tokens.thumbSize.height)
        )
    }
}
