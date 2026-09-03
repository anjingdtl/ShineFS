package com.shinefs.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import com.shinefs.app.data.DivinationCase
import com.shinefs.app.data.Scenes
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 卦例记录（产品方案 §9.6）：列表 + 日期/场景筛选 + 收藏筛选；详情走解读页管理区。 */
@Composable
fun HistoryScreen(
    casesProvider: suspend () -> List<DivinationCase>,
    onBack: () -> Unit,
    onOpenCase: (String) -> Unit,
) {
    val all by produceState(initialValue = listOf<DivinationCase>()) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { casesProvider() }
    }
    var favOnly by remember { mutableStateOf(false) }
    var sceneFilter by remember { mutableStateOf<String?>(null) } // null=全部场景
    var dateFilter by remember { mutableStateOf(DateFilter.ALL) }

    val filtered = remember(all, favOnly, sceneFilter, dateFilter) {
        val now = System.currentTimeMillis()
        all.filter { c ->
            (!favOnly || c.favorite) &&
                (sceneFilter == null || c.sceneId == sceneFilter) &&
                when (dateFilter) {
                    DateFilter.ALL -> true
                    DateFilter.TODAY -> now - c.timestamp < 24L * 3600 * 1000
                    DateFilter.WEEK -> now - c.timestamp < 7L * 24 * 3600 * 1000
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "卦例记录", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip("全部", dateFilter == DateFilter.ALL && !favOnly && sceneFilter == null) {
                favOnly = false; sceneFilter = null; dateFilter = DateFilter.ALL
            }
            FilterChip("★ 收藏", favOnly) { favOnly = !favOnly }
            DateFilter.entries.forEach { f ->
                if (f != DateFilter.ALL) FilterChip(f.label, dateFilter == f && !favOnly && sceneFilter == null) {
                    favOnly = false; sceneFilter = null; dateFilter = f
                }
            }
            Scenes.house.forEach { sc ->
                FilterChip(sc.name, sceneFilter == sc.id) {
                    sceneFilter = if (sceneFilter == sc.id) null else sc.id
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Text(
                if (all.isEmpty()) "暂无卦例：定盘起卦后将自动留存。" else "当前筛选下无卦例。",
                color = ShineColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 32.dp),
            )
        }
        filtered.forEach { c ->
            CaseRow(c) { onOpenCase(c.id) }
            HorizontalDivider(color = ShineColors.Divider, modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

private enum class DateFilter(val label: String) { ALL("全部"), TODAY("今天"), WEEK("近7天") }

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) ShineColors.GoldBright else ShineColors.TextSecondary,
        fontSize = 12.sp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (selected) ShineColors.BackgroundRaised else ShineColors.BackgroundDeep,
                RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun CaseRow(c: DivinationCase, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "shinefs_case_${c.id}" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (c.favorite) "★ " else "",
                color = ShineColors.GoldBright,
                fontSize = 13.sp,
            )
            Text(
                c.sceneName,
                color = ShineColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(c.timestamp)),
                color = ShineColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Text(
            "《${c.originalHexagramName}》${c.changingLine}爻动 → 《${c.changedHexagramName}》　向${c.facingMountain}坐${c.sittingMountain}" +
                if (c.note != null) "　📝" else "",
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
