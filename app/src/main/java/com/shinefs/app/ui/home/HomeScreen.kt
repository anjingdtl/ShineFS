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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.theme.ShineColors

/** 首页枢纽（V2.0 方案 §31 六入口）。 */
@Composable
fun HomeScreen(
    onOpenSpaceTimeCast: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenTimeCast: () -> Unit,
    onOpenHouseAudit: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenRules: () -> Unit,
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
            fontFamily = FontFamily.Serif,
            modifier = Modifier.semantics { contentDescription = "shinefs_home_tagline" },
        )
        Text(
            text = "周易风水罗盘 · 完全离线",
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
            HomeEntry("时空起卦", "罗盘定盘 · 锁定时间+空间 · 合参起卦", onClick = onOpenSpaceTimeCast)
            HomeEntry("风水罗盘", "动态罗盘 · 二十四山 · 坐向 · 时间盘", onClick = onOpenCompass)
            HomeEntry("传统时间起卦", "年月日时 · 梅花易数（无需罗盘）", onClick = onOpenTimeCast)
            HomeEntry("宅居测局", "大门 · 客厅 · 主卧 · 灶位 等场景", onClick = onOpenHouseAudit)
            HomeEntry("卦例记录", "历史 · 收藏 · 结果回顾", onClick = onOpenHistory)
            HomeEntry("规则与典籍", "推演依据 · 周易原典 · 历法说明 · 设置", onClick = onOpenRules)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "完全离线 · 依传统规则演算 · 详见使用说明",
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
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "shinefs_entry_$title" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = ShineColors.TextPrimary,
                fontSize = 17.sp,
            )
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
