package com.example.competition

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val TextMuted = Color(0xFF7C7C7C)
private val PrimaryGreen = Color(0xFF246B54)
private val PageBg = Color(0xFFF5F8F6)
private val BannerBg = Color(0xFFEAF3ED)
private val CardBorder = Color(0xFFE3EAE5)
private val ToggleOffTrack = Color(0xFFD9E2DC)
private val DangerRed = Color(0xFFB34D45)

// ===== 设置页（汇总入口）=====
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onAccountSecurityClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
) {
    SettingsPageScaffold(title = "设置", subtitle = "账号、隐私与通知", onBack = onBack) {
        SettingsCard {
            SettingsRow(title = "账号与安全", onClick = onAccountSecurityClick)
            SettingsDivider()
            SettingsRow(title = "隐私设置", onClick = onPrivacyClick)
            SettingsDivider()
            SettingsRow(title = "消息通知", onClick = onNotificationClick)
            SettingsDivider()
            SettingsRow(title = "问题反馈", onClick = onFeedbackClick)
            SettingsDivider()
            SettingsRow(title = "关于我们", onClick = onAboutClick)
        }
    }
}

// ===== 账号与安全 =====
@Composable
fun AccountSecurityScreen(onBack: () -> Unit = {}) {
    SettingsPageScaffold(
        title = "账号与安全",
        subtitle = "管理登录方式，守护账号安全",
        onBack = onBack,
    ) {
        StatusBanner(
            title = "账号保护已开启",
            subtitle = "已绑定手机号，建议定期检查登录设备",
            action = "安全",
        )

        SettingsSection(title = "登录与绑定") {
            SettingsRow(title = "手机号码", subtitle = "用于登录、找回密码", value = "138 **** 8000")
            SettingsDivider()
            SettingsRow(title = "登录密码", subtitle = "定期更换密码，提升安全性", value = "已设置")
            SettingsDivider()
            SettingsRow(title = "第三方账号", subtitle = "管理微信等账号的绑定关系", value = "未绑定")
        }

        SettingsSection(title = "安全管理") {
            SettingsRow(title = "登录设备管理", subtitle = "查看并移除不常用的设备", value = "2 台")
            SettingsDivider()
            var verify by remember { mutableStateOf(true) }
            SettingsRow(
                title = "新设备登录验证",
                subtitle = "在新设备登录时进行身份验证",
                trailing = { ToggleSwitch(checked = verify, onCheckedChange = { verify = it }) },
            )
        }

        // 注销账号：无边框白色卡片，圆角 14，红字。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "注销账号",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = DangerRed,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f),
            )
            ChevronRight()
        }

        Note(text = "注销后账号及相关数据将按规则删除，请谨慎操作。")
    }
}

// ===== 隐私设置 =====
@Composable
fun PrivacySettingsScreen(onBack: () -> Unit = {}) {
    SettingsPageScaffold(
        title = "隐私设置",
        subtitle = "你的信息，由你掌控",
        onBack = onBack,
    ) {
        var phoneFind by remember { mutableStateOf(false) }
        var recommend by remember { mutableStateOf(true) }

        SettingsSection(title = "个人信息与展示") {
            SettingsRow(title = "个人主页可见范围", subtitle = "控制资料与备赛记录的展示", value = "仅自己")
            SettingsDivider()
            SettingsRow(
                title = "允许通过手机号找到我",
                subtitle = "关闭后，他人无法通过手机号搜索你",
                trailing = { ToggleSwitch(checked = phoneFind, onCheckedChange = { phoneFind = it }) },
            )
            SettingsDivider()
            SettingsRow(
                title = "个性化赛事推荐",
                subtitle = "根据浏览与关注记录推荐相关赛事",
                trailing = { ToggleSwitch(checked = recommend, onCheckedChange = { recommend = it }) },
            )
        }

        SettingsSection(title = "系统权限") {
            SettingsRow(title = "相册权限", subtitle = "用于上传头像和反馈截图", value = "使用时询问")
            SettingsDivider()
            SettingsRow(title = "相机权限", subtitle = "用于拍摄并上传图片", value = "未开启")
            SettingsDivider()
            SettingsRow(title = "位置权限", subtitle = "用于推荐附近的赛事活动", value = "未开启")
        }

        SettingsSection(title = "数据与隐私") {
            SettingsRow(title = "个人信息收集清单", value = "查看")
            SettingsDivider()
            SettingsRow(title = "隐私政策")
        }

        Note(text = "系统权限可在手机系统设置中随时调整。")
    }
}

