package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.competition.ui.school.SchoolCompetitionViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)
private val MutedGray = Color(0xFF939D97)

/**
 * 校赛页 —— 竞学（底部导航第 2 个 Tab）
 * 与 Figma 原型（node 55:261）保持一致。
 *
 * 数据来源：`GET /competitions?slot=school`。
 */
@Composable
fun SchoolCompetitionScreen(
    onDiscoverClick: () -> Unit = {},
    onPreparationClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onViewDetailClick: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: SchoolCompetitionViewModel = viewModel(
        factory = SchoolCompetitionViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation()
        SchoolContent(
            state = state,
            onSearchClick = onSearchClick,
            onViewDetailClick = onViewDetailClick,
        )
    }
}

/** 校赛内容区：按状态分支。 */
@Composable
private fun ColumnScope.SchoolContent(
    state: LoadState<List<CompetitionSummary>>,
    onSearchClick: () -> Unit,
    onViewDetailClick: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(message = state.message, modifier = Modifier.weight(1f))
        is LoadState.Success -> LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item { SearchBox(onClick = onSearchClick) }
            item {
                FilterSection(
                    title = "比赛类型",
                    options = listOf("全部", "计算机类", "文艺类", "竞赛类", "体育类"),
                )
            }
            item {
                FilterSection(
                    title = "主办学院",
                    options = listOf("全部", "人工智能学院", "工程学院", "经济管理学院", "人文艺术学院"),
                )
            }
            item {
                CompetitionList(items = state.data, onViewDetailClick = onViewDetailClick)
            }
        }
    }
}

// ===== 页面导航 =====
@Composable
private fun PageNavigation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "校赛",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "身边的赛场，也有你的高光",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_bell),
            contentDescription = "通知",
            modifier = Modifier.size(20.dp)
        )
    }
}

// ===== 搜索框 =====
@Composable
private fun SearchBox(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGrayBg)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "搜索校内赛事、学院或关键词",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 分类筛选 =====
@Composable
private fun FilterSection(title: String, options: List<String>) {
    var selected by remember { mutableStateOf(options.first()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    text = option,
                    selected = option == selected,
                    onClick = { selected = option }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) PrimaryGreen else TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LightGreenBg else LightGrayBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

// ===== 校内赛事列表（数据驱动） =====
@Composable
private fun CompetitionList(
    items: List<CompetitionSummary>,
    onViewDetailClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "全部赛事",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = items.size.toString() + " 场",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "最近发布",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                Image(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = "校级与院级赛事 · 来自学校发布",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )

        if (items.isEmpty()) {
            EmptyView(
                title = "还没有校内赛事",
                hint = "学校发布后会自动出现在这里",
            )
        } else {
            items.forEach { competition ->
                CompetitionCard(
                    competition = competition,
                    onViewDetailClick = onViewDetailClick,
                )
            }
        }

        Text(
            text = "以上赛事信息由学校发布，实际安排以通知为准",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = MutedGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompetitionCard(
    competition: CompetitionSummary,
    onViewDetailClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 赛事分类与状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelBadge(
                    level = levelText(competition.typeCode),
                    highlight = true,
                )
                if (competition.trackName.isNotBlank()) {
                    Text(
                        text = competition.trackName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = statusText(competition.status),
                fontSize = 11.sp,
                fontWeight = if (isOpen(competition.status)) FontWeight.Medium else FontWeight.Normal,
                color = if (isOpen(competition.status)) PrimaryGreen else TextSecondary
            )
        }

        // 赛事信息
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = competition.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 25.2.sp
            )
            if (competition.organizerName.isNotBlank()) {
                Text(
                    text = "主办：" + competition.organizerName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 18.6.sp
                )
            }
            if (competition.teamSizeText.isNotBlank()) {
                Text(
                    text = competition.teamSizeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 17.05.sp
                )
            }
        }

        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )

        // 报名时间与详情
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = deadlineText(competition.deadline),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = if (isOpen(competition.status)) PrimaryGreen else TextSecondary
            )
            Text(
                text = "查看赛事 →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                modifier = Modifier.clickable { onViewDetailClick() }
            )
        }
    }
}

/**
 * 赛事**级别**码 → 展示名。
 *
 * ⚠️ 用 `typeCode` 而不是 `categoryCode`：
 * `typeCode` 表示级别（campus/college/national），`categoryCode` 表示学科（information/art）。
 * 这里要显示的是级别，所以取 `typeCode`（已实测踩过坑）。
 */
private fun levelText(typeCode: String): String = when (typeCode) {
    "campus", "school" -> "校级"
    "college" -> "院级"
    "provincial" -> "省级"
    "national" -> "国家级"
    else -> "赛事"
}

/** 状态码 → 中文。后端用 open/closed 等值，这里做展示映射。 */
private fun statusText(status: String): String = when (status) {
    "open", "registering" -> "报名中"
    "upcoming" -> "即将报名"
    "closed", "ended" -> "报名结束"
    "running" -> "进行中"
    else -> status
}

/** 是否处于「可报名」状态（用于高亮）。 */
private fun isOpen(status: String): Boolean = status == "open" || status == "registering"

/**
 * 报名截止展示。
 *
 * 后端返回 ISO 8601（如 `2026-11-15T23:59:00+08:00`），界面只显示月.日。
 * 解析失败时原样返回，不吞掉信息。
 */
private fun deadlineText(deadline: String): String {
    if (deadline.isBlank()) return ""
    val short = runCatching {
        val datePart = deadline.substringBefore('T')
        val parts = datePart.split('-')
        parts[1].trimStart('0') + "." + parts[2]
    }.getOrNull()
    return if (short.isNullOrBlank()) "报名截止 $deadline" else "报名截止 $short"
}

@Composable
private fun LevelBadge(level: String, highlight: Boolean) {
    Text(
        text = level,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = if (highlight) PrimaryGreen else TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlight) LightGreenBg else LightGrayBg)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}


// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun SchoolCompetitionScreenPreview() {
    SchoolCompetitionScreen()
}