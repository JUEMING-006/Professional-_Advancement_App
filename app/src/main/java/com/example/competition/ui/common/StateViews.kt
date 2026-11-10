package com.example.competition.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 数据页通用的三种状态视图。
 *
 * 抽出来的原因：原本每屏各写一份「加载中 / 加载失败 / 空数据」，
 * 文案与样式容易不一致；集中一处后新增页面直接复用。
 */
private val TextPrimary = Color(0xFF202A26)
private val TextSecondary = Color(0xFF69746F)
private val PrimaryGreen = Color(0xFF246B54)
private val LightGreenBg = Color(0xFFEDF5F0)

/** 加载中。 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    hint: String = "加载中…",
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(text = hint, fontSize = 14.sp, color = TextSecondary)
    }
}

/** 加载失败：给出原因与重试，而不是空白页。 */
@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "加载失败",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(text = message, fontSize = 12.sp, color = TextSecondary)
            if (onRetry != null) {
                Text(
                    text = "重试",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGreenBg)
                        .clickable(onClick = onRetry)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
        }
    }
}

/** 空数据：说明「为什么空」以及「下一步做什么」，而不是只显示一个空框。 */
@Composable
fun EmptyView(
    title: String,
    hint: String = "",
    modifier: Modifier = Modifier,
    actionText: String = "",
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            if (hint.isNotBlank()) {
                Text(text = hint, fontSize = 12.sp, color = TextSecondary)
            }
            if (actionText.isNotBlank() && onAction != null) {
                Text(
                    text = actionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGreenBg)
                        .clickable(onClick = onAction)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
        }
    }
}