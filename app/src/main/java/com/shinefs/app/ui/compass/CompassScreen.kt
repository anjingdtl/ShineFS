package com.shinefs.app.ui.compass

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shinefs.app.sensor.CompassCapabilityLevel
import com.shinefs.app.sensor.CompassController
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.yijing.rules.LaterHeavenBagua
import com.shinefs.core.yijing.rules.Mountains24
import com.shinefs.core.yijing.rules.Orientation

/** 系统级"减少动画"设置（ANIMATOR_DURATION_SCALE == 0 时禁用装饰性动画）。 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * 风水罗盘页：传感器生命周期（ON_RESUME 启动 / ON_PAUSE 停止，Disposable 兜底注销），
 * 分层展示：罗盘（传感器层）→ 角度/山/卦/五行（术数映射层）→ 状态提示（异常层）。
 */
@Composable
fun CompassScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val controller = remember { CompassController(appContext) }
    val uiState by controller.state.collectAsState()
    val reducedMotion = rememberReducedMotion()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> controller.start()
                Lifecycle.Event.ON_PAUSE -> controller.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.stop()
        }
    }

    val compass = uiState.compass
    val azimuth = compass.smoothedAzimuth ?: compass.rawAzimuth
    val orientation = azimuth?.let { runCatching { Orientation.fromAzimuth(it) }.getOrNull() }
    val stability = compass.stability

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShineColors.BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ScreenHeader(title = "风水罗盘", onBack = onBack)

        when (uiState.capability.level) {
            CompassCapabilityLevel.FULL -> Unit
            CompassCapabilityLevel.LIMITED -> LimitedModeBanner()
        }

        Spacer(Modifier.height(8.dp))
        CompassDial(
            azimuth = azimuth,
            stability = stability,
            locked = false,
            reducedMotion = reducedMotion,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "shinefs_compass_dial" },
        )
        Spacer(Modifier.height(12.dp))

        // 读数区：角度 + 坐向（术数映射层，全部由规则引擎派生）
        val big = azimuth?.let { String.format("%.1f", it) + "°" } ?: "——"
        Text(
            text = big,
            color = ShineColors.GoldBright,
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        val mountainLine = orientation?.let {
            "向 ${it.facingMountain}（${it.facingTrigram.chineseName}·${it.facingElement}）　坐 ${it.sittingMountain}"
        } ?: "等待方位读数…"
        Text(
            text = mountainLine,
            color = ShineColors.TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(12.dp))

        StatusRow("稳定度", stabilityLabel(stability))
        StatusRow("传感器精度", compass.accuracy.label)
        StatusRow(
            "磁场环境",
            if (compass.magneticInterference) "异常（${compass.magneticMagnitudeUt?.toInt()}µT）" else "正常",
            warn = compass.magneticInterference,
        )
        if (compass.tooTilted) StatusRow("持机姿态", "倾斜过大，请正对前方竖持手机", warn = true)
        if (uiState.calibrationRecommended) {
            StatusRow("校准", "精度不可靠：请持机在空中缓慢画「8」字校准", warn = true)
        }
        Spacer(Modifier.height(6.dp))

        HintCard("校准说明", "若指针漂移或精度提示不可靠，请远离金属桌面、音箱、磁吸手机壳与汽车，并持机画 8 字校准后再定盘。")
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹ 返回",
            color = ShineColors.GoldPrimary,
            fontSize = 15.sp,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .semantics { contentDescription = "shinefs_back" },
        )
        Spacer(Modifier.weight(1f))
        Text(title, color = ShineColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Text("", fontSize = 15.sp)
    }
}

@Composable
private fun LimitedModeBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(
            "当前设备不具备完整电子罗盘传感能力，本功能仅可使用有限模式。",
            color = ShineColors.CinnabarBright,
            fontSize = 13.sp,
        )
        Text(
            "将不显示传感器方位；后续版本可在起卦页使用手动输入方位。",
            color = ShineColors.TextSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String, warn: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = ShineColors.TextSecondary, fontSize = 13.sp)
        Text(
            value,
            color = if (warn) ShineColors.CinnabarBright else ShineColors.TextPrimary,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun HintCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(title, color = ShineColors.GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(body, color = ShineColors.TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

private fun stabilityLabel(s: StabilityLevel): String = when (s) {
    StabilityLevel.UNSTABLE -> "不稳定（请持稳手机）"
    StabilityLevel.FAIR -> "一般"
    StabilityLevel.GOOD -> "良好 · 可定盘"
}
