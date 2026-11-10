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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.CompetitionSummary
import com.example.competition.domain.Profile
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.search.SearchProfileData
import com.example.competition.ui.search.SearchProfileViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 搜索与匹配页 —— 竞学（第三页）
 * 与 Figma 原型（node 23:7525）保持一致。
 * 说明：「与你的专业匹配」标签不生成，用「示例赛事」替代。
 */
@Composable
fun SearchProfileScreen(
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: SearchProfileViewModel = viewModel(
        factory = SearchProfileViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        SearchContent(
            state = state,
            viewModel = viewModel,
            onNext = onNext,
        )
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.SearchContent(
    state: LoadState<SearchProfileData>,
    viewModel: SearchProfileViewModel,
    onNext: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(message = state.message, modifier = Modifier.weight(1f))
        is LoadState.Success -> {
            val data = state.data
            val top = data.candidates.firstOrNull()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item { SearchBox() }
                item { KeywordSuggestions(data.profile) }
                item { ProfileCard(data.profile) }
                if (top == null) {
                    item { EmptyView(title = "暂时没有匹配的赛事", hint = "去发现页添加比赛后再来") }
                } else {
                    item { ResultHeader(data.candidates.size) }
                    item { ResultCard(competition = top, onClick = onNext) }
                    item { MatchReason(viewModel.matchReason(data.profile, top)) }
                }
            }
            if (top != null) {
                BottomActionButton(text = "查看匹配赛事详情", onClick = onNext)
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
                text = "搜索与匹配",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "关键词找赛事，个人资料帮你选",
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

// ===== 搜索框（绿色描边，已输入） =====
@Composable
private fun SearchBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGrayBg)
            .border(1.dp, PrimaryGreen, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "校园数字创新",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(R.drawable.ic_x),
            contentDescription = "清除",
            modifier = Modifier.size(16.dp)
        )
    }
}

// ===== 关键词建议 =====
@Composable
private fun KeywordSuggestions(profile: Profile) {
    val interests = profile.interestDirections.filter { it.isNotBlank() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "试试", fontSize = 12.sp, color = TextSecondary)
        if (interests.isEmpty()) {
            Text(text = "先去「我的」填写兴趣方向", fontSize = 12.sp, color = TextSecondary)
        } else {
            interests.take(3).forEach { interest ->
                Text(
                    text = interest,
                    fontSize = 11.sp,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGreenBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// ===== 我的画像 =====
@Composable
private fun ProfileField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
@Composable
private fun ProfileCard(profile: Profile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "我的画像",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileField(
                label = "学校",
                value = profile.school.ifBlank { "未填写" },
                modifier = Modifier.weight(1f),
            )
            ProfileField(
                label = "专业",
                value = profile.major.ifBlank { "未填写" },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileField(
                label = "年级",
                value = profile.grade.ifBlank { "未填写" },
                modifier = Modifier.weight(1f),
            )
            ProfileField(
                label = "兴趣方向",
                value = profile.interestDirections.filter { it.isNotBlank() }
                    .joinToString("、")
                    .ifBlank { "未填写" },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ===== 结果标题 =====
@Composable
private fun ResultHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "搜索结果",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(text = "$count 场", fontSize = 12.sp, color = TextSecondary)
    }
}

// ===== 结果卡片 =====
@Composable
private fun ResultCard(
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = competition.name,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        val subtitle = listOf(competition.trackName, competition.teamSizeText)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        if (competition.organizerName.isNotBlank()) {
            Text(
                text = "主办：${competition.organizerName}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

// ===== 匹配理由 =====
@Composable
private fun MatchReason(reason: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "为什么推荐这场",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = reason,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
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
private fun SearchProfileScreenPreview() {
    SearchProfileScreen()
}