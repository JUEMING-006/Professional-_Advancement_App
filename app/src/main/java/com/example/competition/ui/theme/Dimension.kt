package com.example.competition.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 间距与圆角。
 *
 * 改造前全部为字面量，共 862 处 `dp`/`sp` 散落。现集中于此，
 * 保证 14 个界面的节奏一致。
 */

/** 页面左右安全边距。 */
val ScreenPadding = 20.dp

/** 卡片内边距。 */
val CardPadding = 16.dp

/** 元素之间的常规间距。 */
val GapSmall = 6.dp

/** 元素之间的中等间距。 */
val GapMedium = 12.dp

/** 元素之间的大间距（区块之间）。 */
val GapLarge = 20.dp

/** 列表项之间的间距。 */
val ListItemSpacing = 10.dp

/** 小圆角：标签、chip。 */
val RadiusSmall = 8.dp

/** 中圆角：按钮、输入框。 */
val RadiusMedium = 12.dp

/** 大圆角：卡片。 */
val RadiusLarge = 20.dp

/** 液态玻璃卡片默认圆角（与套件 token 对齐）。 */
val RadiusGlass = 32.dp

/** 可点击元素的最小尺寸，符合无障碍最小点击区域要求。 */
val MinTouchTarget = 48.dp
