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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinefs.app.data.LockedReading
import com.shinefs.app.data.Scenes
import com.shinefs.app.ui.compass.HintCard
import com.shinefs.app.ui.compass.ScreenHeader
import com.shinefs.app.ui.theme.ShineColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 场景选择（产品方案 §9.5）：定盘后选择测局场景，随即按确定性规则起卦。 */
@Composable
fun SceneSelectScreen(
    reading: LockedReading,
    onBack: () -> Unit,
    onSelect: (sceneId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "选择场景", onBack = onBack)
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "已定盘 · 向 ${reading.facingMountain} 坐 ${reading.sittingMountain} · ${String.format(Locale.US, "%.1f", reading.azimuth)}°",
                color = ShineColors.GoldBright,
                fontSize = 15.sp,
            )
            Text(
                "${reading.facingTrigram}卦 · ${reading.facingElement} · ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reading.timestamp))}",
                color = ShineColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(Modifier.height(8.dp))

        SceneRow("单项测量", Scenes.generic.guidance) { onSelect(Scenes.generic.id) }
        HorizontalDivider(color = ShineColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
        Text(
            "宅居测局场景",
            color = ShineColors.GoldPrimary,
            fontSize = 13.sp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Scenes.house.forEach { scene ->
            SceneRow(scene.name, scene.guidance) { onSelect(scene.id) }
        }
        Spacer(Modifier.height(8.dp))
        HintCard("定盘说明", "起卦使用定盘时刻的方位与时间；相同的定盘结果会得到相同卦象。")
    }
}

@Composable
private fun SceneRow(name: String, guidance: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "shinefs_scene_$name" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = ShineColors.TextPrimary, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Text("›", color = ShineColors.GoldMuted, fontSize = 16.sp)
        }
        Text(
            guidance,
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
