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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competition.data.CompetitionApp
import com.example.competition.domain.Award
import com.example.competition.domain.Participation
import com.example.competition.domain.Profile
import com.example.competition.domain.ProfileStats
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.profile.ProfileData
import com.example.competition.ui.profile.ProfileViewModel

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)
private val BorderGray = Color(0xFFE7ECE9)

/**
 * 我的页 —— 竞学（底部导航第 4 个 Tab）
 * 与 Figma 原型（node 45:728）保持一致。
 */
@Composable
fun ProfileScreen(
    onDiscoverClick: () -> Unit = {},
    onSchoolCompetitionClick: () -> Unit = {},
    onPreparationClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(app.container.repository),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onSettingsClick = onSettingsClick)
        ProfileContent(
            state = state,
            onRetry = viewModel::reload,
            onEditProfileClick = onEditProfileClick,
        )
    }
}

/** 我的页内容区：按状态分支。 */
@Composable
private fun ColumnScope.ProfileContent(
    state: LoadState<ProfileData>,
    onRetry: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
            onRetry = onRetry,
        )
        is LoadState.Success -> LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            val data = state.data
            item {
                ProfileCard(
                    profile = data.profile,
                    stats = data.stats,
                    onEditProfileClick = onEditProfileClick,
                )
            }
            item { AwardSection(awards = data.awards) }
            item { ParticipationSection(participations = data.participations) }
        }
    }
}
@Composable
private fun PageNavigation(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "我的",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "每一次投入，都成为成长的足迹",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Row(
            modifier = Modifier.clickable { onSettingsClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = "设置",
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = "设置",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

// ===== 个人资料 =====
@Composable
private fun ProfileCard(
    profile: Profile,
    stats: ProfileStats,
    onEditProfileClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightGreenBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 用户信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.avatarInitial.ifBlank {
                        profile.displayName.take(1).ifBlank { "我" }
                    },
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = profile.displayName.ifBlank { "未设置姓名" },
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 29.4.sp
                    )
                    if (profile.displayName.isBlank()) {
                        Text(
                            text = "去完善",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = schoolLine(profile),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }

        // 个人方向
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "感兴趣的方向",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                Text(
                    text = interestText(profile),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { onEditProfileClick() }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "编辑资料",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen
                )
            }
        }

        Divider()

        // 成长概览
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GrowthData(
                value = stats.competitions.toString(),
                unit = "场",
                label = "历史参赛",
                modifier = Modifier.weight(1f),
            )
            GrowthData(
                value = stats.awards.toString(),
                unit = "项",
                label = "获得奖项",
                modifier = Modifier.weight(1f),
            )
            GrowthData(
                value = stats.records.toString(),
                unit = "条",
                label = "备赛记录",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 「学校 · 专业 · 年级」一行；缺项自动省略。 */
private fun schoolLine(profile: Profile): String =
    listOf(profile.school, profile.major, profile.grade)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { "还没有填写学校与专业" }

/** 兴趣方向；为空时给引导文案。 */
private fun interestText(profile: Profile): String =
    profile.interestDirections
        .filter { it.isNotBlank() }
        .joinToString("、")
        .ifBlank { "还没有选择方向" }

@Composable
private fun GrowthData(value: String, unit: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                lineHeight = 27.6.sp
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 历史获奖（数据驱动） =====
@Composable
private fun AwardSection(awards: List<Award>) {
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
                text = "历史获奖",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = awards.size.toString() + " 项",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }

        if (awards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightGrayBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有获奖记录，第一次参赛后就会出现在这里",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                awards.forEachIndexed { index, award ->
                    if (index > 0) Divider()
                    AwardItem(
                        level = award.level,
                        time = award.time,
                        name = award.name,
                        meta = award.meta,
                    )
                }
            }
        }
    }
}

@Composable
private fun AwardItem(level: String, time: String, name: String, meta: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LightGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_trophy),
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = level,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = meta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

// ===== 历史参赛（数据驱动） =====
@Composable
private fun ParticipationSection(participations: List<Participation>) {
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
                text = "历史参赛",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = participations.size.toString() + " 场 · 按时间排序",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }

        if (participations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightGrayBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有参赛记录，从发现页挑一场比赛开始",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                participations.forEachIndexed { index, item ->
                    if (index > 0) Divider()
                    ParticipationItem(
                        name = item.name,
                        session = item.session,
                        period = periodText(item),
                        result = item.result.ifBlank { "完赛" },
                        resultHighlight = item.awarded,
                    )
                }
            }
        }
    }
}

/** 「周期 · 角色」一行；缺项自动省略。 */
private fun periodText(item: Participation): String =
    listOf(item.period, item.role)
        .filter { it.isNotBlank() }
        .joinToString(" · ")

@Composable
private fun ParticipationItem(
    name: String,
    session: String,
    period: String,
    result: String,
    resultHighlight: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = session,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
        Text(
            text = period,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = result,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = if (resultHighlight) PrimaryGreen else TextSecondary
            )
            Text(
                text = "已结束",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
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


// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen()
}