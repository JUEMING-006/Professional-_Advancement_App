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
import com.example.competition.domain.CompetitionDetail
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

// ===== 信息行 =====
private data class CompetitionDetailInfoRow(
    val label: String,
    val value: String
)

/**
 * 赛事详情页 —— 竞学（第四页）
 * 与 Figma 原型（node 23:7598）保持一致。
 */
@Composable
fun CompetitionDetailScreen(
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
        DetailContent(
            uiState = uiState,
            onNext = onNext,
        )
    }
}

/** 详情内容区：按加载状态分支。 */
@Composable
private fun ColumnScope.DetailContent(
    uiState: CompetitionDetailUiState,
    onNext: () -> Unit,
) {
    val detail = uiState.detail

    when {
        uiState.loading && detail == null -> LoadingView(modifier = Modifier.weight(1f))
        uiState.errorMessage != null && detail == null -> ErrorView(
            message = uiState.errorMessage,
            modifier = Modifier.weight(1f),
        )
        detail == null -> EmptyView(
            title = "赛事不存在",
            hint = "可能已被删除，返回上一页重新选择",
            modifier = Modifier.weight(1f),
        )
        else -> {
            // 顺序很重要：**先放按钮再放列表**会让按钮贴顶，
            // 所以这里用「列表 weight(1f) + 按钮固定高度」的经典布局。
            // ⚠️ 按钮不能再加 navigationBarsPadding —— 外层 Column 已有 statusBarsPadding，
            // 底部安全区由 MainActivity 的玻璃底栏负责，重复加会把按钮推出屏幕（已实测）。
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item { OverviewSection(detail) }
                item { KeyInfoBar(detail) }
                item {
                    InfoSection(
                        title = "参赛前，先了解",
                        rows = buildInfoRows(detail),
                    )
                }
                item { DividerLine() }
                item { TrackSection(detail) }
                item { DeliverableCard(detail) }
                item {
                    InfoSection(
                        title = "报名与奖项",
                        rows = buildAwardRows(detail),
                    )
                }
                item { SourceDisclaimer(detail) }
            }
            BottomActionButton(text = "查看评分细则与赛程", onClick = onNext)
        }
    }
}

/** 参赛须知行：全部来自后端字段，缺失项自动省略。 */
private fun buildInfoRows(detail: CompetitionDetail): List<CompetitionDetailInfoRow> {
    val rows = mutableListOf<CompetitionDetailInfoRow>()
    if (detail.eligibility.isNotBlank()) {
        rows += CompetitionDetailInfoRow("参赛对象", detail.eligibility)
    }
    if (detail.format.isNotBlank()) {
        rows += CompetitionDetailInfoRow("比赛形式", detail.format)
    }
    val registration = registrationText(detail)
    if (registration.isNotBlank()) {
        rows += CompetitionDetailInfoRow("报名时间", registration)
    }
    val team = teamRequirementText(detail)
    if (team.isNotBlank()) {
        rows += CompetitionDetailInfoRow("团队要求", team)
    }
    if (detail.organizer.isNotBlank()) {
        rows += CompetitionDetailInfoRow("组织方", detail.organizer)
    }
    return rows
}

/** 报名与奖项行。 */
private fun buildAwardRows(detail: CompetitionDetail): List<CompetitionDetailInfoRow> {
    val rows = mutableListOf<CompetitionDetailInfoRow>()
    if (detail.feeText.isNotBlank()) {
        rows += CompetitionDetailInfoRow("参赛费用", detail.feeText)
    }
    if (detail.awardsDescription.isNotBlank()) {
        rows += CompetitionDetailInfoRow("奖项设置", detail.awardsDescription)
    }
    return rows
}

/** 报名时间展示：起止都有则拼区间，日期做短格式。 */
private fun registrationText(detail: CompetitionDetail): String {
    val start = shortDate(detail.registrationStart)
    val end = shortDate(detail.registrationEnd)
    return when {
        start.isNotBlank() && end.isNotBlank() -> "$start — $end"
        end.isNotBlank() -> "截止 $end"
        detail.registrationDeadlineText.isNotBlank() -> detail.registrationDeadlineText
        else -> ""
    }
}

/**
 * ISO 8601 → 短日期（`2026-11-15T23:59:00+08:00` → `2026.11.15`）。
 *
 * 解析失败原样返回，不吞掉信息。
 */
private fun shortDate(raw: String): String {
    if (raw.isBlank()) return ""
    return runCatching {
        val parts = raw.substringBefore('T').split('-')
        parts[0] + "." + parts[1] + "." + parts[2]
    }.getOrNull() ?: raw
}

/** 团队要求展示：由 min/maxMembers 与指导教师数派生。 */
private fun teamRequirementText(detail: CompetitionDetail): String {
    val min = detail.minMembers ?: 0
    val max = detail.maxMembers ?: 0
    val team = when {
        max <= 1 -> "个人赛"
        min == max -> "$max 人"
        else -> "$min–$max 人"
    }
    val advisorMin = detail.advisorCountMin ?: 0
    val advisorMax = detail.advisorCountMax ?: 0
    val advisor = when {
        advisorMax <= 0 -> ""
        advisorMin == advisorMax -> "指导教师 $advisorMax 人"
        else -> "指导教师 $advisorMin–$advisorMax 人"
    }
    return listOf(team, advisor).filter { it.isNotBlank() }.joinToString("，")
}

