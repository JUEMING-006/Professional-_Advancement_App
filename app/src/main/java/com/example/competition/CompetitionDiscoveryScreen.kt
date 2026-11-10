package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.CompetitionSummary
import com.example.competition.ui.discovery.DiscoveryViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 赛事发现页 —— 竞学
 * 与 Figma 原型（node 23:7367）保持一致。
 */
@Composable
fun CompetitionDiscoveryScreen(
    onHotCompetitionClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDetailClick: (String) -> Unit = {},
    onAddCompetitionClick: () -> Unit = {},
    onNoticeClick: () -> Unit = {},
    onSchoolCompetitionClick: () -> Unit = {},
    onPreparationClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // 「为你发现」接后端：赛事列表来自后端，不再是写死的示例。
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: DiscoveryViewModel = viewModel(
        factory = DiscoveryViewModel.Factory(app.container.repository)
    )
    val uiState by viewModel.state.collectAsState()

    // 每次进入发现页都刷新一次：从「添加比赛」返回后必须看到刚入库的赛事。
    // 用 Unit 作 key 表示"本屏首次组合时执行一次"，不会因 state 变化反复触发。
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            SearchBox(onClick = onSearchClick)
            RecommendCard()
            QuickEntries(onHotCompetitionClick = onHotCompetitionClick)

            // 智能体入口：用户自己贴章程 → 抽取成结构化赛事。
            // 不抓取任何平台内容，规避采集合规风险（见 docs/11）。
            AddCompetitionEntry(onClick = onAddCompetitionClick)
            NoticeEntry(onClick = onNoticeClick)

            SectionTitle()
            uiState.items.firstOrNull()?.let { competition ->
                InfoCard(
                    competition = competition,
                    onClick = { onDetailClick(competition.id) },
                )
            }
            if (uiState.loading) {
                Text(
                    text = "正在加载赛事…",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                )
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                )
            }
            ReminderBanner()
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
                text = "竞学",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "让每一次参赛，都有成长",
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
    var text by remember { mutableStateOf("") }

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

        Box(modifier = Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(
                    text = "搜索赛事、专业或关键词",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = TextPrimary
                ),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryGreen)
            )
        }
    }
}

// ===== 秋季备赛推荐 =====
@Composable
private fun RecommendCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 专题标识
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Tag(text = "秋季备赛指南", background = LightGreenBg)
            Image(
                painter = painterResource(R.drawable.ic_sparkles),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }

        // 主标题
        Column {
            Text(
                text = "从一场好比赛，",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 37.8.sp
            )
            Text(
                text = "找到你的新可能",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 37.8.sp
            )
        }

        // 副标题
        Column {
            Text(
                text = "发现适合的赛事，选好团队角色，",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 20.15.sp
            )
            Text(
                text = "把每一步准备变成看得见的成长。",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 20.15.sp
            )
        }

        // 专题入口
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "寻找我的比赛 →",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            Image(
                painter = painterResource(R.drawable.ic_pagination),
                contentDescription = null,
                modifier = Modifier
                    .width(26.dp)
                    .height(6.dp)
            )
        }
    }
}

// ===== 发现入口 =====
@Composable
private fun QuickEntries(onHotCompetitionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickEntry(
            iconRes = R.drawable.ic_flame,
            title = "热门赛事",
            subtitle = "大家都在看",
            onClick = onHotCompetitionClick,
            modifier = Modifier.weight(1f)
        )
        QuickEntry(
            iconRes = R.drawable.ic_shapes,
            title = "专业分类",
            subtitle = "找到适合你的",
            modifier = Modifier.weight(1f)
        )
        QuickEntry(
            iconRes = R.drawable.ic_calendar_days,
            title = "近期截止",
            subtitle = "不错过报名",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickEntry(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

// ===== 添加比赛入口（智能体抽取）=====
@Composable
private fun AddCompetitionEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightGreenBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_sparkles),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "添加比赛",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
            Text(
                text = "粘贴章程原文，智能体帮你抽出评分与赛程",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}


// ===== 赛事通知入口（ADR-014）=====
@Composable
private fun NoticeEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightGrayBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_bell),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "赛事通知",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "各赛事官网的最新公告，点击跳转原文",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}
// ===== 区块标题 =====
@Composable
private fun SectionTitle() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "为你发现",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "换一批 ↻",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 信息卡片 =====
@Composable
private fun InfoCard(
    competition: CompetitionSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 赛事类别
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 「与你的专业匹配」需登录且已选专业才展示（契约 R11）；
            // 当前无登录态，故不显示该标签。
            if (competition.tags.isNotEmpty()) {
                Tag(text = competition.tags.first(), background = LightGreenBg)
            }
            Image(
                painter = painterResource(R.drawable.ic_bookmark),
                contentDescription = "收藏",
                modifier = Modifier.size(18.dp)
            )
        }

        // 标题
        Text(
            text = competition.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 25.2.sp
        )

        // 副标题
        Text(
            text = competition.subtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )

        // 分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )

        // 赛事摘要
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = competition.deadline.toDeadlineText(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = PrimaryGreen
            )
            Text(
                text = "查看赛事 →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

/**
 * 把截止时间渲染成「报名截止 M.dd」。
 *
 * 派生展示值由 App 算（契约 R12）：后端只给绝对值 `deadline`。
 */
private fun String.toDeadlineText(): String {
    if (isBlank()) return ""
    val datePart = substringBefore('T')
    val month = datePart.substringAfter('-').toIntOrNull() ?: return datePart
    val day = datePart.substringAfterLast('-').toIntOrNull() ?: return datePart
    return "报名截止 $month.$day"
}
// ===== 推荐提醒 =====
@Composable
private fun ReminderBanner() {    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LightGrayBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_sliders),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "完善专业和年级，让推荐更贴近你的方向。",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ===== 标签 =====
@Composable
private fun Tag(text: String, background: Color = LightGreenBg) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = PrimaryGreen,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun CompetitionDiscoveryScreenPreview() {
    CompetitionDiscoveryScreen()
}
