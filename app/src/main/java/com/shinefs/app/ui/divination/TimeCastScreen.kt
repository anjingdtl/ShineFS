package com.shinefs.app.ui.divination

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.AppGraph
import com.shinefs.app.data.Scenes
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.compass.StatusRow
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.calendar.model.YijingTimeContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 传统时间起卦页（V2.0 方案 §8 模式 A）：年月日时四数确定性起卦，无需罗盘。
 * 时间上下文实时展示（农历/干支/时辰/节气）；起卦瞬间锁定当前时刻。
 */
@Composable
fun TimeCastScreen(
    onBack: () -> Unit,
    onCasted: (String) -> Unit,
) {
    var ctx by remember { mutableStateOf<YijingTimeContext?>(null) }
    var clock by remember { mutableStateOf(System.currentTimeMillis()) }

    // 每秒刷新时钟；每 2 秒重算历法上下文（节气/时辰粒度远大于该周期）
    LaunchedEffect(Unit) {
        while (true) {
            clock = System.currentTimeMillis()
            ctx = withContext(Dispatchers.Default) {
                AppGraph.timeResolver.resolve(clock, AppGraph.timeZone, AppGraph.dayBoundaryPolicy())
            }
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "传统时间起卦", onBack = onBack)

        Text(
            SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault()).format(Date(clock)),
            color = ShineColors.GoldBright,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 12.dp)
                .align(androidx.compose.ui.Alignment.CenterHorizontally),
        )
        Text(
            "起卦以点击瞬间时刻为准（四数：年支＋农历月＋农历日＋时辰）",
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .align(androidx.compose.ui.Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(14.dp))
        val c = ctx
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
                .padding(14.dp)
                .semantics { contentDescription = "shinefs_time_panel" },
        ) {
            Text(
                "时间盘（传统农历历表）",
                color = ShineColors.GoldPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            StatusRow("农历", c?.lunarDisplay ?: "…")
            StatusRow("年干支", c?.dayGanzhi?.let { "${c.yearStem.chinese}${c.yearBranch.chinese}年" } ?: "…")
            StatusRow("日干支", c?.dayGanzhi?.name ?: "…")
            StatusRow("时辰", c?.shichen?.display ?: "…")
            StatusRow("节气", c?.solarTerm?.term?.chinese ?: "…")
            StatusRow("月建", c?.monthBranch?.chinese?.plus("月") ?: "…")
            StatusRow("换日", c?.dayBoundaryPolicy?.let(::dayBoundaryLabel) ?: "…")
        }

        Spacer(Modifier.height(16.dp))
        val scope = rememberCoroutineScope()
        ActionButton(
            text = "起　卦",
            enabled = ctx != null,
            primary = true,
            contentDesc = "shinefs_time_cast_button",
        ) {
            scope.launch(Dispatchers.IO) {
                val case = AppGraph.divinationService.castTime(scene = Scenes.generic)
                onCasted(case.id)
            }
        }

        Spacer(Modifier.height(10.dp))
        HintCard(
            "规则说明",
            "梅花易数年月日时起例：年支、农历月、农历日与时辰分别取数，依传统方法推得上下卦和动爻；" +
                "余数归零时按传统取卦。全程离线，每次同样输入都会得到相同结果。",
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun dayBoundaryLabel(policy: com.shinefs.core.calendar.model.DayBoundaryPolicy): String = when (policy) {
    com.shinefs.core.calendar.model.DayBoundaryPolicy.CIVIL_MIDNIGHT -> "民用午夜（00:00）"
    com.shinefs.core.calendar.model.DayBoundaryPolicy.ZI_HOUR_START_23 -> "晚子时（23:00）"
}
