package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val DangerRed = Color(0xFFB3261E)
private val WarnYellow = Color(0xFFDFE07F)
private val CalmGreen = Color(0xFF286C56)

/**
 * 近期截止页 —— 竞学。
 *
 * 由首页「近期截止」快捷入口进入。按截止时间倒序展示报名提醒，
 * 倒计时徽章按紧急程度分级（≤3 天红 / 4-6 天黄 / ≥7 天绿）。
 */
@Composable
fun RecentDeadlineScreen(
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        DeadlineNavigation(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            SummaryBar()
            Spacer(modifier = Modifier.height(29.dp))
            WeekProgressBar()
            Spacer(modifier = Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                deadlineItems.forEach { item ->
                    DeadlineCard(item = item)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===== 页面导航 =====
@Composable
private fun DeadlineNavigation(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(LightGrayBg)
                .clickable { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_arrow),
                contentDescription = "返回",
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "近期截止",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 80.dp),
        )
        // 右侧日历插画。
        Image(
            painter = painterResource(R.drawable.calendar),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(54.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

// ===== 汇总条 =====
@Composable
private fun SummaryBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LightGrayBg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "本周有 8 场赛事截止报名",
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "查看全部",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = PrimaryGreen,
        )
    }
}

// ===== 周进度条 =====
@Composable
private fun WeekProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(11.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(PrimaryGreen)
    )
}

// ===== 赛事卡片 =====
@Composable
private fun DeadlineCard(item: DeadlineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(LightGrayBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 24.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagChip(text = item.tag)
                Spacer(modifier = Modifier.size(width = 6.dp, height = 0.dp))
                Text(
                    text = item.organizer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                )
            }
        }
        CountdownBadge(daysLeft = item.daysLeft)
    }
}

// ===== 标签 =====
@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGreenBg)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = PrimaryGreen,
        )
    }
}

// ===== 倒计时徽章 =====
@Composable
private fun CountdownBadge(daysLeft: Int) {
    val color = when {
        daysLeft <= 3 -> DangerRed
        daysLeft <= 6 -> WarnYellow
        else -> CalmGreen
    }
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "剩余 $daysLeft 天",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

// ===== 截止数据 =====
private data class DeadlineItem(
    val name: String,
    val tag: String,
    val organizer: String,
    val daysLeft: Int,
)

private val deadlineItems = listOf(
    DeadlineItem("全国大学生数学建模竞赛", "国家级", "数学建模竞赛组委会", 2),
    DeadlineItem("校园数字创新挑战赛", "校赛", "校园数字创新组委会", 3),
    DeadlineItem("大学生创新创业训练计划", "国家级", "创新创业训练组委会", 5),
    DeadlineItem("蓝桥杯程序设计大赛", "国家级", "蓝桥杯大赛组委会", 6),
    DeadlineItem("中国国际大学生创新大赛", "国家级", "大学生创新大赛组委会", 9),
    DeadlineItem("机械创新设计大赛", "国家级", "机械创新大赛组委会", 12),
)

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun RecentDeadlineScreenPreview() {
    RecentDeadlineScreen()
}
