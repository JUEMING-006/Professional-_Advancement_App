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
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.competition.domain.NextLesson
import com.example.competition.domain.Preparation
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.preparation.PreparationData
import com.example.competition.ui.preparation.PreparationViewModel
// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 备赛页 —— 竞学（底部导航第 2 个 Tab）
 * 与 Figma 原型（node 42:390）保持一致。
 */
@Composable
fun PreparationScreen(
    onDiscoverClick: () -> Unit = {},
    onSchoolCompetitionClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onViewDetailClick: () -> Unit = {},
    onContinueLearningClick: (String) -> Unit = {},
    onRecordEntryClick: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: PreparationViewModel = viewModel(
        factory = PreparationViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val data = (state as? LoadState.Success)?.data ?: PreparationData()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(data)
        PreparationContent(
            state = state,
            onViewDetailClick = onViewDetailClick,
            onContinueLearningClick = onContinueLearningClick,
            onRecordEntryClick = onRecordEntryClick,
        )
    }
}

/** 备赛内容区：按状态分支。 */
@Composable
private fun ColumnScope.PreparationContent(
    state: LoadState<PreparationData>,
    onViewDetailClick: () -> Unit,
    onContinueLearningClick: (String) -> Unit,
    onRecordEntryClick: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
        )
        is LoadState.Success -> {
            val data = state.data
            if (data.preparation == null) {
                EmptyView(
                    title = "还没有开始备赛",
                    hint = "去发现页选一场比赛，挑一个岗位开始",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    item { CollectedSection(data.preparation, onViewDetailClick = onViewDetailClick) }
                    item {
                        LearnNextSection(
                            preparation = data.preparation,
                            nextLesson = data.nextLesson,
                            onContinueLearningClick = onContinueLearningClick,
                            onRecordEntryClick = onRecordEntryClick,
                        )
                    }
                }
            }
        }
    }
}



// ===== 页面导航 =====
@Composable
private fun PageNavigation(data: PreparationData) {
    val progress = data.preparation?.progress
    val subtitle = if (progress != null && progress.total > 0) {
        "已完成 " + progress.completed + "/" + progress.total + " 课时"
    } else {
        "从收藏到行动，把准备落在每一步"
    }

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
                text = "备赛",
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

// ===== 当前备赛项目（数据驱动） =====
@Composable
private fun CollectedSection(preparation: Preparation, onViewDetailClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    text = "我的备赛",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "进行中",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
            Text(
                text = "管理",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
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
            // 赛事状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Tag(text = statusLabel(preparation.status))
                Image(
                    painter = painterResource(R.drawable.ic_ellipsis),
                    contentDescription = "更多",
                    modifier = Modifier.size(18.dp)
                )
            }

            // 赛事信息
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = preparation.competitionName.ifBlank { "备赛项目" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (preparation.teamSizeText.isNotBlank()) {
                    Text(
                        text = preparation.teamSizeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
                if (preparation.keyDates.isNotEmpty()) {
                    Text(
                        text = preparation.keyDates.joinToString(" · "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = PrimaryGreen
                    )
                }
            }

            Divider()

            // 我的备赛
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = teamLine(preparation),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
                Text(
                    text = "查看赛事 →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = PrimaryGreen,
                    modifier = Modifier.clickable { onViewDetailClick() }
                )
            }

            // 学习进度
            LearnProgress(preparation)
        }
    }
}

/** 进度条 + 数字。 */
@Composable
private fun LearnProgress(preparation: Preparation) {
    val progress = preparation.progress
    val completed = progress?.completed ?: 0
    val total = progress?.total ?: 0
    val ratio = if (total > 0) completed.toFloat() / total else 0f

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = preparation.pathName.ifBlank { "学习路径" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
            Text(
                text = "已完成 $completed / $total 节",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = PrimaryGreen
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(BorderGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(PrimaryGreen)
            )
        }
    }
}

/** 状态 → 中文标签。 */
private fun statusLabel(status: String): String = when (status) {
    "learning" -> "正在备赛"
    "done" -> "已完赛"
    else -> "筹备中"
}

/** 「小队名 · 岗位」一行；个人赛时只显示岗位。 */
private fun teamLine(preparation: Preparation): String {
    val role = preparation.roleName
    val team = preparation.teamName
    return when {
        team.isNotBlank() && role.isNotBlank() -> "$team · $role"
        role.isNotBlank() -> role
        team.isNotBlank() -> team
        else -> "个人备赛"
    }
}

// ===== 接下来，学什么（数据驱动） =====
@Composable
private fun LearnNextSection(
    preparation: Preparation,
    nextLesson: NextLesson?,
    onContinueLearningClick: (String) -> Unit,
    onRecordEntryClick: () -> Unit,
) {
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
                text = "接下来，学什么",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        val hint = buildHint(preparation)
        if (hint.isNotBlank()) {
            Text(
                text = hint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }

        if (nextLesson == null) {
            AllDoneCard(onRecordEntryClick = onRecordEntryClick)
        } else {
            NextLessonCard(
                lesson = nextLesson,
                preparationId = preparation.id,
                onContinueLearningClick = onContinueLearningClick,
            )
        }
    }
}

/** 「赛事名 · 为岗位推荐」提示行。 */
private fun buildHint(preparation: Preparation): String {
    val competition = preparation.competitionName
    val role = preparation.roleName
    return when {
        competition.isNotBlank() && role.isNotBlank() -> competition + " · 为" + role + "推荐"
        competition.isNotBlank() -> competition
        else -> ""
    }
}

/** 下一课卡片。 */
@Composable
private fun NextLessonCard(
    lesson: NextLesson,
    preparationId: String,
    onContinueLearningClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Tag(text = lesson.moduleName.ifBlank { "推荐课程" })
            Image(
                painter = painterResource(R.drawable.ic_book_pages_green),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = lesson.title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (lesson.durationText.isNotBlank()) {
                Text(
                    text = lesson.durationText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }

        lesson.alignedScoring?.let { scoring ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "对齐评分：" + scoring.name + " " + scoring.score + " 分",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (lesson.topicNote.isNotBlank()) {
                    Text(
                        text = lesson.topicNote,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
            }
        }

        if (lesson.deliverableText.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_file_pen_line),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "交付：" + lesson.deliverableText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }

        Text(
            text = "继续学习 →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryGreen)
                .clickable { onContinueLearningClick(preparationId) }
                .padding(vertical = 11.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** 全部学完时的完成态。 */
@Composable
private fun AllDoneCard(onRecordEntryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "这门路径已经全部学完",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "把你的成果记录下来，答辩时用得上。",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        Text(
            text = "去记录 →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryGreen)
                .clickable { onRecordEntryClick() }
                .padding(horizontal = 20.dp, vertical = 11.dp),
        )
    }
}
@Composable
private fun FollowUpItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderGray)
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
private fun PreparationScreenPreview() {
    PreparationScreen()
}