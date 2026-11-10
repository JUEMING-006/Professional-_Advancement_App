package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.CompetitionSummary
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.hot.HotCategory
import com.example.competition.ui.hot.HotProfessionalViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val TextMuted = Color(0xFF939D97)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 专业分类。
 *
 * `code` 对应后端 `categoryCode`，空串表示「全部」。
 * 分类是前端展示结构，不是后端数据 —— 后端只提供 categoryCode。
 */
private val categories = listOf(
    HotCategory("全部", "全部赛事，总有一个适合你", "浏览全部热门与专业赛事，找到属于你的舞台。", ""),
    HotCategory("信息技术", "信息技术，也欢迎跨专业组队", "产品策划、设计、开发，都能找到位置。", "information"),
    HotCategory("经管商科", "经管商科，商业思维驱动成长", "市场、财务、运营与战略，都能找到位置。", "business"),
    HotCategory("设计艺术", "设计艺术，用创意表达自己", "视觉、交互、产品与影像，都能找到位置。", "art"),
    HotCategory("理工制造", "理工制造，把想法变成作品", "机械、电子、材料与智造，都能找到位置。", "engineering"),
    HotCategory("人文社科", "人文社科，洞察社会与人心", "调研、策划、内容与传播，都能找到位置。", ""),
)
// ===== 专业分类数据 =====
/**
 * 热门与专业赛事页 —— 竞学（第二页）
 * 与 Figma 原型（node 23:7443）保持一致。
 *
 * 数据来源：`GET /competitions`。分类是前端展示结构，赛事数据全部来自后端。
 */
@Composable
fun HotProfessionalScreen(
    onBack: () -> Unit = {},
    onDetailClick: (String) -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: HotProfessionalViewModel = viewModel(
        factory = HotProfessionalViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    var selectedTab by remember { mutableStateOf("专业大类") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        HotContent(
            state = state,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            selectedCategoryIndex = selectedCategoryIndex,
            onCategorySelected = { selectedCategoryIndex = it },
            onDetailClick = onDetailClick,
        )
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.HotContent(
    state: LoadState<List<CompetitionSummary>>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    selectedCategoryIndex: Int,
    onCategorySelected: (Int) -> Unit,
    onDetailClick: (String) -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(message = state.message, modifier = Modifier.weight(1f))
        is LoadState.Success -> {
            val category = categories[selectedCategoryIndex]
            val filtered = if (selectedTab == "热门推荐") {
                state.data
            } else {
                if (category.code.isBlank()) state.data
                else state.data.filter { it.categoryCode == category.code }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item { SearchBox() }
                item { DiscoverTabs(selectedTab = selectedTab, onTabSelected = onTabSelected) }
                if (selectedTab == "专业大类") {
                    item {
                        CategoryFilter(
                            categories = categories,
                            selectedIndex = selectedCategoryIndex,
                            onSelected = onCategorySelected,
                        )
                    }
                    item { CategoryDescription(category = category) }
                }
                item {
                    SectionTitle(
                        title = if (selectedTab == "热门推荐") "热门推荐赛事" else category.name + "赛事",
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        EmptyView(
                            title = "这个分类暂时没有赛事",
                            hint = "换个分类看看，或去发现页添加比赛",
                        )
                    }
                } else {
                    filtered.forEach { competition ->
                        item(key = competition.id) {
                            InfoCard(competition = competition, onClick = { onDetailClick(competition.id) })
                        }
                    }
                }
                item { FooterNote() }
            }
        }
    }
}

// ===== 页面导航 =====
@Composable
private fun PageNavigation(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = "返回",
            modifier = Modifier
                .size(20.dp)
                .clickable { onBack() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "找到你的主场",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "赛事发现 / 热门与专业",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_ellipsis),
            contentDescription = "更多",
            modifier = Modifier.size(20.dp)
        )
    }
}

// ===== 搜索框 =====
@Composable
private fun SearchBox() {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGrayBg)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(
                    text = "搜索赛事、专业或关键词",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = TextPrimary
                ),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryGreen)
            )
        }
    }
}

// ===== 发现方式 Tab =====
@Composable
private fun DiscoverTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            verticalAlignment = Alignment.Top
        ) {
            DiscoverTabItem(
                text = "热门推荐",
                selected = selectedTab == "热门推荐",
                onClick = { onTabSelected("热门推荐") }
            )
            DiscoverTabItem(
                text = "专业大类",
                selected = selectedTab == "专业大类",
                onClick = { onTabSelected("专业大类") }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )
    }
}

@Composable
private fun DiscoverTabItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) PrimaryGreen else TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 4 个全角字符在 16sp 下宽度为 4 * 16sp = 64dp，与文字同宽
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) PrimaryGreen else Color.Transparent)
        )
    }
}

// ===== 专业筛选 =====
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilter(
    categories: List<HotCategory>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEachIndexed { index, category ->
            val selected = index == selectedIndex
            Text(
                text = category.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) PrimaryGreen else TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) LightGreenBg else LightGrayBg)
                    .clickable { onSelected(index) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

// ===== 分类说明 =====
@Composable
private fun CategoryDescription(category: HotCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LightGreenBg)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_code_xml),
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = category.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = category.subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

// ===== 区块标题 =====
@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "综合推荐⌄ · 筛选",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 信息卡片 =====
@Composable
private fun InfoCard(
    competition: CompetitionSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Tag(text = if (competition.isSample) "示例赛事" else "已识别")
            Image(
                painter = painterResource(R.drawable.ic_bookmark),
                contentDescription = "收藏",
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = competition.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 25.2.sp
        )

        val subtitle = listOf(competition.trackName, competition.teamSizeText)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        if (competition.organizerName.isNotBlank()) {
            Text(
                text = "主办：${competition.organizerName}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = deadlineText(competition.deadline),
                fontSize = 11.sp,
                color = if (isOpen(competition.status)) PrimaryGreen else TextSecondary
            )
            Text(
                text = "查看赛事 →",
                fontSize = 12.sp,
                color = PrimaryGreen
            )
        }
    }
}

/** 状态是否可报名。 */
private fun isOpen(status: String): Boolean = status == "open" || status == "registering"

/** 报名截止展示（ISO → 月.日）。 */
private fun deadlineText(deadline: String): String {
    if (deadline.isBlank()) return ""
    val short = runCatching {
        val parts = deadline.substringBefore('T').split('-')
        parts[1].trimStart('0') + "." + parts[2]
    }.getOrNull()
    return if (short.isNullOrBlank()) "报名截止 $deadline" else "报名截止 $short"
}
@Composable
private fun FooterNote() {
    Text(
        text = "以上为产品演示用示例赛事，非实际报名信息",
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = TextMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ===== 标签 =====
@Composable
private fun Tag(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = PrimaryGreen,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightGreenBg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun HotProfessionalScreenPreview() {
    HotProfessionalScreen()
}