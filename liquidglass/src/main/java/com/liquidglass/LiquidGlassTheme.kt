package com.liquidglass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens for the liquid-glass kit.
 *
 * Every numeric default is taken from the upstream library / catalog and is
 * traceable in `analysis/upstream-defaults.tsv` (cited per field).
 *
 * The values mirror what Coolapk 16.6.6 ships: the bottom bar geometry comes
 * from Coolapk's own compiled resources
 * (`dimen/liquid_bottom_navigation_height` = 56dp,
 *  `dimen/liquid_bottom_navigation_content_padding` = 4dp —
 *  see `analysis/data/liquid-resources.txt`).
 */
@Immutable
data class LiquidGlassTokens(
    // ---- container tints -------------------------------------------------
    /** Light-theme glass surface tint. [LiquidBottomTabs.kt:72-74] */
    val containerTintLight: Color = Color(0xFFFAFAFA).copy(alpha = 0.4f),
    /** Dark-theme glass surface tint. [LiquidBottomTabs.kt:72-74] */
    val containerTintDark: Color = Color(0xFF121212).copy(alpha = 0.4f),

    // ---- accents ----------------------------------------------------------
    /** Light-theme accent. [LiquidBottomTabs.kt:69-70] */
    val accentLight: Color = Color(0xFF0088FF),
    /** Dark-theme accent. [LiquidBottomTabs.kt:69-70] */
    val accentDark: Color = Color(0xFF0091FF),
    /** Toggle accent, light theme. [LiquidToggle.kt:56] */
    val toggleAccentLight: Color = Color(0xFF34C759),
    /** Toggle accent, dark theme. [LiquidToggle.kt:57] */
    val toggleAccentDark: Color = Color(0xFF30D158),

    // ---- track colors -----------------------------------------------------
    /** Slider/toggle track, light theme. [LiquidSlider.kt:62] */
    val trackLight: Color = Color(0xFF787878).copy(alpha = 0.2f),
    /** Slider/toggle track, dark theme. [LiquidSlider.kt:63] */
    val trackDark: Color = Color(0xFF787880).copy(alpha = 0.36f),

    // ---- optics -----------------------------------------------------------
    /** Bar blur radius. [LiquidBottomTabs.kt:171] */
    val barBlurRadius: Dp = 8.dp,
    /** Bar refraction height. [LiquidBottomTabs.kt:172] */
    val barRefractionHeight: Dp = 24.dp,
    /** Bar refraction amount. [LiquidBottomTabs.kt:172] */
    val barRefractionAmount: Dp = 24.dp,
    /** Button blur radius. [LiquidButton.kt:60] */
    val buttonBlurRadius: Dp = 2.dp,
    /** Button refraction height. [LiquidButton.kt:61] */
    val buttonRefractionHeight: Dp = 12.dp,
    /** Button refraction amount. [LiquidButton.kt:61] */
    val buttonRefractionAmount: Dp = 24.dp,

    // ---- geometry ---------------------------------------------------------
    /** Bottom bar outer height. [LiquidBottomTabs.kt:183] */
    val barHeight: Dp = 64.dp,
    /** Bottom bar inner (tab row) height. [LiquidBottomTabs.kt:222,284] */
    val tabRowHeight: Dp = 56.dp,
    /** Bottom bar padding. [LiquidBottomTabs.kt:185] */
    val barPadding: Dp = 4.dp,
    /** Tab content spacing. [LiquidBottomTab.kt:45] */
    val tabContentSpacing: Dp = 2.dp,
    /** Button height. [LiquidButton.kt:116] */
    val buttonHeight: Dp = 48.dp,
    /** Button horizontal padding. [LiquidButton.kt:117] */
    val buttonPadding: Dp = 16.dp,
    /** Button content spacing. [LiquidButton.kt:118] */
    val buttonContentSpacing: Dp = 8.dp,
    /** Slider track height. [LiquidSlider.kt:127,135] */
    val sliderTrackHeight: Dp = 6.dp,
    /** Slider / toggle thumb size. [LiquidSlider.kt:209, LiquidToggle.kt:200] */
    val thumbSize: androidx.compose.ui.unit.DpSize = androidx.compose.ui.unit.DpSize(40.dp, 24.dp),
    /** Toggle track size. [LiquidToggle.kt:129] */
    val toggleTrackSize: androidx.compose.ui.unit.DpSize = androidx.compose.ui.unit.DpSize(64.dp, 28.dp),
    /** Toggle thumb inset from the track edge. [LiquidToggle.kt:136] */
    val toggleThumbInset: Dp = 2.dp,
    /** Toggle drag travel distance. [LiquidToggle.kt:64] */
    val toggleDragWidth: Dp = 20.dp,

    // ---- radii ------------------------------------------------------------
    /** Card corner radius. [ScrollContainerContent.kt:40] */
    val cardCornerRadius: Dp = 32.dp,

    // ---- motion -----------------------------------------------------------
    /** Indicator pressed scale (78/56). [LiquidBottomTabs.kt:109] */
    val indicatorPressedScale: Float = 78f / 56f,
    /** Tab content scale on press. [LiquidBottomTabs.kt:191-193] */
    val tabPressedScale: Float = 1.2f,
    /** Panel slide distance while dragging. [LiquidBottomTabs.kt:92] */
    val panelOffsetDistance: Dp = 4.dp,
    /** Panel return spring. [LiquidBottomTabs.kt:118] */
    val panelReturnDampingRatio: Float = 1f,
    /** Panel return spring stiffness. [LiquidBottomTabs.kt:118] */
    val panelReturnStiffness: Float = 300f,
    /** Panel return spring visibility threshold. [LiquidBottomTabs.kt:118] */
    val panelReturnVisibilityThreshold: Float = 0.5f,
    /** Indicator velocity divisor. [LiquidBottomTabs.kt:270] */
    val indicatorVelocityDivisor: Float = 10f,
    /** Indicator horizontal squash factor. [LiquidBottomTabs.kt:271] */
    val indicatorScaleXSquash: Float = 0.75f,
    /** Indicator vertical squash factor. [LiquidBottomTabs.kt:272] */
    val indicatorScaleYSquash: Float = 0.25f,
    /** Indicator squash clamp. [LiquidBottomTabs.kt:271-272] */
    val indicatorSquashLimit: Float = 0.2f,
    /** Button press growth. [LiquidButton.kt:69] */
    val buttonPressGrowth: Dp = 4.dp,
    /** Button drag scale. [LiquidButton.kt:77] */
    val buttonDragScale: Dp = 4.dp,
    /** Button follow derivative. [LiquidButton.kt:72] */
    val buttonFollowDerivative: Float = 0.05f,
    /** Toggle pressed scale. [LiquidToggle.kt:75] */
    val togglePressedScale: Float = 1.5f,
    /** Slider pressed scale. [LiquidSlider.kt:83] */
    val sliderPressedScale: Float = 1.5f,
    /** Toggle velocity divisor. [LiquidToggle.kt:191] */
    val toggleVelocityDivisor: Float = 50f
) {
    companion object {
        /** Upstream 1.0.6 defaults. */
        @JvmField
        val Default = LiquidGlassTokens()
    }
}

/** CompositionLocal holding the active tokens. */
val LocalLiquidGlassTokens = staticCompositionLocalOf { LiquidGlassTokens.Default }
