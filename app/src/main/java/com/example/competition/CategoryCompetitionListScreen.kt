package com.example.competition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val LightGreenBg = Color(0xFFEDF5F0)
private val CardBorder = Color(0xFFE0E8E3)
private val NavBg = Color(0xFFF6F8F6)

/**
 * 专业分类赛事列表页 —— 竞学。
 *
 * 由「专业分类」页点击某个专业卡片进入。顶部给该专业的方向概览，
 * 下方列出该专业的精选赛项。5 个专业共用一套布局，仅数据与配色不同。
 */
@Composable
fun CategoryCompetitionListScreen(
    categoryKey: String,
    onBack: () -> Unit = {},
) {
    val detail = categoryDetails.firstOrNull { it.key == categoryKey }
        ?: categoryDetails.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        CategoryListNavigation(onBack = onBack, title = detail.navTitle)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            OverviewCard(detail = detail)
            Spacer(modifier = Modifier.height(24.dp))
            SelectedHeader()
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                detail.competitions.forEach { item ->
                    CompetitionCard(item = item, accent = detail.accent)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            InfoNote()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===== 页面导航 =====
@Composable
private fun CategoryListNavigation(onBack: () -> Unit, title: String) {
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
                .background(NavBg)
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
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 80.dp),
        )
    }
}

// ===== 方向概览卡 =====
@Composable
private fun OverviewCard(detail: CategoryDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(detail.bgColor)
            .padding(start = 18.dp, end = 12.dp, top = 18.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = detail.breadcrumb,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = detail.accent,
                lineHeight = 16.sp,
            )
            Text(
                text = detail.name,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 43.sp,
            )
            Text(
                text = detail.subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = detail.accent,
                lineHeight = 18.sp,
            )
            Text(
                text = detail.description,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 18.sp,
            )
        }
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassIconChip(
                iconRes = detail.iconRes,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

// ===== 玻璃图标底 =====
@Composable
private fun GlassIconChip(
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        LightGreenBg.copy(alpha = 0.2f),
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ===== 精选赛项标题 =====
@Composable
private fun SelectedHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "精选赛项",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 29.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "3 项 · 原有优先",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
    }
}

// ===== 赛事卡片 =====
@Composable
private fun CompetitionCard(item: CategoryCompetitionItem, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                lineHeight = 16.sp,
            )
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 25.sp,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            InfoRow(label = "含金量", value = item.value, valueColor = accent, valueWeight = FontWeight.Medium)
            InfoRow(label = "时间", value = item.time, valueColor = TextPrimary)
            InfoRow(label = "适合群体", value = item.group, valueColor = TextPrimary)
        }
    }
}

// ===== 简要信息行 =====
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color,
    valueWeight: FontWeight = FontWeight.Normal,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 17.sp,
            modifier = Modifier.width(48.dp),
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = valueWeight,
            color = valueColor,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

// ===== 信息说明 =====
@Composable
private fun InfoNote() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "含金量为参赛价值参考，非权威排名。",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
        Text(
            text = "示例赛项沿用原页设定；其余赛程及资格以官方章程为准。",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 16.sp,
        )
    }
}

// ===== 数据 =====
private data class CategoryCompetitionItem(
    val tag: String,
    val name: String,
    val value: String,
    val time: String,
    val group: String,
)

private data class CategoryDetail(
    val key: String,
    val navTitle: String,
    val breadcrumb: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val accent: Color,
    val bgColor: Color,
    val iconRes: Int,
    val competitions: List<CategoryCompetitionItem>,
)

