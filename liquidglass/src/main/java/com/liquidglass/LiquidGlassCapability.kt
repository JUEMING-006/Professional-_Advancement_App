package com.liquidglass

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Capability tier of the liquid-glass kit.
 *
 * The three tiers are exactly the values Coolapk 16.6.6 ships in its compiled
 * resource arrays:
 *  - `array/liquid_glass_capability_value` = ["full", "blur_only", "fallback"]
 *  - `array/liquid_glass_capability_name` = ["液态玻璃", "背景模糊", "半透明"]
 * (evidence: `analysis/data/liquid-resources.txt`)
 */
@Immutable
enum class LiquidGlassCapability {
    /** 液态玻璃 — blur + refraction + dispersion + highlight + inner shadow. */
    FULL,

    /** 背景模糊 — blur only, no refraction/dispersion. */
    BLUR_ONLY,

    /** 半透明 — no RenderEffect at all, plain translucent surface. */
    FALLBACK;

    /** True when refraction (lens) may be requested. Requires API 33+. */
    val supportsRefraction: Boolean
        get() = this == FULL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** True when blur may be requested. Requires API 31+. */
    val supportsBlur: Boolean
        get() = this != FALLBACK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** True when the shader-based specular highlight may be requested. Requires API 33+. */
    val supportsHighlightShader: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

/**
 * Per-effect overrides, mirroring Coolapk's `LiquidGlassCapabilityOverrides`
 * (ten boolean properties recovered from the APK string pool:
 *  useVibrancy, useBlur, useChromaticAberration, useFilter, useInnerShadow,
 *  useLens, useRuntimeShader, useTargetVersion, useVibrancy, blurOnly).
 *
 * `null` = not overridden, follow the [LiquidGlassCapability] tier.
 */
@Immutable
data class LiquidGlassOverrides(
    val useVibrancy: Boolean? = null,
    val useBlur: Boolean? = null,
    val useLens: Boolean? = null,
    val useChromaticAberration: Boolean? = null,
    val useInnerShadow: Boolean? = null,
    val useBackdrop: Boolean? = null,
    val blurOnly: Boolean? = null
) {
    /**
     * Resolves this override set against [capability] into the concrete per-effect
     * booleans every glass component must honour.
     *
     * Components MUST route their effect DSL through this - gating only on
     * `capability == FALLBACK` would silently ignore [LiquidGlassCapability.BLUR_ONLY]
     * and every field of [LiquidGlassOverrides].
     */
    fun resolve(capability: LiquidGlassCapability): LiquidGlassResolvedEffects {
        val full = capability == LiquidGlassCapability.FULL
        return LiquidGlassResolvedEffects(
            vibrancy = useVibrancy ?: full,
            blur = useBlur ?: capability.supportsBlur,
            lens = useLens ?: capability.supportsRefraction,
            chromaticAberration = useChromaticAberration ?: capability.supportsRefraction,
            innerShadow = useInnerShadow ?: full,
            backdrop = useBackdrop ?: true
        )
    }
}

/**
 * The concrete effect set a glass component must apply for the current
 * capability tier + overrides. Produced by [LiquidGlassOverrides.resolve].
 */
@Immutable
data class LiquidGlassResolvedEffects(
    val vibrancy: Boolean,
    val blur: Boolean,
    val lens: Boolean,
    val chromaticAberration: Boolean,
    val innerShadow: Boolean,
    val backdrop: Boolean
) {
    /** True when no RenderEffect-based effect may be applied at all. */
    val isPlain: Boolean get() = !backdrop || (!vibrancy && !blur && !lens)
}

/** Convenience: resolve the kit-wide overrides against a tier. */
fun LiquidGlassCapability.resolveEffects(
    overrides: LiquidGlassOverrides = LiquidGlassOverrides()
): LiquidGlassResolvedEffects = overrides.resolve(this)

/** CompositionLocal holding the active capability tier. */
val LocalLiquidGlassCapability = staticCompositionLocalOf { LiquidGlassCapability.FULL }

/** CompositionLocal holding per-effect overrides. */
val LocalLiquidGlassOverrides = staticCompositionLocalOf { LiquidGlassOverrides() }

/** CompositionLocal holding the dark-theme flag (upstream reads `isSystemInDarkTheme()`). */
val LocalLiquidGlassIsDarkTheme = staticCompositionLocalOf { false }

/**
 * Installs the kit-wide configuration for [content].
 *
 * ```
 * LiquidGlassTheme(capability = LiquidGlassCapability.FULL) { … }
 * ```
 */
@Composable
fun LiquidGlassTheme(
    capability: LiquidGlassCapability = LiquidGlassCapability.FULL,
    overrides: LiquidGlassOverrides = LiquidGlassOverrides(),
    tokens: LiquidGlassTokens = LiquidGlassTokens.Default,
    isDarkTheme: Boolean = liquidGlassIsDarkTheme(),
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLiquidGlassCapability provides capability,
        LocalLiquidGlassOverrides provides overrides,
        LocalLiquidGlassTokens provides tokens,
        LocalLiquidGlassIsDarkTheme provides isDarkTheme
    ) {
        content()
    }
}

/** Current capability tier. */
@Composable
@ReadOnlyComposable
fun liquidGlassCapability(): LiquidGlassCapability = LocalLiquidGlassCapability.current

/** Current overrides. */
@Composable
@ReadOnlyComposable
fun liquidGlassOverrides(): LiquidGlassOverrides = LocalLiquidGlassOverrides.current

/** Current design tokens. */
@Composable
@ReadOnlyComposable
fun liquidGlassTokens(): LiquidGlassTokens = LocalLiquidGlassTokens.current

/** True when the device can render refraction (API 33+). */
val deviceSupportsRefraction: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** True when the device can render blur (API 31+). */
val deviceSupportsBlur: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Dark-theme flag for the kit. Uses [LocalLiquidGlassIsDarkTheme] so that a
 * host app can force a theme; falls back to the system setting.
 */
@Composable
@ReadOnlyComposable
fun liquidGlassIsDarkTheme(): Boolean =
    if (LocalLiquidGlassIsDarkTheme.current) true
    else androidx.compose.foundation.isSystemInDarkTheme()
