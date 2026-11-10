package com.liquidglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.liquidglass.internal.DampedDragAnimation
import com.liquidglass.internal.InteractiveHighlight
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * A bottom glass tab bar with a draggable, deforming glass indicator.
 *
 * Ported from upstream catalog `components/LiquidBottomTabs.kt` (Apache-2.0)
 * and `components/LiquidBottomTab.kt`.
 *
 * Numbers, all traceable in `analysis/upstream-defaults.tsv`:
 *  - tab width `(maxWidth - 8.dp) / tabsCount`          [LiquidBottomTabs.kt:83-85]
 *  - panel offset `4.dp * sign(f) * EaseOut(|f|)`      [LiquidBottomTabs.kt:92]
 *  - panel return spring `spring(1f, 300f, 0.5f)`      [LiquidBottomTabs.kt:118]
 *  - indicator pressed scale `78f/56f`                 [LiquidBottomTabs.kt:109]
 *  - container effects `vibrancy(); blur(8.dp); lens(24.dp, 24.dp)` [:169-173]
 *  - container height 64dp, padding 4dp                 [:183,185]
 *  - tab content scale `lerp(1f, 1.2f, p)`             [:191-193]
 *  - mid layer: invisible copy (alpha 0), height 56dp   [:198,222]
 *  - indicator effects `lens(10.dp*p, 14.dp*p, ca)`    [:245-250]
 *  - indicator inner shadow `8.dp*p`                   [:260-266]
 *  - velocity divisor 10, squash 0.75/0.25, clamp 0.2 [:270-272]
 *
 * Interaction: drag the indicator to change tabs, or simply tap a tab 鈥? * a tap without movement is resolved to the tapped slot (the indicator layer
 * sits above the tab content and therefore receives the pointer events).
 */
