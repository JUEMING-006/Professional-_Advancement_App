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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.competition.domain.Deliverable
import com.example.competition.domain.Record
import com.example.competition.domain.RelatedLesson
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.records.RecordDetailUiState
import com.example.competition.ui.records.RecordDetailViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

// ===== 访谈问题 =====
/**
 * 回看备赛记录详情页 —— 竞学（第十页）
 * 与 Figma 原型（node 23:8062）保持一致。
 *
 * 数据来源：`GET /records/{id}`。所有字段来自后端，不再硬编码。
 */
@Composable
fun RecordDetailScreen(
    recordId: String,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: RecordDetailViewModel = viewModel(
        factory = RecordDetailViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(recordId) { viewModel.load(recordId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        RecordContent(
            state = state,
            onRetry = { viewModel.load(recordId, force = true) },
            onNext = onNext,
        )
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.RecordContent(
    state: RecordDetailUiState,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    val record = state.record

    when {
        state.loading && record == null -> LoadingView(modifier = Modifier.weight(1f))
        state.errorMessage != null && record == null -> ErrorView(
            message = state.errorMessage,
            modifier = Modifier.weight(1f),
            onRetry = onRetry,
        )
        record == null -> EmptyView(
            title = "记录不存在",
            hint = "可能已被删除",
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
                item { RecordTitle(record) }
                if (record.relatedLesson != null) {
                    item { RelatedLesson(record.relatedLesson) }
                }
                item { PracticeSummary(record) }
                if (record.deliverable != null) {
                    item { DeliverableCard(record.deliverable) }
                }
                item { FooterNote() }
            }
            BottomActionButton(text = "继续备赛", onClick = onNext)
        }
    }
}

/** 页脚说明。 */
@Composable
private fun FooterNote() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "已保存 · 仅自己可见",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
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
                text = "记录详情",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "学习与备赛记录 / 备赛实践",
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

// ===== 记录标题 =====
@Composable
private fun RecordTitle(record: Record) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Tag(text = typeLabel(record.type), background = LightGreenBg, textColor = PrimaryGreen)
            if (record.durationMinutes > 0) {
                Text(
                    text = "${record.durationMinutes} 分钟",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = record.title,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 32.sp
        )
        Text(
            text = record.occurredAt,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

/** 记录类型 → 中文。 */
private fun typeLabel(type: String): String = when (type) {
    "practice" -> "备赛实践"
    "deliverable" -> "产出成果"
    "lesson" -> "课程学习"
    "reflection" -> "反思"
    else -> "记录"
}

// ===== 关联课时 =====
@Composable
private fun RelatedLesson(lesson: RelatedLesson) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "对应课时",
            fontSize = 11.sp,
            color = TextSecondary
        )
        Text(
            text = lesson.moduleName.ifBlank { "课程" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (lesson.lessonIndex > 0) {
            Text(
                text = "第 ${lesson.lessonIndex} 节",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

// ===== 实践小结 =====
@Composable
private fun PracticeSummary(record: Record) {
    if (record.summary.isBlank()) {
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "做了什么",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = record.summary,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

// ===== 产出成果 =====
@Composable
private fun DeliverableCard(deliverable: Deliverable) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "产出成果",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (deliverable.version.isNotBlank()) {
                Text(text = deliverable.version, fontSize = 11.sp, color = TextSecondary)
            }
        }
        Text(
            text = deliverable.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        deliverable.items.forEachIndexed { index, item ->
            Text(
                text = "${index + 1}. ${item.text}",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
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
private fun RecordDetailScreenPreview() {
    RecordDetailScreen(recordId = "preview")
}