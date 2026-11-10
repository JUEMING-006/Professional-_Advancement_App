package com.example.competition.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.competition.R
import com.example.competition.ui.theme.TextMuted
import com.example.competition.ui.theme.TextPrimary
import com.liquidglass.LiquidGlassTab
import com.liquidglass.LiquidGlassTabBar
import com.kyant.backdrop.Backdrop

/**
 * 底部导航栏（液态玻璃）。
 *
 * 改造前：5 个界面各写一份私有 `BottomNavigationBar`，
 * 其中 `HotProfessionalScreen` 那份还是"死"的——只切高亮不导航。
 * 现统一到本组件，Tab 选中态由外部路由决定。
 */

/** 底部导航的 5 个 Tab。 */
enum class AppTab(
    val label: String,
    val iconRes: Int,
) {
    Discovery("发现", R.drawable.ic_compass),
    School("校赛", R.drawable.ic_trophy_muted),
    Preparation("备赛", R.drawable.ic_book_open),
    Record("记录", R.drawable.ic_notebook_pen),
    Profile("我的", R.drawable.ic_user_round),
}

@Composable
fun LiquidGlassBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val tabs = AppTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)

    // 套件的 TabBar 内部用 remember(selectedTabIndex) 与 LaunchedEffect(selectedTabIndex)
    // 跟踪选中态，两者都以 lambda 引用为 key。直接传 lambda 的话，每次重组都是新对象，
    // 内部状态会被反复重置 —— 表现为「页面切了但底栏高亮不动」。
    // 解决办法：把 lambda 固定为 remember 出来的稳定引用，内容用 State 承载。
    val selectedState = rememberUpdatedState(selectedIndex)
    val callbackState = rememberUpdatedState(onTabSelected)
    val stableSelectedIndex: () -> Int = remember { { selectedState.value } }
    val stableOnTabSelected: (Int) -> Unit = remember { { index -> callbackState.value(tabs[index]) } }

    LiquidGlassTabBar(
        selectedTabIndex = stableSelectedIndex,
        onTabSelected = stableOnTabSelected,
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            LiquidGlassTab(
                selected = selected,
                onClick = { onTabSelected(tab) },
            ) {
                Column(
                    modifier = Modifier.semantics { contentDescription = tab.label },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(tab.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    androidx.compose.material3.Text(
                        text = tab.label,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = if (selected) TextPrimary else TextMuted,
                    )
                }
            }
        }
    }
}
