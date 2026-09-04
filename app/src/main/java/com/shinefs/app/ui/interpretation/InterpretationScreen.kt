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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.data.DivinationCase
import com.shinefs.app.ui.compass.ActionButton
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 解卦页（V2.0 方案 §24 固定九段）：渲染本地规则引擎生成的完整报告
 * （0 AI；报告随卦例留存，可离线复算）。V1 fixture 旧例显示 legacy 横幅。
 */
@Composable
fun InterpretationScreen(
    caseId: String,
    caseLoader: suspend (String) -> DivinationCase?,
    recompute: (DivinationCase) -> String?,
    onBack: () -> Unit,
    onUpdateCase: ((DivinationCase) -> Unit)? = null,
    onDeleteCase: (suspend (String) -> Unit)? = null,
) {
    var caseState by remember { mutableStateOf<DivinationCase?>(null) }
    var noteText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var noteInitialized by remember { mutableStateOf(false) }
    var recomputeText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(caseId) {
        val loaded = withContext(Dispatchers.IO) { caseLoader(caseId) }
        caseState = loaded
        if (!noteInitialized) {
            noteText = loaded?.note ?: ""
            noteInitialized = true
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
        var case by remember(loaded.id) { mutableStateOf(loaded) }

        if (case.legacyFixture) {
            Banner(
                "V1 联调期卦例（legacy-fixture）：起卦口径为临时 Fixture 规则，仅供查看，" +
                    "不属于 V2 正式演算结果（rules-v0.1）。",
                ShineColors.CinnabarBright,
            )
        }

        val report = case.reportText
        if (report != null) {
            // 九段报告：按「一、二、…九、」标题行分节渲染
            val lines = report.split("\n")
            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                if (line.length > 2 && line[1] == '、' && line[0] in '一'..'九') {
                    SectionTitle(line)
                    val body = StringBuilder()
                    var j = i + 1
                    while (j < lines.size && !(lines[j].length > 2 && lines[j][1] == '、' && lines[j][0] in '一'..'九')) {
                        if (body.isNotEmpty()) body.append('\n')
                        body.append(lines[j])
                        j++
                    }
                    if (body.isNotEmpty()) SectionBody(body.toString())
                    i = j
                } else {
                    i++
                }
            }
        } else {
            SectionTitle("卦象结果（V1 旧例）")
            SectionBody(
                "本卦 ${case.originalHexagramName}（第${case.originalHexagramOrder}卦） · " +
                    "第${case.changingLine}爻动 · 变卦 ${case.changedHexagramName}；" +
                    "规则：${case.ruleDisplayName}（${case.rulesVersion}）。",
            )
            SectionTitle("规则来源与版本")
            SectionBody("V1 旧例无九段报告与复算轨迹。")
        }

        SectionTitle("离线复算")
        if (recomputeText == null && !case.legacyFixture) {
            ActionButton(
                text = "按原规则版本复算",
                enabled = true,
                primary = false,
                contentDesc = "shinefs_recompute",
            ) {
                recomputeText = recompute(case)
            }
        }
        recomputeText?.let { SectionBody(it) }

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
        fontFamily = FontFamily.Serif,
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
private fun Banner(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    )
}
