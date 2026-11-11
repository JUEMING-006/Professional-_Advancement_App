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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.liquidglass.LiquidGlassOptics
import com.liquidglass.liquidGlass

// ===== 颜色 =====
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val TextMuted = Color(0xFF939D97)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)
private val LightGrayBg = Color(0xFFF6F8F6)

/**
 * 专业分类页 —— 竞学。
 *
 * 由首页「专业分类」快捷入口进入。按专业大类浏览赛事，
 * 顶部给出「为你推荐」的高亮方向。数据为演示用静态内容。
 */
@Composable
fun ProfessionalCategoryScreen(
    onBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
) {
    // 局部 backdrop：页面内的玻璃卡要折射「本页背景」，必须自己建一层。
    // 全局 backdrop 会把整页内容层记录进去，玻璃卡若放进内容层会递归栈溢出；
    // 所以这里仿照底栏的做法 —— 记录层 + 玻璃层互为兄弟节点。
    val backdrop = rememberLayerBackdrop()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // 记录层：页面内容（放大镜背景 + 导航 + 标题 + 玻璃卡占位 + 分类网格）。
        // 注意：玻璃卡本身不在这里，只留一个等高的占位。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            // 装饰性放大镜：黑色写实放大镜 + 透明镜片（蒙版挖空镜片）。
            // 位于页面上部偏右，压在所有内容之下，镜片透出背景。
            Image(
                painter = painterResource(R.drawable.magnifier),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 305.dp, height = 282.dp),
                contentScale = ContentScale.Fit,
            )

            Column(modifier = Modifier.fillMaxSize()) {
                CategoryNavigation(onBack = onBack)

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    HeroTitle()
                    Spacer(modifier = Modifier.height(20.dp))
                    // 玻璃卡的占位（高度 112dp 与玻璃卡一致），实际内容由下方兄弟节点渲染。
                    Spacer(modifier = Modifier.height(112.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader()
                    Spacer(modifier = Modifier.height(12.dp))
                    CategoryGrid(onCategoryClick = onCategoryClick)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // 玻璃层：与记录层并列（兄弟节点），折射上面的记录层。
        // 位置 = 导航 64 + 标题区 74 + 间距 20 = 158dp。
        RecommendCard(
            backdrop = backdrop,
            onClick = { onCategoryClick("it") },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 158.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
    }
}

// ===== 页面导航 =====
@Composable
private fun CategoryNavigation(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // 返回按钮：灰底圆角方块内放绿色箭头。
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
            text = "专业分类",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 80.dp),
        )
    }
}

// ===== 标题区 =====
@Composable
private fun HeroTitle() {
    Column {
        Text(
            text = "找到适合你的方向",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 44.sp,
            letterSpacing = 6.3.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "根据你的专业，推荐最匹配的赛事",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 20.sp,
        )
    }
}

// ===== 为你推荐 =====
@Composable
private fun RecommendCard(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(112.dp)
            .liquidGlass(
                backdrop = backdrop,
                shape = RoundedCornerShape(20.dp),
                optics = LiquidGlassOptics(
                    blurRadius = 8.dp,
                    refractionHeight = 16.dp,
                    refractionAmount = 32.dp,
                    surfaceTint = LightGreenBg.copy(alpha = 0.2f),
                ),
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 75.dp, height = 26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "为你推荐",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                )
            }
            Text(
                text = "计算机科学与技术",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "匹配 12 场在报赛事",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
            )
        }
        GlassIconChip(
            iconRes = R.drawable.ic_arrow_right,
            modifier = Modifier.size(40.dp),
        )
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

// ===== 区块标题 =====
@Composable
private fun SectionHeader() {
    Text(
        text = "全部专业分类",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        lineHeight = 36.sp,
    )
}

// ===== 分类网格 =====
@Composable
private fun CategoryGrid(onCategoryClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            categories[0].let { CategoryCard(it, onCategoryClick) }
            categories[1].let { CategoryCard(it, onCategoryClick) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            categories[2].let { CategoryCard(it, onCategoryClick) }
            categories[3].let { CategoryCard(it, onCategoryClick) }
        }
        Row {
            CategoryCard(categories[4], onCategoryClick)
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryItem, onCategoryClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 175.dp, height = 137.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCategoryClick(category.key) },
    ) {
        Image(
            painter = painterResource(category.bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            GlassIconChip(
                iconRes = category.iconRes,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "${category.count} 场赛事",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
            )
        }
    }
}

// ===== 分类数据 =====
private data class CategoryItem(
    val key: String,
    val name: String,
    val count: Int,
    val bgRes: Int,
    val iconRes: Int,
)

private val categories = listOf(
    CategoryItem("it", "信息技术", 28, R.drawable.cat_it, R.drawable.ic_cat_it),
    CategoryItem("business", "经管商科", 16, R.drawable.cat_business, R.drawable.ic_cat_business),
    CategoryItem("design", "设计艺术", 11, R.drawable.cat_design, R.drawable.ic_cat_design),
    CategoryItem("engineering", "理工制造", 19, R.drawable.cat_engineering, R.drawable.ic_cat_engineering),
    CategoryItem("humanities", "人文社科", 9, R.drawable.cat_humanities, R.drawable.ic_cat_humanities),
)

// ===== 预览 =====
@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun ProfessionalCategoryScreenPreview() {
    ProfessionalCategoryScreen()
}
