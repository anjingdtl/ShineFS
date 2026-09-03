package com.shinefs.app.ui.divination

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.compass.rememberReducedMotion
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.model.Hexagram
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 起卦揭示页：六爻自下而上逐爻生成 → 动爻朱砂高亮 + 阴阳翻转 → 本卦→变卦过渡。
 * 结果由确定性规则引擎在进入本页前算出；动画仅呈现，不参与计算。
 */
@Composable
fun HexagramRevealScreen(
    caseId: String,
    caseLoader: suspend (String) -> com.shinefs.app.data.DivinationCase?,
    ruleExplain: String,
    onBackToHome: () -> Unit,
    onOpenInterpretation: (String) -> Unit,
) {
    val caseState by produceState<com.shinefs.app.data.DivinationCase?>(initialValue = null, caseId) {
        value = withContext(kotlinx.coroutines.Dispatchers.IO) { caseLoader(caseId) }
    }
    var revealed by remember { mutableIntStateOf(0) }        // 已生成爻数 0..6
    var flipDone by remember { mutableStateOf(false) }       // 动爻翻转完成
    var changedShown by remember { mutableStateOf(false) }   // 变卦呈现
    var showRule by remember { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotion()

    LaunchedEffect(caseState?.id) {
        val c = caseState ?: return@LaunchedEffect
        if (reducedMotion) {
            revealed = 6
            flipDone = true
            changedShown = true
            return@LaunchedEffect
        }
        // 初爻 → 上爻 逐爻生成
        for (i in 1..6) {
            delay(280)
            revealed = i
        }
        delay(500)
        flipDone = true
        delay(650)
        changedShown = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "卦象已成", onBack = onBackToHome)

        val case = caseState ?: run {
            Text("未找到卦例", color = ShineColors.CinnabarBright, modifier = Modifier.padding(24.dp))
            return@Column
        }

        val original = Hexagrams.byKingWenOrder(case.originalHexagramOrder)
        val changed = Hexagrams.byKingWenOrder(case.changedHexagramOrder)

        Text(
            "场景 · ${case.sceneName}",
            color = ShineColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            "向 ${case.facingMountain} 坐 ${case.sittingMountain} · ${String.format("%.1f", case.azimuth ?: 0f)}°",
            color = ShineColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp),
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            HexagramFigure(
                hexagram = original,
                label = "本卦",
                revealedLines = revealed,
                changingLine = case.changingLine,
                flipDone = flipDone,
                reducedMotion = reducedMotion,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp),
            ) {
                Text("→", color = ShineColors.GoldMuted, fontSize = 22.sp)
                Text("动", color = ShineColors.CinnabarBright, fontSize = 14.sp)
                Text("${case.changingLine}", color = ShineColors.CinnabarBright, fontSize = 16.sp)
            }
            Box(modifier = Modifier.alpha(if (changedShown || reducedMotion) 1f else 0f)) {
                HexagramFigure(
                    hexagram = changed,
                    label = "变卦",
                    revealedLines = if (changedShown || reducedMotion) 6 else 0,
                    changingLine = 0,
                    flipDone = true,
                    reducedMotion = reducedMotion,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "上卦 ${case.upperTrigram} · 下卦 ${case.lowerTrigram} · ${case.changingLine.let { "第${it}爻动" }}",
            color = ShineColors.TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            "规则：${case.ruleDisplayName}（${case.rulesVersion}）",
            color = if (case.ruleId.startsWith("fixture")) ShineColors.CinnabarBright else ShineColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp),
        )
        Spacer(Modifier.height(16.dp))

        ActionButton(
            text = "查看解读",
            enabled = true,
            primary = true,
            contentDesc = "shinefs_open_interpretation",
        ) { onOpenInterpretation(case.id) }
        Spacer(Modifier.height(10.dp))
        ActionButton(
            text = if (showRule) "收起算法依据" else "查看算法依据",
            enabled = true,
            primary = false,
            contentDesc = "shinefs_show_rule",
        ) { showRule = !showRule }
        if (showRule) {
            Spacer(Modifier.height(8.dp))
            HintCard("算法依据", ruleExplain)
        }
        Spacer(Modifier.height(8.dp))
        HintCard("卦例留存", "本卦例已存入记录（规则版本 ${case.rulesVersion} / 解释版本 ${case.interpretationVersion}），可在卦例记录中查看。")
        Spacer(Modifier.height(10.dp))
        ActionButton(text = "返回首页", enabled = true, primary = false) { onBackToHome() }
    }
}

/**
 * 卦象图：六爻自下而上（初爻在底部）；reached 的爻可见；
 * 动爻在 flipDone 前为本卦爻、其后朱砂高亮并翻转为变卦爻。
 */
@Composable
fun HexagramFigure(
    hexagram: Hexagram,
    label: String,
    revealedLines: Int,
    changingLine: Int,
    flipDone: Boolean,
    reducedMotion: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 上爻(6)在顶、初爻(1)在底
        for (lineNo in 6 downTo 1) {
            val lineValue = hexagram.lines[lineNo - 1]
            val isChanging = lineNo == changingLine
            // 翻转后动爻显示为相反爻（变卦该位）
            val showYang = if (isChanging && flipDone) lineValue == 0 else lineValue == 1
            val visible = lineNo <= revealedLines
            LineBar(
                yang = showYang,
                visible = visible,
                highlighted = isChanging && flipDone,
                reducedMotion = reducedMotion,
            )
            if (lineNo != 1) Spacer(Modifier.height(7.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "$label · ${hexagram.chineseName}",
            color = ShineColors.GoldBright,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "${hexagram.symbol} · 第${hexagram.kingWenOrder}卦",
            color = ShineColors.TextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun LineBar(
    yang: Boolean,
    visible: Boolean,
    highlighted: Boolean,
    reducedMotion: Boolean,
) {
    val appear by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(if (reducedMotion) 0 else 220),
        label = "lineAppear",
    )
    val color = when {
        highlighted -> ShineColors.CinnabarBright
        yang -> ShineColors.GoldPrimary
        else -> ShineColors.GoldMuted
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(120.dp)
            .height(13.dp)
            .alpha(appear)
            .scale(scaleX = if (highlighted && !reducedMotion) 1.06f else 1f, scaleY = 1f)
            .semantics { contentDescription = if (yang) "yang" else "yin" },
    ) {
        if (yang) {
            Box(
                Modifier
                    .width(120.dp)
                    .height(13.dp)
                    .background(color, RoundedCornerShape(3.dp)),
            )
        } else {
            Box(
                Modifier
                    .width(52.dp)
                    .height(13.dp)
                    .background(color, RoundedCornerShape(3.dp)),
            )
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier
                    .width(52.dp)
                    .height(13.dp)
                    .background(color, RoundedCornerShape(3.dp)),
            )
        }
    }
}