@Composable
fun LiquidGlassTabBar(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    capability: LiquidGlassCapability = LocalLiquidGlassCapability.current,
    overrides: LiquidGlassOverrides = LocalLiquidGlassOverrides.current,
    tokens: LiquidGlassTokens = liquidGlassTokens(),
    content: @Composable RowScope.() -> Unit
) {
    require(tabsCount > 0) { "tabsCount must be > 0" }
    val isDark = liquidGlassIsDarkTheme()
    val accentColor = if (isDark) tokens.accentDark else tokens.accentLight
    val containerColor = if (isDark) tokens.containerTintDark else tokens.containerTintLight

    val tabsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        // LiquidBottomTabs.kt:83-85
        val resolved = overrides.resolve(capability)
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8f.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        // LiquidBottomTabs.kt:88-95
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    tokens.panelOffsetDistance.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var currentIndex by remember(selectedTabIndex) {
            mutableIntStateOf(selectedTabIndex())
        }
        val dampedDragAnimation = remember(animationScope, tabsCount) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTabIndex().toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = tokens.indicatorPressedScale,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    currentIndex = targetIndex
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(
                                tokens.panelReturnDampingRatio,
                                tokens.panelReturnStiffness,
                                tokens.panelReturnVisibilityThreshold
                            )
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }
        LaunchedEffect(selectedTabIndex) {
            snapshotFlow { selectedTabIndex() }
                .collectLatest { index ->
                    currentIndex = index
                }
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentIndex }
                .drop(1)
                .collectLatest { index ->
                    dampedDragAnimation.animateToValue(index.toFloat())
                    onTabSelected(index)
                }
        }

        val interactiveHighlight = remember(animationScope) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, offset ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        // ===== layer 1: glass container =====
        Row(
            Modifier
                .graphicsLayer { translationX = panelOffset }
                .then(
                    if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { Capsule() },
                        effects = {
                            if (resolved.vibrancy) vibrancy()
                            if (resolved.blur) blur(tokens.barBlurRadius.toPx())
                            if (resolved.lens) lens(
                                tokens.barRefractionHeight.toPx(),
                                tokens.barRefractionAmount.toPx()
                            )
                        },
                        layerBlock = {
                            val progress = dampedDragAnimation.pressProgress
                            val scale = lerp(1f, 1f + 16f.dp.toPx() / size.width, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                )
                .then(interactiveHighlight.modifier)
                .height(tokens.barHeight)
                .fillMaxWidth()
                .padding(tokens.barPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )

        // ===== layer 2: invisible copy of the tab row, recorded for the indicator =====
        CompositionLocalProvider(
            LocalLiquidGlassTabScale provides {
                lerp(1f, tokens.tabPressedScale, dampedDragAnimation.pressProgress)
            }
        ) {
            Row(
                Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .then(
                        if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { Capsule() },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                if (resolved.vibrancy) vibrancy()
                                if (resolved.blur) blur(tokens.barBlurRadius.toPx())
                                if (resolved.lens) lens(
                                    tokens.barRefractionHeight.toPx() * progress,
                                    tokens.barRefractionAmount.toPx() * progress
                                )
                            },
                            highlight = {
                                val progress = dampedDragAnimation.pressProgress
                                Highlight.Default.copy(alpha = progress)
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                    )
                    .then(interactiveHighlight.modifier)
                    .height(tokens.tabRowHeight)
                    .fillMaxWidth()
                    .padding(horizontal = tokens.barPadding)
                    .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        // ===== layer 3: the glass indicator =====
        Box(
            Modifier
                .padding(horizontal = tokens.barPadding)
                .graphicsLayer {
                    translationX =
                        if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                }
                .then(interactiveHighlight.gestureModifier)
                .then(dampedDragAnimation.modifier)
                // Tap (without drag) selects the tapped slot. The indicator layer
                // sits above the tab content, so it owns the pointer events.
                //
                // IMPORTANT: this handler must NOT consume the pointer events.
                // It observes them (Main pass, no consume) so that siblings and
                // ancestors keep receiving them - consuming here would swallow
                // every click inside the bar's parent (e.g. a LazyColumn item).
                .pointerInput(tabsCount, isLtr, tabWidth) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Main)
                        val localX = if (isLtr) down.position.x else size.width - down.position.x
                        val tappedSlot = (localX / tabWidth).toInt().fastCoerceIn(0, tabsCount - 1)
                        var moved = false
                        var upChange: PointerInputChange? = null
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) continue
                            if (change.changedToUpIgnoreConsumed()) {
                                upChange = change
                                break
                            }
                            if (change.positionChange() != Offset.Zero) {
                                moved = true
                                break
                            }
                        }
                        if (!moved && upChange != null) {
                            currentIndex = tappedSlot
                            dampedDragAnimation.animateToValue(tappedSlot.toFloat())
                            onTabSelected(tappedSlot)
                            animationScope.launch {
                                offsetAnimation.animateTo(
                                    0f,
                                    spring(
                                        tokens.panelReturnDampingRatio,
                                        tokens.panelReturnStiffness,
                                        tokens.panelReturnVisibilityThreshold
                                    )
                                )
                            }
                        }
                    }
                }
                .then(
                    if (resolved.isPlain) Modifier else Modifier.drawBackdrop(
                        backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            if (resolved.lens) lens(
                                10f.dp.toPx() * progress,
                                14f.dp.toPx() * progress,
                                chromaticAberration = true
                            )
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Default.copy(alpha = progress)
                        },
                        shadow = {
                            val progress = dampedDragAnimation.pressProgress
                            Shadow(alpha = progress)
                        },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            if (resolved.innerShadow) InnerShadow(
                                radius = 8f.dp * progress,
                                alpha = progress
                            ) else null
                        },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / tokens.indicatorVelocityDivisor
                            scaleX /= 1f - (velocity * tokens.indicatorScaleXSquash).fastCoerceIn(
                                -tokens.indicatorSquashLimit,
                                tokens.indicatorSquashLimit
                            )
                            scaleY *= 1f - (velocity * tokens.indicatorScaleYSquash).fastCoerceIn(
                                -tokens.indicatorSquashLimit,
                                tokens.indicatorSquashLimit
                            )
                        },
                        onDrawSurface = {
                            val progress = dampedDragAnimation.pressProgress
                            drawRect(
                                if (isDark) Color.Black.copy(0.1f)
                                else Color.White.copy(0.1f),
                                alpha = 1f - progress
                            )
                            drawRect(Color.Black.copy(alpha = 0.03f * progress))
                        }
                    )
                )
                .height(tokens.tabRowHeight)
                .fillMaxWidth(1f / tabsCount)
        )
    }
}

/** Per-tab content scale, driven by the tab bar's press progress. */
internal val LocalLiquidGlassTabScale =
    androidx.compose.runtime.staticCompositionLocalOf { { 1f } }

/**
 * One tab slot inside [LiquidGlassTabBar].
 *
 * Ported from upstream `components/LiquidBottomTab.kt`:
 * capsule clip, no ripple, content spacing 2dp, press scale 1鈫?.2.
 */
@Composable
fun RowScope.LiquidGlassTab(
    selected: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidGlassTabScale.current
    Column(
        modifier
            .clip(Capsule())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val s = scale()
                scaleX = s
                scaleY = s
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
