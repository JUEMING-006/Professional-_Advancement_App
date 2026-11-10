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
import com.example.competition.domain.ValueAssessmentResponse
import com.example.competition.domain.ValueDimension
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.detail.CompetitionDetailUiState
import com.example.competition.ui.detail.CompetitionDetailViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val TextMuted = Color(0xFF939D97)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

// ===== 价值维度 =====
/**
 * 参赛价值判断页 —— 竞学（第六页）
 * 与 Figma 原型（node 23:7773）保持一致。
 *
 * ⚠️ 本文件**不再定义**本地 `ValueDimension`：
 * 旧副本含 `iconRes` / `tagBackground` / `tagTextColor` 等纯 UI 字段，
 * 这些不该和契约混在一起。现在统一用 `domain/Contracts.kt` 的类型，
 * 图标与配色在渲染时按 `status` 派生。
 */
@Composable
fun ValueAssessmentScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        ValueContent(uiState = uiState, onNext = onNext)
    }
}

/** 内容区：按加载状态分支。 */
@Composable
private fun ColumnScope.ValueContent(
    uiState: CompetitionDetailUiState,
    onNext: () -> Unit,
) {
    val assessment = uiState.valueAssessment

    when {
        uiState.loading && assessment == null -> LoadingView(modifier = Modifier.weight(1f))
        uiState.errorMessage != null && assessment == null -> ErrorView(
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
                item { OverviewCard(assessment) }
                item { SectionTitle("四个维度，透明看依据") }
                if (assessment != null && assessment.dimensions.isNotEmpty()) {
                    assessment.dimensions.forEach { dimension ->
                        item(key = dimension.name) { ValueDimensionItem(dimension) }
                    }
                } else {
                    item { EmptyView(title = "还没有价值评估", hint = "需要先有赛事与岗位信息") }
                }
                item { DecisionChecklist(assessment) }
                item {
                    Text(
                        text = "依据：本赛事章程与已填写资料。未验证外部信息，不提供综合分数或获奖概率。",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted,
                    )
                }
            }
            BottomActionButton(text = "适合我的目标，选择团队岗位", onClick = onNext)
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
                text = "这场比赛，值得参加吗？",
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

// ===== 价值参考信息卡片 =====
@Composable
private fun OverviewCard(assessment: ValueAssessmentResponse?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_scan_eye),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "参赛价值参考 · 不做权威排名",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "先看目标，再看「含金量」",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 30.8.sp
        )
        val summary = assessment?.summary.orEmpty()
        if (summary.isNotBlank()) {
            Text(
                text = summary,
                fontSize = 13.sp,
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
    }
}

/**
 * 单个价值维度。
 *
 * 图标与配色按 `status` 派生 —— 契约里只有 status/name/description/source，
 * 不含任何 UI 字段（这是有意的：后端不该决定怎么渲染）。
 */
@Composable
private fun ValueDimensionItem(dimension: ValueDimension) {
    val verified = dimension.status == "verified"
    val background = if (verified) LightGreenBg else LightGrayBg
    val textColor = if (verified) PrimaryGreen else TextSecondary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(iconFor(dimension.name)),
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = dimension.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Tag(text = statusLabel(dimension.status), background = background, textColor = textColor)
        }
        if (dimension.description.isNotBlank()) {
            Text(
                text = dimension.description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        // 来源说明：契约红线要求"没有来源不允许显示"
        val sourceLabel = dimension.source?.label.orEmpty()
        if (sourceLabel.isNotBlank()) {
            Text(
                text = "来源：$sourceLabel",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )
    }
}

/** 维度名 → 图标。按关键词匹配，未知维度用通用图标。 */
private fun iconFor(name: String): Int = when {
    name.contains("组织") || name.contains("认可") -> R.drawable.ic_building_2
    name.contains("评审") || name.contains("规则") -> R.drawable.ic_clipboard_check
    name.contains("作品") || name.contains("能力") -> R.drawable.ic_folder_check
    name.contains("时间") || name.contains("投入") -> R.drawable.ic_clock_3
    else -> R.drawable.ic_clipboard_check
}

/** 状态码 → 中文标签。 */
private fun statusLabel(status: String): String = when (status) {
    "verified" -> "已验证"
    "unverified" -> "待核实"
    "partial" -> "部分可查"
    else -> "待核实"
}

// ===== 决定前清单 =====
@Composable
private fun DecisionChecklist(assessment: ValueAssessmentResponse?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "决定前，再确认两件事",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        val checklist = assessment?.checklist.orEmpty()
        if (checklist.isEmpty()) {
            Text(
                text = "□ 向学院确认，是否计入竞赛或实践学分",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = "□ 与队友确认，每周能投入的时间",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )
        } else {
            checklist.forEach { item ->
                Text(
                    text = "□ " + item.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }
    }
}
@Composable
private fun Tag(text: String, background: Color, textColor: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

// ===== 底部操作按钮 =====
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
private fun ValueAssessmentScreenPreview() {
    ValueAssessmentScreen(competitionId = "preview")
}