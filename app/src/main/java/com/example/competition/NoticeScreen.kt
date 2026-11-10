package com.example.competition

import android.content.Intent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.NoticeDto
import com.example.competition.domain.NoticeListResponse
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.notice.NoticeViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val BorderGray = Color(0xFFE7ECE9)
private val MutedGray = Color(0xFF939D97)

/**
 * 赛事通知页（ADR-014）。
 *
 * ⚠️ **设计约束**：本页只展示「标题 + 日期 + 来源站」，
 * 点击必须**跳转官网原文**（用系统浏览器）。
 * 后端不存正文，App 也不做二次渲染 —— 我们只做指路，不做转载。
 */
@Composable
fun NoticeScreen(
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val app = context.applicationContext as CompetitionApp
    val viewModel: NoticeViewModel = viewModel(
        factory = NoticeViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        NoticeNavigation(response = (state as? LoadState.Success)?.data, onBack = onBack)
        NoticeContent(
            state = state,
            onRetry = viewModel::reload,
            onOpenUrl = { url -> context.openInBrowser(url) },
        )
    }
}

/** 用系统浏览器打开原文 —— 唯一允许的「看正文」方式。 */
private fun android.content.Context.openInBrowser(url: String) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

/** 顶部导航。 */
@Composable
private fun NoticeNavigation(
    response: NoticeListResponse?,
    onBack: () -> Unit,
) {
    val unread = response?.unreadCount ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "←",
            fontSize = 20.sp,
            color = TextPrimary,
            modifier = Modifier.clickable(onClick = onBack),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "赛事通知",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = if (unread > 0) "$unread 条未读 · 来自赛事官网" else "来自赛事官网",
                fontSize = 10.sp,
                color = TextSecondary,
            )
        }
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.NoticeContent(
    state: LoadState<NoticeListResponse>,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
            onRetry = onRetry,
        )
        is LoadState.Success -> {
            val items = state.data.items
            if (items.isEmpty()) {
                EmptyView(
                    title = "还没有通知",
                    hint = "后端会定时抓取各赛事官网的公告列表",
                    modifier = Modifier.weight(1f),
                )
            } else {
                NoticeList(items = items, onOpenUrl = onOpenUrl, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 通知列表。 */
@Composable
private fun NoticeList(
    items: List<NoticeDto>,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SourceNote() }
        items.forEach { notice ->
            item(key = notice.id) {
                NoticeCard(notice = notice, onOpenUrl = onOpenUrl)
            }
        }
    }
}

/** 来源说明：让用户清楚这些数据从哪来、怎么看正文。 */
@Composable
private fun SourceNote() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGreenBg)
            .padding(14.dp),
    ) {
        Text(
            text = "以下为各赛事官网发布的公告标题，点击后跳转官网查看原文。" +
                "本应用只聚合标题与链接，不转载正文。",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp,
        )
    }
}

/** 单条通知卡片。 */
@Composable
private fun NoticeCard(
    notice: NoticeDto,
    onOpenUrl: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, BorderGray, RoundedCornerShape(14.dp))
            .clickable { onOpenUrl(notice.url) }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = notice.competitionName.ifBlank { "赛事通知" },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
            )
            Text(
                text = notice.publishedDate.ifBlank { "" },
                fontSize = 10.sp,
                color = MutedGray,
            )
        }
        Text(
            text = notice.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            lineHeight = 20.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = notice.sourceHost,
                fontSize = 10.sp,
                color = MutedGray,
            )
            Text(
                text = "查看原文 →",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun NoticeScreenPreview() {
    NoticeScreen()
}