private val categoryDetails = listOf(
    CategoryDetail(
        key = "it",
        navTitle = "信息技术赛事",
        breadcrumb = "专业分类 / 信息技术",
        name = "信息技术",
        subtitle = "计算机科学与技术 · 软件与应用",
        description = "用技术解决问题，让创意真正落地。",
        accent = Color(0xFF246B54),
        bgColor = Color(0xFFEDF7F7),
        iconRes = R.drawable.ic_cat_it,
        competitions = listOf(
            CategoryCompetitionItem("原有赛项 · 示例", "校园数字创新挑战赛", "项目实践 · 调研、原型与计划书", "2026.11.15 报名截止", "在校本科、专科生 · 3–5 人组队"),
            CategoryCompetitionItem("原有赛项 · 示例", "高校开源应用创作赛", "开发实践 · 积累开源应用作品", "11.22 报名截止（原页设定）", "适合应用开发方向 · 2–4 人组队"),
            CategoryCompetitionItem("原有赛项", "蓝桥杯程序设计大赛", "算法训练 · 积累竞赛解题经验", "本届报名与比赛时间待核实", "适合计算机、软件及算法爱好者"),
        ),
    ),
    CategoryDetail(
        key = "business",
        navTitle = "经管商科赛事",
        breadcrumb = "专业分类 / 经管商科",
        name = "经管商科",
        subtitle = "商业分析 · 创新创业 · 经营决策",
        description = "从商业洞察出发，验证你的新想法。",
        accent = Color(0xFF76611E),
        bgColor = Color(0xFFF8F5DF),
        iconRes = R.drawable.ic_cat_business,
        competitions = listOf(
            CategoryCompetitionItem("原有赛项 · 示例", "商业经营沙盘模拟赛", "经营实践 · 锻炼团队决策能力", "2026.10.10—10.30 报名（已结束）", "本院学生 · 4–6 人组队"),
            CategoryCompetitionItem("原有赛项", "中国国际大学生创新大赛", "创新实践 · 商业方案与项目路演", "本届报名与比赛时间待核实", "适合创新创业团队 · 资格以章程为准"),
            CategoryCompetitionItem("补充赛项", "全国大学生市场调查与分析大赛", "调研实践 · 数据分析与报告表达", "本届报名与比赛时间待核实", "适合经管、统计及市场调研方向"),
        ),
    ),
    CategoryDetail(
        key = "design",
        navTitle = "设计艺术赛事",
        breadcrumb = "专业分类 / 设计艺术",
        name = "设计艺术",
        subtitle = "产品设计 · 视觉表达 · 创意实践",
        description = "把灵感变成作品，让表达更有力量。",
        accent = Color(0xFF895761),
        bgColor = Color(0xFFF8EDF0),
        iconRes = R.drawable.ic_cat_design,
        competitions = listOf(
            CategoryCompetitionItem("原有赛项 · 历史示例", "校园公益产品设计赛", "作品积累 · 产品策划与用户研究", "2026.04—06（历史参赛记录）", "适合产品、交互及用户研究方向"),
            CategoryCompetitionItem("原有赛项 · 历史示例", "新生创新创意赛", "入门实践 · 从创意到方案表达", "2025.10（历史参赛记录）", "适合新生及希望尝试方案设计的同学"),
            CategoryCompetitionItem("补充赛项", "全国大学生广告艺术大赛", "创意表达 · 丰富广告与视觉作品", "本届报名与比赛时间待核实", "适合视觉传达、广告及数字媒体方向"),
        ),
    ),
    CategoryDetail(
        key = "engineering",
        navTitle = "理工制造赛事",
        breadcrumb = "专业分类 / 理工制造",
        name = "理工制造",
        subtitle = "机械结构 · 工程设计 · 数学建模",
        description = "连接理论与实践，做出可验证的方案。",
        accent = Color(0xFF4C647B),
        bgColor = Color(0xFFEDF0F4),
        iconRes = R.drawable.ic_cat_engineering,
        competitions = listOf(
            CategoryCompetitionItem("原有赛项 · 示例", "智能结构设计挑战赛", "工程实践 · 验证结构设计方案", "2026.11.05—11.20 报名", "本院学生 · 2–4 人组队"),
            CategoryCompetitionItem("原有赛项", "机械创新设计大赛", "工程创新 · 结构设计与实物制作", "本届报名与比赛时间待核实", "适合机械、机电及工业设计方向"),
            CategoryCompetitionItem("原有赛项", "全国大学生数学建模竞赛", "建模实践 · 数学、编程与论文表达", "本届报名与比赛时间待核实", "适合数学、统计及编程基础较好的同学"),
        ),
    ),
    CategoryDetail(
        key = "humanities",
        navTitle = "人文社科赛事",
        breadcrumb = "专业分类 / 人文社科",
        name = "人文社科",
        subtitle = "社会观察 · 语言表达 · 学术研究",
        description = "观察真实生活，让思考被更多人看见。",
        accent = Color(0xFF885D42),
        bgColor = Color(0xFFF8EEE7),
        iconRes = R.drawable.ic_cat_humanities,
        competitions = listOf(
            CategoryCompetitionItem("原有赛项 · 示例", "校园原创音乐大赛", "艺术实践 · 原创表达与舞台展示", "2026.10.28—11.12 报名", "全校学生 · 个人或团队"),
            CategoryCompetitionItem("补充赛项", "“外研社·国才杯”全国大学生外语能力大赛", "语言实践 · 外语表达与综合运用", "本届报名与比赛时间待核实", "适合外语学习者 · 按赛项选择方向"),
            CategoryCompetitionItem("补充赛项", "“挑战杯”全国大学生课外学术科技作品竞赛", "研究实践 · 社会调查与学术表达", "本届报名与比赛时间待核实", "适合有社会调研或学术研究积累的团队"),
        ),
    ),
)

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun CategoryCompetitionListScreenPreview() {
    CategoryCompetitionListScreen(categoryKey = "it")
}