// ===== 消息通知 =====
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit = {}) {
    SettingsPageScaffold(
        title = "消息通知",
        subtitle = "不错过重要消息，也少一些打扰",
        onBack = onBack,
    ) {
        StatusBanner(
            title = "系统通知已开启",
            subtitle = "如需调整系统权限，可前往手机设置",
            action = "去设置",
        )

        var event by remember { mutableStateOf(true) }
        var team by remember { mutableStateOf(true) }
        var system by remember { mutableStateOf(true) }
        var activity by remember { mutableStateOf(false) }

        SettingsSection(title = "通知内容") {
            SettingsRow(
                title = "赛事提醒",
                subtitle = "报名截止、比赛时间与赛程变更",
                trailing = { ToggleSwitch(checked = event, onCheckedChange = { event = it }) },
            )
            SettingsDivider()
            SettingsRow(
                title = "团队消息",
                subtitle = "入队邀请、成员动态与协作提醒",
                trailing = { ToggleSwitch(checked = team, onCheckedChange = { team = it }) },
            )
            SettingsDivider()
            SettingsRow(
                title = "系统消息",
                subtitle = "账号安全与重要服务通知",
                trailing = { ToggleSwitch(checked = system, onCheckedChange = { system = it }) },
            )
            SettingsDivider()
            SettingsRow(
                title = "活动与推荐",
                subtitle = "精选赛事、专题与活动资讯",
                trailing = { ToggleSwitch(checked = activity, onCheckedChange = { activity = it }) },
            )
        }

        var sound by remember { mutableStateOf(true) }
        var quiet by remember { mutableStateOf(true) }

        SettingsSection(title = "提醒方式") {
            SettingsRow(
                title = "声音与振动",
                subtitle = "收到通知时播放提示音并振动",
                trailing = { ToggleSwitch(checked = sound, onCheckedChange = { sound = it }) },
            )
            SettingsDivider()
            SettingsRow(
                title = "免打扰时段",
                subtitle = "开启后，指定时段内静默通知",
                trailing = { ToggleSwitch(checked = quiet, onCheckedChange = { quiet = it }) },
            )
            SettingsDivider()
            SettingsRow(title = "静默时间", value = "22:00 — 08:00")
        }

        Note(text = "免打扰期间消息仍会保留，可在消息中心查看。")
    }
}

// ===== 问题反馈 =====
@Composable
fun FeedbackScreen(onBack: () -> Unit = {}) {
    SettingsPageScaffold(
        title = "问题反馈",
        subtitle = "告诉我们你的问题和建议",
        onBack = onBack,
        contentSpacing = 20.dp,
    ) {
        val types = listOf("功能异常", "内容问题", "意见建议")
        var selectedType by remember { mutableStateOf(0) }

        SettingsSection(title = "问题类型") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEachIndexed { index, label ->
                    FeedbackTypeChip(
                        label = label,
                        selected = index == selectedType,
                        onClick = { selectedType = index },
                    )
                }
            }
        }

        SettingsSection(title = "问题描述 *") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "请描述你遇到的问题、操作步骤或改进建议，方便我们更快定位和处理。",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 19.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "0 / 500",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SettingsSection(title = "上传截图（选填，最多 3 张）") {
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .height(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clickable { },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "+",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 39.sp,
                )
                Text(
                    text = "添加图片",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                )
            }
        }

        SettingsSection(title = "联系方式（选填）") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = "请输入手机号或邮箱，便于联系你",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 19.sp,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryGreen)
                    .clickable { }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "提交反馈",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 22.sp,
                )
            }
            Text(
                text = "仅用于问题处理，我们会保护你的个人信息。",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "查看我的反馈记录",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryGreen,
            lineHeight = 19.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            textAlign = TextAlign.Center,
        )
    }
}

