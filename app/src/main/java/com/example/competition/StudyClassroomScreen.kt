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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.competition.domain.NextLesson
import com.example.competition.domain.StudyPath
import com.example.competition.ui.classroom.ClassroomData
import com.example.competition.ui.classroom.StudyClassroomViewModel
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import kotlinx.coroutines.launch

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

// ===== 学习模块 =====
/**
 * 产品策划专属学习页 —— 竞学（第八页）
 * 与 Figma 原型（node 23:7899）保持一致。
 *
 * ⚠️ 本文件**不再定义**本地 `StudyModule`：
 * 学习路径来自 `GET /preparations/{id}/study-path`，图标按模块名派生。
 *
 * 这是 P1 链路的**终点**：点「完成本课」会真的调 `POST /lessons/{id}/complete`，
 * 后端把进度汇总到模块与路径，界面刷新后能看到数字变化。
 */
@Composable
fun StudyClassroomScreen(
    preparationId: String,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: StudyClassroomViewModel = viewModel(
        factory = StudyClassroomViewModel.Factory(app.container.repository, preparationId),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val scope = rememberCoroutineScope()
    var completing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        ClassroomContent(
            state = state,
            completing = completing,
            onComplete = { lessonId ->
                completing = true
                scope.launch {
                    viewModel.completeLesson(lessonId)
                    completing = false
                }
            },
            onNext = onNext,
        )
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.ClassroomContent(
    state: LoadState<ClassroomData>,
    completing: Boolean,
    onComplete: (String) -> Unit,
    onNext: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
        )
        is LoadState.Success -> {
            val data = state.data
            if (data.path == null) {
                EmptyView(
                    title = "还没有学习路径",
                    hint = "先在备赛页选一个岗位，系统会生成路径",
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
                    item { PlanOverviewCard(data.path) }
                    item { CurrentLesson(data.current) }
                    if (data.current != null) {
                        item { PracticeCard(data.current) }
                    }
                    item { LearningPath(data.path) }
                }
                val lesson = data.current
                BottomActionButton(
                    text = when {
                        completing -> "正在提交…"
                        lesson != null -> "完成本课：${lesson.title}"
                        else -> "全部学完了"
                    },
                    enabled = lesson != null && !completing,
                    onClick = {
                        if (lesson != null) {
                            onComplete(lesson.lessonId)
                        } else {
                            onNext()
                        }
                    },
                )
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
                text = "我的备赛课堂",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "校园数字创新挑战赛 · 产品策划",
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

// ===== 计划概览卡片 =====
@Composable
private fun PlanOverviewCard(path: StudyPath) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = path.pathName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (path.tagline.isNotBlank()) {
            Text(
                text = path.tagline,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetricItem(
                value = "${path.completedLessons}/${path.totalLessons}",
                label = "已完成课时",
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                value = if (path.weeklyHours > 0) "${path.weeklyHours} 小时" else "—",
                label = "每周投入",
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                value = path.targetDate.ifBlank { "—" },
                label = "目标日期",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

// ===== 当前该学 =====
@Composable
private fun CurrentLesson(lesson: NextLesson?) {
    if (lesson == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LightGreenBg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "这条路径已经全部学完",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "去记录页把你的成果整理下来，答辩时用得上。",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Tag(text = "当前该学", background = Color.White, textColor = PrimaryGreen)
            if (lesson.durationText.isNotBlank()) {
                Text(text = lesson.durationText, fontSize = 11.sp, color = TextSecondary)
            }
        }
        Text(
            text = lesson.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        val meta = listOf(lesson.moduleName, "第 ${lesson.lessonIndex} 节")
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (meta.isNotBlank()) {
            Text(text = meta, fontSize = 12.sp, color = TextSecondary)
        }
        lesson.alignedScoring?.let { scoring ->
            Text(
                text = "对齐评分：${scoring.name} ${scoring.score} 分",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen
            )
        }
        if (lesson.topicNote.isNotBlank()) {
            Text(text = lesson.topicNote, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// ===== 练习 =====
@Composable
private fun PracticeCard(lesson: NextLesson) {
    if (lesson.deliverableText.isBlank()) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "本节要产出",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = lesson.deliverableText,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

// ===== 学习路径 =====
@Composable
private fun LearningPath(path: StudyPath) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "完整路径",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        path.modules.forEach { module ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(iconFor(module.status)),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${module.completedLessons}/${module.totalLessons} 节",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    text = statusText(module.status),
                    fontSize = 11.sp,
                    color = if (module.status == "done") PrimaryGreen else TextSecondary
                )
            }
        }
    }
}

/** 模块状态 → 图标。 */
private fun iconFor(status: String): Int = when (status) {
    "done" -> R.drawable.ic_circle_check
    "in_progress" -> R.drawable.ic_circle
    else -> R.drawable.ic_circle
}

/** 模块状态 → 中文。 */
private fun statusText(status: String): String = when (status) {
    "done" -> "已完成"
    "in_progress" -> "进行中"
    else -> "未开始"
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
private fun BottomActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
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
                .background(if (enabled) PrimaryGreen else PrimaryGreen.copy(alpha = 0.4f))
                .clickable(enabled = enabled) { onClick() },
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
private fun StudyClassroomScreenPreview() {
    StudyClassroomScreen(preparationId = "preview")
}