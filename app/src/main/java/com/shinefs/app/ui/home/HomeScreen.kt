package com.shinefs.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.theme.ShineColors

/** 首页枢纽：四大入口（产品方案 §9.1）。 */
@Composable
fun HomeScreen(
    onOpenCompass: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .padding(horizontal = 28.dp, vertical = 48.dp)
            .semantics { contentDescription = "shinefs_home_root" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "观方辨位 · 依易起卦",
            color = ShineColors.GoldPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.semantics { contentDescription = "shinefs_home_tagline" },
        )
        Text(
            text = "ShineFS 周易风水罗盘",
            color = ShineColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HomeEntry("风水罗盘", "动态罗盘 · 定盘 · 坐向", enabled = true, tag = "", onClick = onOpenCompass)
            HomeEntry("起卦解易", "方位起卦 · 时间起卦 · 数字起卦", enabled = false, tag = "建设中 Cycle 04-05", onClick = {})
            HomeEntry("宅居测局", "大门 · 客厅 · 主卧 · 灶位 等场景", enabled = false, tag = "建设中 Cycle 06", onClick = {})
            HomeEntry("卦例记录", "历史 · 收藏 · 规则版本", enabled = false, tag = "建设中 Cycle 07", onClick = {})
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "V1.0 建设中 · 术数规则见 YIJING_RULES",
                color = ShineColors.GoldMuted,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun HomeEntry(
    title: String,
    subtitle: String,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit,
) {
    val modifier = if (enabled) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "shinefs_entry_$title" }
    } else {
        Modifier.fillMaxWidth()
    }
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (enabled) ShineColors.TextPrimary else ShineColors.TextSecondary,
                fontSize = 17.sp,
            )
            if (tag.isNotEmpty()) {
                Text(
                    text = "　$tag",
                    color = ShineColors.GoldMuted,
                    fontSize = 11.sp,
                )
            }
        }
        Text(
            text = subtitle,
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        HorizontalDivider(
            color = ShineColors.Divider,
            modifier = Modifier
                .padding(top = 14.dp)
                .height(1.dp),
        )
    }
}
