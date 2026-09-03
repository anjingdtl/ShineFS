package com.shinefs.app.ui.divination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors

/** 起卦解易入口：三种模式可用性（模式 B/C 公式待决策，只占位不实现）。 */
@Composable
fun CastModesScreen(
    onBack: () -> Unit,
    onOpenCompass: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "起卦解易", onBack = onBack)
        Spacer(Modifier.height(16.dp))

        ModeCard(
            title = "方位起卦",
            status = "可用",
            statusColor = ShineColors.JadeAccent,
            desc = "以定盘向首起上卦，结合时刻取数定下卦与动爻（当前为临时联调口径，待正式规则核定）。",
        ) { onOpenCompass() }
        ModeCard(
            title = "时间起卦",
            status = "待决策 D-02",
            statusColor = ShineColors.GoldMuted,
            desc = "年月日时换算算法未定（公历数字 / 农历 / 干支口径），拍板后开放。",
        )
        ModeCard(
            title = "数字起卦",
            status = "待决策 D-03",
            statusColor = ShineColors.GoldMuted,
            desc = "三数取卦与求余统一规则（余 0 约定）未定，拍板后开放。",
        )
        Spacer(Modifier.height(10.dp))
        HintCard(
            "规则说明",
            "卦象一律由确定性规则引擎计算：八卦、二十四山、六十四卦、动爻、变卦均有自动化测试覆盖（含 64×6 变卦全量用例）。AI 仅做白话解释，不参与卦象生成。临时口径以显著标识区分，不会伪装成正式规则。",
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    status: String,
    statusColor: androidx.compose.ui.graphics.Color,
    desc: String,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = ShineColors.TextPrimary, fontSize = 17.sp)
            Spacer(Modifier.weight(1f))
            Text(status, color = statusColor, fontSize = 12.sp)
        }
        Text(
            desc,
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