// ===== 关于我们 =====
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    SettingsPageScaffold(
        title = "关于我们",
        subtitle = "陪伴每一次探索与成长",
        onBack = onBack,
        contentSpacing = 22.dp,
    ) {
        // 产品介绍
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "校赛",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                lineHeight = 50.sp,
            )
            Text(
                text = "发现值得投入的比赛",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 23.sp,
            )
            Text(
                text = "v1.0.0（示例）",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 19.sp,
            )
        }

        // 我们的初心
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "让备赛更专注，让成长有迹可循",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 23.sp,
            )
            Text(
                text = "从赛事发现到组队协作，从学习计划到备赛记录，我们希望帮助每一位同学找到适合自己的比赛，认真对待每一次尝试。",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 20.sp,
            )
            Text(
                text = "发现赛事  ·  高效备赛  ·  团队协作",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
                lineHeight = 19.sp,
            )
        }

        SettingsSection(title = "了解更多") {
            SettingsCard {
                SettingsDivider()
                SettingsRow(title = "用户协议")
                SettingsDivider()
                SettingsRow(title = "隐私政策")
                SettingsDivider()
                ContactRow()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "陪伴每一次探索与成长",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 19.sp,
            )
            Text(
                text = "让每一次投入，都离成长更近。",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 17.sp,
            )
        }
    }
}

/** 「联系我们」特殊行：左侧标题，右侧两行（客服与合作 + 邮箱）。 */
@Composable
private fun ContactRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "联系我们",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextPrimary,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f),
        )
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "客服与合作",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 19.sp,
            )
            Text(
                text = "邮箱：8888888888@qq.com",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                lineHeight = 19.sp,
            )
        }
    }
}

// ===== 通用页面骨架 =====
@Composable
private fun SettingsPageScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    contentSpacing: androidx.compose.ui.unit.Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        SettingsNavigation(title = title, subtitle = subtitle, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(PageBg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            content()
        }
    }
}

// ===== 页面导航 =====
@Composable
private fun SettingsNavigation(title: String, subtitle: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = "返回",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .size(20.dp)
                .clickable { onBack() },
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 52.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 28.sp,
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 16.sp,
            )
        }
    }
}

// ===== 状态横幅 =====
@Composable
private fun StatusBanner(title: String, subtitle: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BannerBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
                lineHeight = 22.sp,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 17.sp,
            )
        }
        Text(
            text = action,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryGreen,
            lineHeight = 19.sp,
        )
    }
}

// ===== 区块 =====
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            lineHeight = 19.sp,
        )
        content()
    }
}

// ===== 白色卡片 =====
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        content = content,
    )
}

// ===== 列表行 =====
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
                lineHeight = 22.sp,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 17.sp,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 19.sp,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            ChevronRight()
        }
    }
}

// ===== 右向箭头 =====
@Composable
private fun ChevronRight() {
    Image(
        painter = painterResource(R.drawable.ic_chevron_right_dark),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
    )
}

// ===== 分隔线 =====
@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(CardBorder),
    )
}

// ===== 开关 =====
@Composable
private fun ToggleSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbOffset by animateDpAsState(if (checked) 18.dp else 2.dp, label = "toggle")
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) PrimaryGreen else ToggleOffTrack)
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 2.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
        )
    }
}

// ===== 反馈类型选项 =====
@Composable
private fun FeedbackTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(114.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) BannerBg else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) PrimaryGreen else CardBorder,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) PrimaryGreen else TextPrimary,
            lineHeight = 20.sp,
        )
    }
}

// ===== 说明文字 =====
@Composable
private fun Note(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary,
        lineHeight = 17.sp,
    )
}

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun AccountSecurityPreview() {
    AccountSecurityScreen()
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun FeedbackPreview() {
    FeedbackScreen()
}
