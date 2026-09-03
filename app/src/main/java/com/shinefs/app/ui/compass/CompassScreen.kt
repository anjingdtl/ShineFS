package com.shinefs.app.ui.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shinefs.app.data.LockedReading
import com.shinefs.app.sensor.CompassCapabilityLevel
import com.shinefs.app.sensor.CompassController
import com.shinefs.app.ui.theme.ShineColors
import com.shinefs.core.compass.CompassState
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.yijing.rules.Orientation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 风水罗盘页：传感器生命周期（ON_RESUME 启动 / ON_PAUSE 停止，Disposable 兜底注销），
 * 分层展示：罗盘（传感器层）→ 角度/山/卦/五行（术数映射层）→ 状态提示（异常层）。
 * 定盘：稳定度良好且无磁扰/倾斜时锁定读数（朱砂"定"印 + 触觉），随后进入起卦流程。
 */
@Composable
fun CompassScreen(
    onBack: () -> Unit,
    onCast: (LockedReading) -> Unit,
) {
    val context = LocalContext.current
    val controller = remember(context) { CompassController(context.applicationContext) }
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
    val liveAzimuth = compass.smoothedAzimuth ?: compass.rawAzimuth

    var locked by remember { mutableStateOf<LockedReading?>(null) }
    var sealVisible by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val displayAzimuth = locked?.azimuth ?: liveAzimuth
    val orientation = displayAzimuth?.let { runCatching { Orientation.fromAzimuth(it) }.getOrNull() }
    val canLock = uiState.capability.level == CompassCapabilityLevel.FULL &&
        liveAzimuth != null && locked == null &&
        compass.stability == StabilityLevel.GOOD &&
        !compass.magneticInterference && !compass.tooTilted

    fun doLock() {
        val az = liveAzimuth ?: return
        val o = runCatching { Orientation.fromAzimuth(az) }.getOrNull() ?: return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        locked = LockedReading(
            azimuth = az,
            facingMountain = o.facingMountain,
            sittingMountain = o.sittingMountain,
            facingTrigram = o.facingTrigram.chineseName,
            facingElement = o.facingElement,
            timestamp = System.currentTimeMillis(),
            stability = compass.stability.label,
            accuracy = compass.accuracy.label,
        )
        sealVisible = true
    }

    LaunchedEffect(sealVisible) {
        if (sealVisible) {
            val anim = Animatable(0f)
            anim.animateTo(1f, tween(1100))
            sealVisible = false
        }
    }

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
        Box(modifier = Modifier.fillMaxWidth()) {
            CompassDial(
                azimuth = displayAzimuth,
                stability = if (locked != null) StabilityLevel.GOOD else compass.stability,
                locked = locked != null,
                reducedMotion = reducedMotion,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "shinefs_compass_dial" },
            )
            if (sealVisible) {
                SealOverlay(reducedMotion = reducedMotion)
            }
        }
        Spacer(Modifier.height(12.dp))

        val big = displayAzimuth?.let { String.format(Locale.US, "%.1f", it) + "°" } ?: "——"
        Text(
            text = big,
            color = if (locked != null) ShineColors.CinnabarBright else ShineColors.GoldBright,
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
        val lk = locked
        if (lk != null) {
            Text(
                text = "已定盘 · ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lk.timestamp))}",
                color = ShineColors.CinnabarBright,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
                    .semantics { contentDescription = "shinefs_locked_badge" },
            )
        }
        Spacer(Modifier.height(12.dp))

        StatusRow("稳定度", stabilityLabel(compass.stability))
        StatusRow("传感器精度", compass.accuracy.label)
        StatusRow(
            "磁场环境",
            if (compass.magneticInterference) "异常（${compass.magneticMagnitudeUt?.toInt()}µT）" else "正常",
            warn = compass.magneticInterference,
        )
        if (lk == null && compass.tooTilted) StatusRow("持机姿态", "倾斜过大，请正对前方竖持手机", warn = true)
        if (lk == null && uiState.calibrationRecommended) {
            StatusRow("校准", "精度不可靠：请持机在空中缓慢画「8」字校准", warn = true)
        }

        Spacer(Modifier.height(14.dp))
        if (lk == null) {
            ActionButton(
                text = "定　盘",
                enabled = canLock,
                primary = true,
                contentDesc = "shinefs_lock_button",
            ) { doLock() }
            if (!canLock && uiState.capability.level == CompassCapabilityLevel.FULL) {
                Text(
                    text = lockHint(compass),
                    color = ShineColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp),
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionButton(
                    text = "重新测量",
                    enabled = true,
                    primary = false,
                    modifier = Modifier.weight(1f),
                ) {
                    locked = null
                }
                ActionButton(
                    text = "起 卦",
                    enabled = true,
                    primary = true,
                    modifier = Modifier.weight(1f),
                    contentDesc = "shinefs_cast_button",
                ) { onCast(lk) }
            }
        }
        Spacer(Modifier.height(10.dp))
        HintCard("校准说明", "若指针漂移或精度提示不可靠，请远离金属桌面、音箱、磁吸手机壳与汽车，并持机画 8 字校准后再定盘。")
    }
}

@Composable
private fun SealOverlay(reducedMotion: Boolean) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(if (reducedMotion) 300 else 1100)) }
    val appear = (progress.value / 0.4f).coerceAtMost(1f)
    val fade = if (progress.value > 0.4f) 1f - (progress.value - 0.4f) / 0.6f else 1f
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(bottom = 110.dp)
                .scale(0.7f + 0.5f * appear)
                .alpha(fade * 0.92f)
                .border(BorderStroke(3.dp, ShineColors.CinnabarBright), RoundedCornerShape(6.dp))
                .background(ShineColors.BackgroundDeep.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp),
        ) {
            Text(
                "定",
                color = ShineColors.CinnabarBright,
                fontFamily = FontFamily.Serif,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    enabled: Boolean,
    primary: Boolean,
    modifier: Modifier = Modifier,
    contentDesc: String = "shinefs_action_button",
    onClick: () -> Unit,
) {
    val border = when {
        !enabled -> ShineColors.Divider
        primary -> ShineColors.GoldPrimary
        else -> ShineColors.GoldMuted
    }
    val textColor = when {
        !enabled -> ShineColors.TextSecondary
        primary -> ShineColors.GoldBright
        else -> ShineColors.TextPrimary
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, border), RoundedCornerShape(10.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

private fun lockHint(c: CompassState): String = when {
    c.magneticInterference -> "磁场环境异常，已暂停定盘：请远离金属与磁体"
    c.tooTilted -> "请正对前方竖持手机，减少倾斜"
    c.stability != StabilityLevel.GOOD -> "稳定度需达到「良好」，请持稳手机"
    else -> "等待传感器就绪…"
}

/** 系统级"减少动画"设置（ANIMATOR_DURATION_SCALE == 0 时禁用装饰性动画）。 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        } catch (_: Exception) {
            false
        }
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
fun StatusRow(label: String, value: String, warn: Boolean = false) {
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

fun stabilityLabel(s: StabilityLevel): String = when (s) {
    StabilityLevel.UNSTABLE -> "不稳定（请持稳手机）"
    StabilityLevel.FAIR -> "一般"
    StabilityLevel.GOOD -> "良好 · 可定盘"
}
