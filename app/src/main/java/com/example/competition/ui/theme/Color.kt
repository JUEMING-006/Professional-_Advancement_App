package com.example.competition.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 品牌色板。
 *
 * 改造前这 7 个色值以字面量形式散落在 13 个界面文件里，共出现 81 次。
 * 现集中于此，界面代码一律引用常量，避免改主题要翻十几个文件。
 */

/** 主色：深绿。用于强调按钮、选中态、进度条填充。 */
val BrandPrimary = Color(0xFF246B54)

/** 主色浅色背景：用于标签底色、选中 chip 底色。 */
val BrandSurfaceTint = Color(0xFFEDF5F0)

/** 主文字色。 */
val TextPrimary = Color(0xFF202A26)

/** 分隔线与描边。 */
val BorderSubtle = Color(0xFFE7ECE9)

/** 次级文字：说明文字、元信息。 */
val TextSecondary = Color(0xFF69746F)

/** 卡片背景。 */
val SurfaceCard = Color(0xFFF6F8F6)

/** 弱化文字与占位图标。 */
val TextMuted = Color(0xFF939D97)

/** 页面底色。 */
val SurfaceBackground = Color(0xFFFFFFFF)

/** 危险与警示：红线违规、未完成状态。 */
val StatusDanger = Color(0xFFB3261E)
