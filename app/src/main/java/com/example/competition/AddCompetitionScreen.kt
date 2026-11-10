package com.example.competition

import androidx.compose.foundation.background
import com.example.competition.ui.theme.BorderSubtle
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.ExtractResponse
import com.example.competition.ui.extract.ExtractViewModel
import com.example.competition.ui.theme.BrandPrimary
import com.example.competition.ui.theme.SurfaceCard
import com.example.competition.ui.theme.TextMuted
import com.example.competition.ui.theme.TextPrimary
import com.example.competition.ui.theme.TextSecondary

/**
 * 添加比赛 —— 智能体抽取入口。
 *
 * 设计思路（见 docs/11）：**不抓取任何平台内容**，由用户自己粘贴章程。
 * 这样既规避了公众号采集的合规风险，又让智能体成为产品核心而非装饰。
 *
 * ⚠️ 本屏**不使用 layerBackdrop**：液态玻璃的背景捕获与可滚动内容 +
 * 输入框组合会触发 RenderNode 递归（实测栈溢出崩溃）。
 * 这里改用实色容器，玻璃效果留给固定层的底栏。
 */
@Composable
fun AddCompetitionScreen(
    onBack: () -> Unit = {},
    onExtracted: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: ExtractViewModel = viewModel(
        factory = ExtractViewModel.Factory(app.container.repository)
    )
    val uiState by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "把赛事章程贴进来",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "从公众号、班群或学院通知里复制文字，粘贴到下面。" +
                    "智能体会抽出评分维度、赛程与团队要求，每条评分都会附上原文出处。",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
            )

            // 输入区（实色容器）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                BasicTextField(
                    value = uiState.text,
                    onValueChange = viewModel::onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    decorationBox = { inner ->
                        if (uiState.text.isEmpty()) {
                            Text(
                                text = "在此粘贴章程原文…",
                                fontSize = 13.sp,
                                color = TextMuted,
                            )
                        }
                        inner()
                    },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${uiState.text.length} 字",
                    fontSize = 11.sp,
                    color = TextMuted,
                )
            }

            // 提交按钮（实色，用主题主色）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (uiState.canSubmit) BrandPrimary else BorderSubtle)
                    .clickable(enabled = uiState.canSubmit) { viewModel.submit() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.extracting) "识别中…" else "开始识别",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            // 示例章程：用户不知道贴什么时可以一键填入，便于先看到效果。
            Text(
                text = "没有现成章程？填入示例试试 →",
                fontSize = 12.sp,
                color = BrandPrimary,
                modifier = Modifier
                    .clickable { viewModel.onTextChange(SAMPLE_CHARTER) }
                    .padding(vertical = 4.dp),
            )

            uiState.errorMessage?.let { message -> ErrorCard(message) }

            uiState.result?.let { result ->
                if (result.valid) {
                    SuccessResult(
                        result = result,
                        onDone = onExtracted,
                    )
                } else {
                    InvalidResult(result)
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(12.dp),
    ) {
        Text("出错了", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
        Spacer(Modifier.height(4.dp))
        Text(message, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun SuccessResult(
    result: ExtractResponse,
    onDone: () -> Unit,
) {
    val draft = result.draft
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("识别成功", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandPrimary)
        Text(draft.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (draft.organizer.isNotBlank()) {
            Text("主办：${draft.organizer}", fontSize = 12.sp, color = TextSecondary)
        }

        Text(
            text = "评分细则（总分 ${draft.totalScore} 分）",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        draft.scoring.forEach { dim ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(dim.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text(
                        text = "${dim.score} 分",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                    )
                }
                if (dim.quote.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "原文：${dim.quote}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp,
                    )
                }
            }
        }

        if (draft.schedule.isNotEmpty()) {
            Text("赛程", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            draft.schedule.forEach { node ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(node.title, fontSize = 12.sp, color = TextPrimary)
                    Text(node.date, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        if (result.notes.isNotBlank()) {
            Text(
                text = "模型说明：${result.notes}",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp,
            )
        }

        Spacer(Modifier.height(4.dp))

        // 已落库：返回发现页即可看到这条新赛事。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BrandPrimary)
                .clickable { onDone() }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "已保存，返回发现页",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun InvalidResult(result: ExtractResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "识别结果需要人工确认",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
        )
        Text(
            text = "下面这些问题说明模型没能从原文里找到可靠依据，建议核对章程后再试：",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
        result.errors.forEach { error ->
            Row(verticalAlignment = Alignment.Top) {
                Text("· ", fontSize = 12.sp, color = BrandPrimary)
                Text(error, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
            }
        }
        if (result.draft.scoring.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "已抽到的评分项（未通过校验）：",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            result.draft.scoring.forEach { dim ->
                Text("· ${dim.name} ${dim.score} 分", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

/**
 * 示例章程原文。
 *
 * 与后端 `MockExtractClient` 的返回内容对齐，便于在无真实章程时先跑通链路。
 */
private const val SAMPLE_CHARTER = """2026 校园数字创新挑战赛章程（节选）

一、参赛对象：全日制在校本科生、专科生；允许跨校组队。
二、团队要求：3–5 人，指导教师 0–2 人。
三、报名时间：2026年10月15日至11月15日 23:59。
四、初赛评分（总分 100 分）：
  问题洞察（30 分）：需求真实，有访谈或数据支持，场景清晰。
  方案创新（25 分）：切入点有差异化，不重复既有产品。
  完成质量（25 分）：功能可用、流程完整、细节打磨到位。
  展示表达（20 分）：演示清晰、讲述到位、答辩有说服力。
五、赛程：
  11月15日 报名截止
  11月29日 初赛作品提交
  12月05日 入围名单公布
  12月12日 决赛答辩"""

/** 顶部返回栏。 */
@Composable
private fun PageNavigation(onBack: () -> Unit) {    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = "返回",
            modifier = Modifier
                .size(20.dp)
                .clickable { onBack() },
        )
        Text(
            text = "添加比赛",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
    }
}
