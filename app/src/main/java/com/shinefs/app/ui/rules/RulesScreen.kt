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
import com.shinefs.core.calendar.table.LunarTableData
import com.shinefs.core.divination.classimage.ClassImageTable
import com.shinefs.core.divination.rule.MeihuaPostHeavenObjectDirectionRuleV1
import com.shinefs.core.divination.rule.MeihuaTimeDivinationRuleV1
import com.shinefs.core.divination.rule.TimeCastWithSpatialResponse

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

        SectionTitle("起卦规则（正式）")
        RuleCard(
            "meihua-time-v1",
            "梅花易数 · 年月日时起卦",
            AppGraph.meihuaTimeRule.manifest.assumptions.joinToString("；"),
        )
        RuleCard(
            "time-cast-with-spatial-response-v1",
            "时空合参 · 时间卦＋罗盘方应",
            TimeCastWithSpatialResponse().manifest.assumptions.joinToString("；"),
        )
        RuleCard(
            "meihua-postheaven-v1",
            "梅花易数 · 后天端法（物象方位）",
            MeihuaPostHeavenObjectDirectionRuleV1().manifest.assumptions.joinToString("；"),
        )
        RuleCard(
            ClassImageTable.VERSION,
            "八卦类象表（仅《说卦》明文）",
            "共 ${ClassImageTable.all.size} 条；不扩充现代配卦（TD-V2-07）",
        )

        SectionTitle("周易原典")
        val corpus = AppGraph.classicCorpus
        StatusRow("版本", corpus.version)
        StatusRow("底本", "维基文库「周易」通行本系统")
        StatusRow("校验和", corpus.corpusChecksum.take(16) + "…")
        Spacer(Modifier.height(6.dp))
        ActionButton(
            text = "浏览六十四卦原典",
            enabled = true,
            primary = true,
            contentDesc = "shinefs_open_corpus",
        ) { onOpenCorpus() }

        SectionTitle("历法与策略")
        StatusRow("历表", "${LunarTableData.VERSION}（1900–2100）")
        StatusRow("历表校验和", LunarTableData.checksum.take(16) + "…")
        StatusRow("北参考", "磁北（MAGNETIC，TD-V2-08 真北未启用）")
        StatusRow("闰月政策", "SAME_MONTH_NUMBER（闰月取同月号）")
        StatusRow("互卦策略", "STANDARD_234_345（乾坤有互）")

        SectionTitle("设置 · 日界策略")
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
            "切换日界策略后请重新起卦；每条卦例均留存其起卦时的策略与规则版本，可按原口径复算。",
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
        )

        SectionTitle("体系声明")
        HintCard(
            "术数体系分层",
            "《周易》经传（A 级）负责卦爻正典；梅花易数传统（B 级）负责起卦/体用/五行；" +
                "罗经传统（C 级）负责二十四山坐向；历法工程（E 级）负责农历/干支/节气。" +
                "三层不混写，全部规则可溯源（DOCS/SOURCE_CATALOG.md）。",
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
private fun RuleCard(ruleId: String, name: String, assumptions: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text("$name", color = ShineColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text("ruleId：$ruleId", color = ShineColors.GoldMuted, fontSize = 11.sp)
        if (assumptions.isNotBlank()) {
            Text("假设：$assumptions", color = ShineColors.TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
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
            "${AppGraph.classicCorpus.version} · 已核定 · 双源核验",
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
