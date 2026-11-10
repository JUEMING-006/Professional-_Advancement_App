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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.competition.domain.Role
import com.example.competition.ui.common.EmptyView
import com.example.competition.ui.common.ErrorView
import com.example.competition.ui.common.LoadState
import com.example.competition.ui.common.LoadingView
import com.example.competition.ui.roles.RoleSelectionViewModel
import kotlinx.coroutines.launch

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val TextMuted = Color(0xFF939D97)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val BorderGray = Color(0xFFE7ECE9)

// ===== 岗位选项 =====
/**
 * 选择团队岗位页 —— 竞学（第七页）
 * 与 Figma 原型（node 23:7840）保持一致。
 *
 * ⚠️ 本文件**不再定义**本地 `RoleOption`：
 * 岗位数据来自 `GET /roles`，图标按 `iconKey` 派生。
 *
 * 这是「选岗位 → 建备赛项目 → 学课时」链路的**起点**：
 * 点确认会调 `POST /preparations` 真的创建项目。
 */
@Composable
fun RoleSelectionScreen(
    competitionId: String,
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as CompetitionApp
    val viewModel: RoleSelectionViewModel = viewModel(
        factory = RoleSelectionViewModel.Factory(app.container.repository, competitionId),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val roles = (state as? LoadState.Success)?.data.orEmpty()
    var selectedIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    var creating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        PageNavigation(onBack = onBack)
        RoleContent(
            state = state,
            roles = roles,
            selectedIndex = selectedIndex,
            onSelected = { selectedIndex = it },
            creating = creating,
            onConfirm = {
                val role = roles.getOrNull(selectedIndex) ?: return@RoleContent
                creating = true
                scope.launch {
                    val preparationId = viewModel.createPreparation(roleId = role.id)
                    creating = false
                    if (preparationId != null) onNext()
                }
            },
        )
    }
}

/** 内容区。 */
@Composable
private fun ColumnScope.RoleContent(
    state: LoadState<List<Role>>,
    roles: List<Role>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    creating: Boolean,
    onConfirm: () -> Unit,
) {
    when {
        state is LoadState.Loading -> LoadingView(modifier = Modifier.weight(1f))
        state is LoadState.Failure -> ErrorView(
            message = state.message,
            modifier = Modifier.weight(1f),
        )
        roles.isEmpty() -> EmptyView(
            title = "还没有可选岗位",
            hint = "赛事需要先配置岗位与学习路径",
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
                item { RoleIntro() }
                item { CurrentTeam() }
                item {
                    RoleOptions(
                        roles = roles,
                        selectedIndex = selectedIndex,
                        onSelected = onSelected,
                    )
                }
                item { FirstDeliverable(roles.getOrNull(selectedIndex)) }
            }
            val selectedRole = roles.getOrNull(selectedIndex)
            BottomActionButton(
                text = if (creating) "正在创建…" else "确认${selectedRole?.name ?: ""}，生成学习计划",
                onClick = onConfirm,
            )
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
                text = "找到你的团队角色",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "校园数字创新挑战赛 / 智慧校园赛道",
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

// ===== 岗位引导 =====
@Composable
private fun RoleIntro() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "你想负责哪一部分？",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 33.6.sp
        )
        Text(
            text = "选择主要分工，为你匹配专属备赛任务。",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
        Text(
            text = "岗位不限制专业，之后也可以调整。",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
        )
    }
}

// ===== 当前团队 =====
@Composable
private fun CurrentTeam() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_users_round),
            contentDescription = null,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = "拾光小队 · 校园闲置物品循环平台",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

// ===== 岗位选项 =====
@Composable
private fun RoleOptions(
    roles: List<Role>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        roles.forEachIndexed { index, role ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) LightGreenBg else Color.White)
                    .border(
                        width = 1.dp,
                        color = if (selected) PrimaryGreen else BorderGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelected(index) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(iconFor(role.iconKey)),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = role.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(
                            if (selected) R.drawable.ic_circle_check else R.drawable.ic_circle
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(if (selected) 20.dp else 19.dp)
                    )
                }
                if (role.description.isNotBlank()) {
                    Text(
                        text = role.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }
                if (role.learningFocus.isNotBlank()) {
                    Text(
                        text = "学习重点：${role.learningFocus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (selected) PrimaryGreen else TextMuted
                    )
                }
            }
        }
    }
}

/** iconKey → 图标资源。未知 key 用通用图标。 */
private fun iconFor(iconKey: String): Int = when (iconKey) {
    "lightbulb" -> R.drawable.ic_lightbulb
    "pen" -> R.drawable.ic_pen_tool
    "code" -> R.drawable.ic_code_xml
    "chart" -> R.drawable.ic_chart_no_axes_combined
    else -> R.drawable.ic_lightbulb
}
@Composable
private fun FirstDeliverable(role: Role?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "你的第一份成果",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        val focus = role?.learningFocus.orEmpty()
        val text = if (focus.isNotBlank()) {
            "从「${role?.name.orEmpty()}」出发，先完成与「$focus」相关的第一节课，" +
                "产出可直接放进作品集的成果。"
        } else {
            "选好岗位后，系统会为你生成对应的学习路径与第一份成果。"
        }
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary
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
private fun RoleSelectionScreenPreview() {
    RoleSelectionScreen(competitionId = "preview")
}