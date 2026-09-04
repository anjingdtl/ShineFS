package com.shinefs.app.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.AppGraph
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.compass.StatusRow
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.divination.classimage.ClassImageTable

/**
 * 规则与典籍页（V2.0 方案 §31）：规则版本、原典版本、来源、历法策略与北向策略，附设置。
 */
@Composable
fun RulesScreen(
    onBack: () -> Unit,
    onOpenCorpus: () -> Unit,
) {
    var ziBoundary by remember {
        mutableStateOf(AppGraph.dayBoundaryPolicy().name == "ZI_HOUR_START_23")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "规则与典籍", onBack = onBack)

        SectionTitle("起卦方法")
        RuleCard(
            "梅花易数 · 年月日时起卦",
            "按年支、农历月、农历日和时辰取数；闰月沿用本月序号。",
        )
        RuleCard(
            "时空合参 · 时间卦与罗盘方应",
            "时间决定卦象，罗盘只补充方位信息，不改变时间起卦结果。",
        )
        RuleCard(
            "梅花易数 · 后天端法（物象方位）",
            "物象取卦只采用《说卦》明文；方位取后天八卦。",
        )
        RuleCard(
            "八卦类象表（仅《说卦》明文）",
            "共 ${ClassImageTable.all.size} 条；只收《说卦》明文，不增加现代附会。",
        )

        SectionTitle("周易原典")
        val corpus = AppGraph.classicCorpus
        StatusRow("典籍状态", if (corpus.all.all { it.verified }) "已核定" else "待核对")
        StatusRow("底本", "《周易》通行本电子底本")
        StatusRow("核对情况", "六十四卦内容已核对")
        Spacer(Modifier.height(6.dp))
        ActionButton(
            text = "浏览六十四卦原典",
            enabled = true,
            primary = true,
            contentDesc = "shinefs_open_corpus",
        ) { onOpenCorpus() }

        SectionTitle("历法与策略")
        StatusRow("历表", "传统农历历表（1900–2100）")
        StatusRow("历表状态", "1900–2100 年份已核对")
        StatusRow("方位基准", "磁北（暂未校正磁偏角）")
        StatusRow("闰月处理", "闰月沿用本月序号")
        StatusRow("互卦取法", "按二、三、四爻与三、四、五爻取互卦")

        SectionTitle("设置 · 换日时刻")
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                text = if (!ziBoundary) "● 民用午夜（默认）" else "○ 民用午夜（默认）",
                enabled = true,
                primary = !ziBoundary,
                modifier = Modifier.weight(1f),
            ) {
                ziBoundary = false
                AppGraph.setDayBoundaryPolicy(com.shinefs.core.calendar.model.DayBoundaryPolicy.CIVIL_MIDNIGHT)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                text = if (ziBoundary) "● 晚子时 23:00 换日" else "○ 晚子时 23:00 换日",
                enabled = true,
                primary = ziBoundary,
                modifier = Modifier.weight(1f),
            ) {
                ziBoundary = true
                AppGraph.setDayBoundaryPolicy(com.shinefs.core.calendar.model.DayBoundaryPolicy.ZI_HOUR_START_23)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "切换换日时刻后请重新起卦；每条卦例都会保留当时的换日选择，之后可按原选择重新核对。",
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
        )

        SectionTitle("体系声明")
        HintCard(
            "术数体系分层",
            "《周易》经传负责卦爻依据；梅花易数负责起卦、体用与五行；" +
                "罗经传统负责二十四山坐向；历法负责农历、干支与节气。" +
                "各部分彼此分开，出处清楚可查。",
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
internal fun SectionTitle(title: String) {
    Text(
        title,
        color = ShineColors.GoldPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Serif,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun RuleCard(name: String, assumptions: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text("$name", color = ShineColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (assumptions.isNotBlank()) {
            Text("说明：$assumptions", color = ShineColors.TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
        }
    }
}

/** 六十四卦原典列表页。 */
@Composable
fun CorpusListScreen(
    onBack: () -> Unit,
    onOpenDetail: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "周易原典", onBack = onBack)
        Text(
            "已核定 · 六十四卦原典可查",
            color = ShineColors.JadeAccent,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        LazyColumn {
            items(AppGraph.classicCorpus.all) { e ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(e.kingWenOrder) }
                        .padding(vertical = 10.dp),
                ) {
                    Text(
                        "${e.name} ${hexSymbol(e.kingWenOrder)} · 第${e.kingWenOrder}卦",
                        color = ShineColors.TextPrimary,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                    )
                    Text(
                        e.judgment,
                        color = ShineColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun hexSymbol(order: Int): String =
    String(Character.toChars(0x4DC0 + order - 1))
