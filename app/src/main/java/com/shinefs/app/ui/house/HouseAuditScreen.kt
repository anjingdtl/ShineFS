package com.shinefs.app.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.data.HouseSummarizer
import com.shinefs.app.data.Scenes
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors
import java.util.UUID

/**
 * 宅居测局（产品方案 §9.5）：八场景逐项测量 → 整宅摘要。
 * 测量走"罗盘→定盘→起卦（场景预选）"链路；结果卦例挂 houseAuditId。
 */
@Composable
fun HouseAuditScreen(
    casesProvider: suspend () -> List<com.shinefs.app.data.DivinationCase>,
    onBack: () -> Unit,
    onMeasureScene: (auditId: String, sceneId: String) -> Unit,
    onOpenCase: (caseId: String) -> Unit,
) {
    // 当前测局会话（跨导航存活于 AppGraph；卦例入仓储，可回溯）
    var auditId by remember { mutableStateOf(com.shinefs.app.AppGraph.obtainHouseAuditId()) }
    var refresh by remember { mutableStateOf(0) }
    val cases by produceState(initialValue = listOf<com.shinefs.app.data.DivinationCase>(), auditId, refresh) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { casesProvider() }
    }
    val summary = remember(cases, auditId) { HouseSummarizer.summarize(auditId, cases) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "宅居测局", onBack = onBack)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "本次测局 ${summary.measuredCount}/${summary.totalCount} 处",
                color = ShineColors.GoldBright,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "新开测局",
                color = ShineColors.GoldMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable {
                        auditId = com.shinefs.app.AppGraph.newHouseAuditId()
                        refresh++
                    }
                    .padding(6.dp)
                    .semantics { contentDescription = "shinefs_new_audit" },
            )
        }
        Spacer(Modifier.height(6.dp))

        Scenes.house.forEach { scene ->
            val entry = summary.entries.firstOrNull { it.sceneId == scene.id }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        Modifier.clickable {
                            if (entry != null) onOpenCase(entry.caseId) else onMeasureScene(auditId, scene.id)
                        },
                    )
                    .padding(vertical = 10.dp)
                    .semantics { contentDescription = "shinefs_house_${scene.name}" },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(scene.name, color = ShineColors.TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    if (entry != null) {
                        Text(
                            "向${entry.facingMountain}坐${entry.sittingMountain} · 《${entry.hexagramName}》›",
                            color = ShineColors.GoldPrimary,
                            fontSize = 13.sp,
                        )
                    } else {
                        Text("待测 ›", color = ShineColors.TextSecondary, fontSize = 13.sp)
                    }
                }
                Text(
                    scene.guidance,
                    color = ShineColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            HorizontalDivider(color = ShineColors.Divider, modifier = Modifier.padding(vertical = 2.dp))
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "空间测局摘要",
            color = ShineColors.GoldPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            HouseSummarizer.summaryText(summary),
            color = ShineColors.TextPrimary,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
                .padding(12.dp),
        )
        Spacer(Modifier.height(10.dp))
        HintCard("测局说明", "每处场景独立定盘测量；同一测局内每场景保留最近一次卦例。摘要仅汇总坐向与卦象，不构成综合吉凶断语。")
    }
}
