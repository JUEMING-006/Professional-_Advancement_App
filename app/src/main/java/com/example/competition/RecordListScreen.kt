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
import com.example.competition.domain.Record
import com.example.competition.domain.RecordGroup
import com.example.competition.domain.RecordListResponse
import com.example.competition.domain.RecordStatistics
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.records.RecordListViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 学习与备赛记录页 —— 竞学（第九页）
 * 与 Figma 原型（node 23:7978）保持一致。
 * 说明：本页为「记录」Tab 顶级页，无返回按钮；底部导航「发现」回到发现页。
 *
 * 数据来源：`GET /api/v1/records`。加载中显示占位，失败显示重试。
 * 列表用 LazyColumn：记录数量会随用户使用增长，固定 Column 会溢出屏幕。
 */
@Composable
fun RecordListScreen(
    onDiscoverClick: () -> Unit = {},
    onSchoolCompetitionClick: () -> Unit = {},
    onRecordDetailClick: () -> Unit = {},
    onPreparationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: RecordListViewModel = viewModel(
        factory = RecordListViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val response = (state as? LoadState.Success)?.data

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(response)
        RecordContent(
            state = state,
            onRetry = viewModel::reload,
            onRecordDetailClick = onRecordDetailClick,
        )
    }
}

/** 记录内容区：按状态分支渲染。 */
@Composable
private fun ColumnScope.RecordContent(
    state: LoadState<RecordListResponse>,
    onRetry: () -> Unit,
    onRecordDetailClick: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
            onRetry = onRetry,
        )
        is LoadState.Success -> RecordList(
            response = state.data,
            onRecordDetailClick = onRecordDetailClick,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 记录列表主体：LazyColumn 承载可增长的记录流。 */
@Composable
private fun RecordList(
    response: RecordListResponse,
    onRecordDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = response.groups
    val statistics = response.statistics

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item { GrowthStats(statistics) }
        item { CurrentProject(response) }
        item { RecordTypeTabs() }

        if (groups.isEmpty()) {
            item { EmptyView(title = "还没有记录", hint = "去学习一节课，这里会自动出现记录") }
        } else {
            groups.forEach { group ->
                item(key = "group-" + group.date) {
                    RecordGroupSection(group = group, onRecordDetailClick = onRecordDetailClick)
                }
            }
        }
    }
}




// ===== 页面导航（无返回按钮） =====
@Composable
private fun PageNavigation(response: RecordListResponse?) {
    val total = response?.statistics?.totalRecords ?: 0
    val subtitle = if (total > 0) "已积累 $total 条记录" else "还没有记录"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "每一步，都算数",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
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

// ===== 累计学习（数据驱动） =====
@Composable
private fun GrowthStats(statistics: RecordStatistics?) {
    val recordCount = statistics?.totalRecords ?: 0
    val minutes = statistics?.totalMinutes ?: 0
    val deliverables = statistics?.deliverableCount ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GrowthStat(value = recordCount.toString(), label = "累计记录", modifier = Modifier.weight(1f))
        GrowthStat(value = "$minutes 分钟", label = "学习与实践", modifier = Modifier.weight(1f))
        GrowthStat(value = "$deliverables 份", label = "产出成果", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GrowthStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
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

// ===== 当前备赛项目（数据驱动） =====
@Composable
private fun CurrentProject(response: RecordListResponse?) {
    val latest = response?.groups?.firstOrNull()?.items?.firstOrNull()
    val relationText = latest?.relatedLesson?.moduleName.orEmpty()
    val title = if (latest != null) latest.title else "还没有进行中的备赛"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "最近一次",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 25.2.sp
        )
        if (relationText.isNotBlank()) {
            Text(
                text = relationText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

// ===== 记录类型 =====
@Composable
private fun RecordTypeTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tag(text = "全部记录", background = LightGreenBg, textColor = PrimaryGreen)
        Tag(text = "学习笔记", background = LightGrayBg, textColor = TextSecondary)
        Tag(text = "备赛实践", background = LightGrayBg, textColor = TextSecondary)
        Tag(text = "成果", background = LightGrayBg, textColor = TextSecondary)
    }
}

// ===== 记录分组（数据驱动） =====
@Composable
private fun RecordGroupSection(group: RecordGroup, onRecordDetailClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = group.date,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "共 " + group.count + " 条",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }

        group.items.forEach { record ->
            RecordCard(record = record, onClick = onRecordDetailClick)
        }
    }
}

/** 单条记录卡片。 */
@Composable
private fun RecordCard(record: Record, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Tag(text = typeLabel(record.type), background = LightGreenBg, textColor = PrimaryGreen)
            Text(
                text = record.durationMinutes.let { if (it > 0) "$it 分钟" else "" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Text(
            text = record.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (record.summary.isNotBlank()) {
            Text(
                text = record.summary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

/** 记录类型 → 中文标签。 */
private fun typeLabel(type: String): String = when (type) {
    "practice" -> "备赛实践"
    "deliverable" -> "产出成果"
    "lesson" -> "课程学习"
    else -> "记录"
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


// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RecordListScreenPreview() {
    RecordListScreen()
}