/** 来源与免责说明：必须标明数据来源，避免用户误以为是官方承诺。 */
@Composable
private fun SourceDisclaimer(detail: CompetitionDetail) {
    val sourceLabel = detail.source?.label.orEmpty()
    val text = if (sourceLabel.isNotBlank()) {
        "信息来源：$sourceLabel。报名与获奖以主办方通知为准。"
    } else {
        "报名与获奖以主办方通知为准。"
    }
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary,
    )
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
                text = "赛事详情",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "信息技术 / 智慧校园",
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

// ===== 赛事概览 =====
@Composable
private fun OverviewSection(detail: CompetitionDetail) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Tag(
                text = statusText(detail.status),
                background = LightGreenBg,
                textColor = PrimaryGreen,
            )
            if (detail.isSample) {
                Tag(text = "示例赛事", background = LightGrayBg, textColor = TextSecondary)
            }
        }
        Text(
            text = detail.name,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 35.sp
        )
        if (detail.subtitle.isNotBlank()) {
            Text(
                text = detail.subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

/** 状态码 → 中文。 */
private fun statusText(status: String): String = when (status) {
    "open", "registering" -> "报名中"
    "upcoming" -> "即将报名"
    "closed", "ended" -> "报名结束"
    "running" -> "进行中"
    else -> "赛事"
}

// ===== 核心信息 =====
@Composable
private fun KeyInfoBar(detail: CompetitionDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        KeyMetric(value = teamSizeText(detail), label = "团队规模", modifier = Modifier.weight(1f))
        KeyMetric(value = feeText(detail), label = "参赛费用", modifier = Modifier.weight(1f))
        KeyMetric(value = deadlineText(detail), label = "报名截止", modifier = Modifier.weight(1f))
    }
}

/** 团队规模展示。 */
private fun teamSizeText(detail: CompetitionDetail): String {
    val min = detail.minMembers ?: 0
    val max = detail.maxMembers ?: 0
    return when {
        max <= 1 -> "个人赛"
        min == max -> "$max 人"
        min > 0 -> "$min–$max 人"
        else -> "不限"
    }
}

/** 参赛费用展示。 */
private fun feeText(detail: CompetitionDetail): String = when {
    detail.feeText.isNotBlank() -> detail.feeText
    detail.fee == 0 -> "免费"
    detail.fee != null && detail.fee > 0 -> "¥${detail.fee}"
    else -> "以通知为准"
}

/**
 * 报名截止展示。
 *
 * 后端返回 ISO 8601（如 `2026-11-15T23:59:00+08:00`），这里只取月.日。
 * 解析失败时原样返回，不吞掉信息。
 */
private fun deadlineText(detail: CompetitionDetail): String {
    val raw = detail.registrationEnd
    if (raw.isBlank()) return "以通知为准"
    val short = runCatching {
        val parts = raw.substringBefore('T').split('-')
        parts[1].trimStart('0') + "." + parts[2]
    }.getOrNull()
    return short?.takeIf { it.isNotBlank() } ?: raw
}
@Composable
private fun KeyMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = value,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 信息区块 =====
@Composable
private fun InfoSection(title: String, rows: List<CompetitionDetailInfoRow>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = row.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    modifier = Modifier.width(72.dp)
                )
                Text(
                    text = row.value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ===== 赛道说明 =====
@Composable
private fun TrackSection(detail: CompetitionDetail) {
    val trackName = detail.trackName.ifBlank { detail.categoryName }
    if (trackName.isBlank() && detail.trackDescription.isBlank() && detail.trackTags.isEmpty()) {
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (trackName.isNotBlank()) {
            Text(
                text = trackName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        if (detail.trackDescription.isNotBlank()) {
            Text(
                text = detail.trackDescription,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )
        }
        if (detail.trackTags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.trackTags.forEach { tag ->
                    Tag(text = tag, background = LightGrayBg, textColor = TextSecondary)
                }
            }
        }
    }
}

// ===== 需要交付什么 =====
@Composable
private fun DeliverableCard(detail: CompetitionDetail) {
    if (detail.deliverables.isEmpty() && detail.noticeDocumentUrl.isBlank()) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "需要交付什么",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (detail.deliverables.isNotEmpty()) {
            detail.deliverables.forEach { item ->
                Text(
                    text = "· " + item,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }
        if (detail.noticeDocumentUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderGray)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_file_text),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "查看赛事通知原文",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(R.drawable.ic_arrow_up_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ===== 分隔线 =====
@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderGray)
    )
}

// ===== 标签 =====
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
private fun CompetitionDetailScreenPreview() {
    CompetitionDetailScreen(competitionId = "preview")
}