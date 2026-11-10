package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.competition.domain.ScheduleNode
import com.example.competition.domain.ScheduleResponse
import com.example.competition.domain.ScoringDimension
import com.example.competition.domain.ScoringResponse
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.detail.CompetitionDetailUiState
import com.example.competition.ui.detail.CompetitionDetailViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 规则与赛程页 —— 竞学（第五页）
 * 与 Figma 原型（node 23:7685）保持一致。
 *
 * ⚠️ 本文件**不再定义** `ScoringDimension` / `ScheduleNode`：
 * 原先的本地副本含 `fraction`（进度条比例）与 `highlighted` 字段，
 * 而契约红线 R1/R3 明确禁止后端下发这类字段。
 * 现在统一用 `domain/Contracts.kt` 的契约类型，从源头消除违规可能。
 */
@Composable
fun RulesScheduleScreen(
    competitionId: String,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: CompetitionDetailViewModel = viewModel(
        factory = CompetitionDetailViewModel.Factory(app.container.repository),
    )
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(competitionId) { viewModel.load(competitionId) }

    var selectedTab by remember { mutableStateOf("评分细则") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        RulesContent(
            uiState = uiState,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onNext = onNext,
        )
    }
}

/** 内容区：按加载状态分支。 */
@Composable
private fun ColumnScope.RulesContent(
    uiState: CompetitionDetailUiState,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onNext: () -> Unit,
) {
    val scoring = uiState.scoring
    val schedule = uiState.schedule

    when {
        uiState.loading && scoring == null -> LoadingView(modifier = Modifier.weight(1f))
        uiState.errorMessage != null && scoring == null -> ErrorView(
            message = uiState.errorMessage,
            modifier = Modifier.weight(1f),
        )
        else -> {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item { CategoryTabs(selectedTab = selectedTab, onSelected = onTabSelected) }
                item { ScoringOverview(scoring) }
                if (scoring != null && scoring.dimensions.isNotEmpty()) {
                    scoring.dimensions.forEach { dimension ->
                        item(key = dimension.id) { ScoringCard(dimension) }
                    }
                } else {
                    item {
                        EmptyView(
                            title = "还没有评分细则",
                            hint = "粘贴章程用智能体识别后会出现",
                        )
                    }
                }
                item { ScheduleHeader(schedule) }
                if (schedule != null && schedule.nodes.isNotEmpty()) {
                    schedule.nodes.forEach { node ->
                        item(key = node.id) { ScheduleItem(node) }
                    }
                } else {
                    item {
                        EmptyView(title = "还没有赛程", hint = "章程里没写赛程时不会显示")
                    }
                }
                item {
                    Text(
                        text = "时间均为北京时间；实际参赛应以组织方最新章程为准。",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                    )
                }
            }
            BottomActionButton(text = "下一步：了解参赛价值", onClick = onNext)
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
                text = "规则与赛程",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "校园数字创新挑战赛",
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

// ===== 信息分类 Tab =====
@Composable
private fun CategoryTabs(selectedTab: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color.White)
            .border(width = 0.dp, color = Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val tabs = listOf("评分细则", "比赛时间", "提交材料", "常见问题")
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Text(
                text = tab,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) PrimaryGreen else TextSecondary,
                modifier = Modifier.clickable { onSelected(tab) }
            )
        }
    }
}

// ===== 评分总览 =====
@Composable
private fun ScoringOverview(scoring: ScoringResponse?) {
    val total = scoring?.totalScore ?: 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "先理解，评委在看什么",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = if (total > 0) "总分 $total 分" else "总分以章程为准",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        if (scoring != null && scoring.dimensions.isNotEmpty()) {
            Text(
                text = "以下评分维度与分值均取自章程原文。",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

// ===== 评分维度卡片 =====
@Composable
private fun ScoringCard(dimension: ScoringDimension) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dimension.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${dimension.score} 分",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        }
        if (dimension.description.isNotBlank()) {
            Text(
                text = dimension.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        // 原文引用：契约红线要求每条评分必须能回引章程
        if (dimension.quote.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGrayBg)
                    .padding(10.dp)
            ) {
                Text(
                    text = "原文：${dimension.quote}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}

// ===== 关键时间标题 =====
@Composable
private fun ScheduleHeader(schedule: ScheduleResponse?) {
    val count = schedule?.nodes?.size ?: 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "什么时候做什么",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (count > 0) {
                Text(
                    text = "$count 个节点",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = schedule?.timezone.orEmpty().let { tz ->
                if (tz.isBlank()) "时间取自章程原文。" else "时区：$tz"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 赛程条目 =====
@Composable
private fun ScheduleItem(node: ScheduleNode) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (node.isNext) LightGreenBg else LightGrayBg)
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shortDate(node.date),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (node.isNext) PrimaryGreen else TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = node.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            val subtitle = listOf(node.time, node.description)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * ISO 8601 → 短日期（`2026-11-15T23:59:00+08:00` → `11.15`）。
 *
 * 解析失败原样返回，不吞掉信息。
 */
private fun shortDate(raw: String): String {
    if (raw.isBlank()) return ""
    return runCatching {
        val parts = raw.substringBefore('T').split('-')
        parts[1].trimStart('0') + "." + parts[2]
    }.getOrNull() ?: raw
}
@Composable
private fun BottomActionButton(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryGreen)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RulesScheduleScreenPreview() {
    RulesScheduleScreen(competitionId = "preview")
}