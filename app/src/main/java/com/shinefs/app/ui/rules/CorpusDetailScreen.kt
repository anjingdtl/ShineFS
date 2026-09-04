package com.shinefs.app.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.AppGraph
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors

/** 单卦原典详情页：卦辞 / 彖 / 大象 / 逐爻（爻辞+小象）/ 用九用六。 */
@Composable
fun CorpusDetailScreen(
    kingWenOrder: Int,
    onBack: () -> Unit,
) {
    val e = AppGraph.classicCorpus.byKingWenOrder(kingWenOrder)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "周易 · ${e?.name ?: "未知"}卦", onBack = onBack)
        if (e == null) {
            Text("未找到该卦", color = ShineColors.CinnabarBright)
            return@Column
        }

        Text(
            "${e.name} ${String(Character.toChars(0x4DC0 + e.kingWenOrder - 1))} · 第${e.kingWenOrder}卦",
            color = ShineColors.GoldBright,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
        )

        CorpusBlock("卦辞", e.judgment)
        e.tuan?.let { CorpusBlock("彖曰", it) }
        e.greatImage?.let { CorpusBlock("象曰（大象）", it) }
        e.lines.forEach { line ->
            CorpusBlock(
                lineName(line.line),
                line.text + (line.smallImage?.let { "\n小象曰：$it" } ?: ""),
            )
        }
        e.specialUseText?.let {
            CorpusBlock(
                "特爻",
                it + (e.specialUseSmallImage?.let { s -> "\n小象曰：$s" } ?: ""),
            )
        }

        if (e.textualVariants.isNotEmpty()) {
            CorpusBlock("校勘注记", e.textualVariants.joinToString("\n"))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "典籍状态：${if (e.verified) "已核定" else "待核对"}\n底本：《周易》通行本电子底本",
            color = ShineColors.GoldMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(vertical = 10.dp),
        )
    }
}

private fun lineName(line: Int): String = when (line) {
    1 -> "初爻"
    2 -> "二爻"
    3 -> "三爻"
    4 -> "四爻"
    5 -> "五爻"
    else -> "上爻"
}

@Composable
private fun CorpusBlock(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(
            title,
            color = ShineColors.GoldPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            body,
            color = ShineColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 23.sp,
        )
    }
}
