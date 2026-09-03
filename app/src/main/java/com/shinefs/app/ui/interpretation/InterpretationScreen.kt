package com.shinefs.app.ui.interpretation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.ai.AiInterpreter
import com.shinefs.app.ai.AiStatus
import com.shinefs.app.data.DivinationCase
import com.shinefs.app.interpret.RuleBasedInterpreter
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.yijing.data.Hexagrams
import com.shinefs.core.yijing.text.ClassicTextRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 解卦页（产品方案 §9.4）：固定八段结构 + 卦例管理（收藏/备注/删除）。
 * 原典依仓储返回（fixture 显著标注未核定）；AI 不可用时降级为确定性摘要，
 * **任何情况下不出空白页**。
 */
@Composable
fun InterpretationScreen(
    caseId: String,
    caseLoader: suspend (String) -> DivinationCase?,
    classicTexts: ClassicTextRepository,
    interpreter: AiInterpreter,
    interpreter2: RuleBasedInterpreter,
    onBack: () -> Unit,
    onUpdateCase: ((DivinationCase) -> Unit)? = null,
    onDeleteCase: (suspend (String) -> Unit)? = null,
) {
    var caseState by remember { mutableStateOf<DivinationCase?>(null) }
    var aiText by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var noteInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(caseId) {
        val loaded = withContext(Dispatchers.IO) { caseLoader(caseId) }
        caseState = loaded
        if (!noteInitialized) {
            noteText = loaded?.note ?: ""
            noteInitialized = true
        }
        loaded?.let { c ->
            val result = interpreter.interpret(c)
            aiText = if (result.status == AiStatus.OK) result.plainText else null
        }
    }

    if (deleteTargetId != null) {
        LaunchedEffect(deleteTargetId) {
            withContext(Dispatchers.IO) { onDeleteCase?.invoke(deleteTargetId!!) }
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "解卦", onBack = onBack)

        val loaded = caseState
        if (loaded == null) {
            Text("读取卦例…", color = ShineColors.TextSecondary, modifier = Modifier.padding(24.dp))
            return@Column
        }
        // 可变工作副本（收藏/备注就地更新），id 不变期间保持
        var case by remember(loaded.id) { mutableStateOf(loaded) }

        val original = Hexagrams.byKingWenOrder(case.originalHexagramOrder)
        val changed = Hexagrams.byKingWenOrder(case.changedHexagramOrder)
        val classic = classicTexts.byKingWenOrder(case.originalHexagramOrder)

        SectionTitle("一、测量结果")
        SectionBody(
            buildString {
                appendLine("场景：${case.sceneName}")
                appendLine("定盘时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(case.timestamp))}")
                append("方位：${String.format(Locale.US, "%.1f", case.azimuth ?: 0f)}°　向 ${case.facingMountain}　坐 ${case.sittingMountain}（稳定度 ${case.stability}）")
            },
        )

        SectionTitle("二、卦象结果")
        SectionBody(
            "本卦 《${original.chineseName}》${original.symbol}（第${original.kingWenOrder}卦）　" +
                "动爻 ${interpreter2.lineName(case.changingLine)}　" +
                "变卦 《${changed.chineseName}》${changed.symbol}（第${changed.kingWenOrder}卦）\n" +
                "上卦${case.upperTrigram} · 下卦${case.lowerTrigram}；规则：${case.ruleDisplayName}",
        )

        SectionTitle("三、原典依据")
        if (classic != null) {
            SectionBody(
                buildString {
                    appendLine("《周易·${classic.hexagramName}》")
                    appendLine("卦辞：${classic.judgment}")
                    append(classic.imageText)
                },
            )
            if (!classic.verified) {
                Badge("原典为临时联调数据（${classic.version}，未核定）· 正式底本待决策 D-09", ShineColors.CinnabarBright)
            } else {
                Badge("原典版本：${classic.version}（已核定）", ShineColors.JadeAccent)
            }
            if (!classic.hasLineTexts) {
                Badge("爻辞未录入：动爻（${interpreter2.lineName(case.changingLine)}）原文待原典核定入库", ShineColors.GoldMuted)
            }
        } else {
            SectionBody("本卦原典数据尚未录入。")
            Badge("原典数据待核定入库（D-09：底本与录入流程决策后补全 64 卦）", ShineColors.CinnabarBright)
        }

        SectionTitle("四、象义解析")
        SectionBody(interpreter2.symbolism(case))

        SectionTitle("五、空间解读")
        SectionBody(interpreter2.spatial(case))

        SectionTitle("六、宜忌与注意")
        SectionBody(interpreter2.advisories())

        SectionTitle("七、AI 白话解读")
        if (aiText != null) {
            SectionBody(aiText!!)
            Badge("由 AI 生成，仅供参考；卦象与原典以上文确定性结果为准", ShineColors.GoldMuted)
        } else {
            SectionBody(
                buildString {
                    appendLine("AI 解读未配置（或不可用）。以下为确定性摘要：")
                    appendLine()
                    append(interpreter2.symbolism(case).lineSequence().take(2).joinToString("\n"))
                },
            )
            Badge("AI 仅负责白话解释，不参与卦象计算；AI 不可用时完整结果不受影响", ShineColors.JadeAccent)
        }

        SectionTitle("八、规则来源与版本")
        SectionBody(
            buildString {
                appendLine("规则标识：${case.ruleId}")
                appendLine("规则名称：${case.ruleDisplayName}")
                appendLine("规则版本：${case.rulesVersion}（DOCS/YIJING_RULES.md）")
                appendLine("解释版本：${case.interpretationVersion}")
                append("原典版本：${classic?.version ?: "未入库"}")
            },
        )

        if (onUpdateCase != null && onDeleteCase != null) {
            SectionTitle("卦例管理")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ActionButton(
                        text = if (case.favorite) "★ 已收藏" else "☆ 收藏",
                        enabled = true,
                        primary = false,
                    ) {
                        val updated = case.copy(favorite = !case.favorite)
                        case = updated
                        onUpdateCase(updated)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ActionButton(
                        text = "保存备注",
                        enabled = noteText.isNotBlank() && noteText.trim() != (case.note ?: ""),
                        primary = false,
                    ) {
                        val updated = case.copy(note = noteText.trim().ifBlank { null })
                        case = updated
                        onUpdateCase(updated)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("备注（可选）", color = ShineColors.TextSecondary, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = ShineColors.TextPrimary, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShineColors.GoldPrimary,
                    unfocusedBorderColor = ShineColors.Divider,
                    cursorColor = ShineColors.GoldPrimary,
                ),
                minLines = 2,
            )
            Spacer(Modifier.height(8.dp))
            if (!confirmDelete) {
                ActionButton(
                    text = "删除此卦例",
                    enabled = true,
                    primary = false,
                ) { confirmDelete = true }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ActionButton(
                            text = "确认删除",
                            enabled = true,
                            primary = false,
                        ) { deleteTargetId = case.id }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ActionButton(
                            text = "取消",
                            enabled = true,
                            primary = false,
                        ) { confirmDelete = false }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = ShineColors.GoldPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun SectionBody(body: String) {
    Text(
        body,
        color = ShineColors.TextPrimary,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    )
}

@Composable
private fun Badge(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 6.dp),
    )
}
