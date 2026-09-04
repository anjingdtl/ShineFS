package com.shinefs.app.ui.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.rotate
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
import com.shinefs.core.compass.PreCastGuidanceResolver
import com.shinefs.core.compass.PreCastReadiness
import com.shinefs.core.compass.StabilityLevel
import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.yijing.rules.Orientation
import com.shinefs.app.AppGraph
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
    var guideExpanded by remember(context) {
        mutableStateOf(
            !context.getSharedPreferences(HOLD_GUIDE_PREFS, android.content.Context.MODE_PRIVATE)
                .getBoolean(HOLD_GUIDE_COMPLETED, false),
        )
    }
    val haptics = LocalHapticFeedback.current

    // 时间盘（V2.0 方案 §30）：农历/年支/时辰/节气，每 2 秒刷新
    var timeCtx by remember { mutableStateOf<com.shinefs.core.calendar.model.YijingTimeContext?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            timeCtx = AppGraph.timeResolver.resolve(
                System.currentTimeMillis(), AppGraph.timeZone, AppGraph.dayBoundaryPolicy(),
            )
            kotlinx.coroutines.delay(2000)
        }
    }

    val displayAzimuth = locked?.azimuth ?: liveAzimuth
    val orientation = displayAzimuth?.let { runCatching { Orientation.fromAzimuth(it) }.getOrNull() }
    val readiness = uiState.readiness
    val canLock = uiState.capability.level == CompassCapabilityLevel.FULL &&
        liveAzimuth != null && locked == null && readiness.ready

    LaunchedEffect(readiness.ready) {
        if (readiness.ready && guideExpanded) {
            context.getSharedPreferences(HOLD_GUIDE_PREFS, android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(HOLD_GUIDE_COMPLETED, true)
                .apply()
            guideExpanded = false
        }
    }

    fun doLock() {
        val az = liveAzimuth ?: return
        val o = runCatching { Orientation.fromAzimuth(az) }.getOrNull() ?: return
        val timestamp = System.currentTimeMillis()
        val lockedTime = AppGraph.timeResolver.resolve(
            timestamp,
            AppGraph.timeZone,
            AppGraph.dayBoundaryPolicy(),
        )
        val snapshot = controller.captureSnapshot(
            capturedAt = timestamp,
            northReference = com.shinefs.core.compass.NorthReference.MAGNETIC,
            facingMountain = o.facingMountain,
            sittingMountain = o.sittingMountain,
            directionTrigram = o.facingTrigram.chineseName,
        ) ?: return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        locked = LockedReading(
            azimuth = snapshot.smoothedAzimuth ?: az,
            facingMountain = o.facingMountain,
            sittingMountain = o.sittingMountain,
            facingTrigram = o.facingTrigram.chineseName,
            facingElement = o.facingElement,
            timestamp = timestamp,
            stability = snapshot.stability.label,
            accuracy = snapshot.orientationAccuracy.label,
            magneticAccuracy = snapshot.magneticAccuracy.label,
            zoneId = lockedTime.zoneId,
            localDateTime = lockedTime.localDateTime,
            utcOffsetMinutes = lockedTime.utcOffsetMinutes,
            snapshot = snapshot,
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
                text = "已定盘 · ${lk.localDateTime?.replace('T', ' ') ?: formatLocalTime(lk.timestamp, lk.zoneId)}",
                color = ShineColors.CinnabarBright,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
                    .semantics { contentDescription = "shinefs_locked_badge" },
            )
        }
        Spacer(Modifier.height(12.dp))

        if (lk == null && uiState.capability.level == CompassCapabilityLevel.FULL) {
            HoldPoseGuideCard(
                compass = compass,
                pose = uiState.holdPose.pose,
                readiness = readiness,
                expanded = guideExpanded,
                reducedMotion = reducedMotion,
            )
            Spacer(Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
                .padding(12.dp)
                .semantics { contentDescription = "shinefs_time_panel" },
        ) {
            Text(
                "时间盘 · 传统农历历表",
                color = ShineColors.GoldPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
            )
            Spacer(Modifier.height(6.dp))
            StatusRow("农历", timeCtx?.lunarDisplay ?: "…")
            StatusRow("年干支", timeCtx?.let { "${it.yearStem.chinese}${it.yearBranch.chinese}年" } ?: "…")
            StatusRow("日干支", timeCtx?.dayGanzhi?.name ?: "…")
            StatusRow("时辰", timeCtx?.shichen?.display ?: "…")
            StatusRow("节气", timeCtx?.solarTerm?.term?.chinese ?: "…")
            StatusRow("时区", timeCtx?.let { "${it.zoneId} · UTC${offsetLabel(it.utcOffsetMinutes)}" } ?: "…")
        }
        Spacer(Modifier.height(12.dp))

        StatusRow("稳定度", stabilityLabel(compass.stability))
        StatusRow("持握姿态", holdPoseLabel(uiState.holdPose.pose))
        val poseAngles = uiState.holdPose.let { pose ->
            if (pose.pitchDeg.isFinite() && pose.rollDeg.isFinite()) {
                "pitch ${String.format(Locale.US, "%.1f", pose.pitchDeg)}° · " +
                    "roll ${String.format(Locale.US, "%.1f", pose.rollDeg)}°"
            } else {
                "等待姿态读数"
            }
        }
        StatusRow("姿态角", poseAngles)
        StatusRow("方位准确度", compass.orientationAccuracy.label)
        StatusRow("磁场准确度", compass.magneticAccuracy.label)
        StatusRow(
            "磁场环境",
            if (compass.magneticInterference) "异常（约 ${compass.magneticMagnitudeUt?.toInt()} 微特斯拉）" else "正常",
            warn = compass.magneticInterference,
        )
        if (lk == null && uiState.holdPose.stableMillis > 0L && !uiState.holdPose.valid) {
            StatusRow("姿态状态", "${uiState.holdPose.pose.label}，${uiState.holdPose.stableMillis}ms", warn = true)
        }
        if (lk == null && uiState.calibrationRecommended) {
            StatusRow("读数校正", "准确度不足：请持机在空中缓慢画「8」字，让读数恢复稳定", warn = true)
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
                        text = lockHint(compass, uiState.holdPose.pose, readiness),
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
        HintCard("使用提示", "若指针漂移或读数不稳，请远离金属桌面、音箱、磁吸手机壳与汽车，并持机画 8 字让读数稳定后再定盘。")
    }
}

@Composable
private fun HoldPoseGuideCard(
    compass: CompassState,
    pose: HoldPose,
    readiness: PreCastReadiness,
    expanded: Boolean,
    reducedMotion: Boolean,
) {
    val guidance = PreCastGuidanceResolver.resolve(
        compass = compass,
        pose = com.shinefs.core.compass.pose.HoldPoseState(pose = pose),
        readiness = readiness,
    )
    val transition = if (reducedMotion) {
        null
    } else {
        rememberInfiniteTransition(label = "holdPoseGuide")
    }
    val sway = transition?.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "holdPoseGuideSway",
    )?.value ?: 0f
    val phoneRotation = when (pose) {
        HoldPose.FLAT -> 0f
        HoldPose.UPRIGHT -> 0f
        HoldPose.TRANSITION -> 28f
        HoldPose.INVALID -> 16f
    } + sway
    val phoneWidth = if (pose == HoldPose.UPRIGHT) 32.dp else 54.dp
    val phoneHeight = if (pose == HoldPose.UPRIGHT) 54.dp else 32.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShineColors.BackgroundRaised, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "shinefs_hold_pose_guide"
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .width(phoneWidth)
                    .height(phoneHeight)
                    .rotate(phoneRotation)
                    .border(BorderStroke(1.5.dp, if (guidance.ready) ShineColors.GoldPrimary else ShineColors.GoldMuted), RoundedCornerShape(7.dp))
                    .background(ShineColors.BackgroundDeep, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (guidance.ready) "✓" else "·",
                    color = if (guidance.ready) ShineColors.GoldBright else ShineColors.CinnabarBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (expanded) "起卦前持握引导" else "起卦前状态",
                    color = ShineColors.GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = guidance.headline,
                    color = if (guidance.ready) ShineColors.GoldBright else ShineColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = guidance.detail,
                    color = ShineColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        if (expanded && !guidance.ready) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "姿态、稳定度、磁场和传感器精度满足后，定盘按钮会自动通过。",
                color = ShineColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
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

private fun lockHint(c: CompassState, pose: HoldPose, readiness: PreCastReadiness): String = when {
    c.magneticInterference -> "磁场环境异常，已暂停定盘：请远离金属与磁体"
    pose == HoldPose.FLAT || pose == HoldPose.UPRIGHT ->
        readiness.primaryReason
    pose == HoldPose.TRANSITION -> "请将手机再放平一些或再竖直一些"
    pose == HoldPose.INVALID -> "当前姿态无效，请让屏幕朝上或竖直持握"
    c.stability != StabilityLevel.GOOD -> "稳定度需达到「良好」，请持稳手机"
    else -> "等待罗盘读数稳定…"
}

private fun holdPoseLabel(pose: HoldPose): String = when (pose) {
    HoldPose.FLAT -> "平放 · 已识别"
    HoldPose.UPRIGHT -> "竖持 · 已识别"
    HoldPose.TRANSITION -> "过渡态 · 请调整"
    HoldPose.INVALID -> "无效 · 请调整"
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
            "当前设备暂不支持完整电子罗盘功能，本页仅提供有限使用方式。",
            color = ShineColors.CinnabarBright,
            fontSize = 13.sp,
        )
        Text(
            "暂不显示方位读数；后续可在起卦页手动填写方位。",
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

private fun formatLocalTime(timestamp: Long, zoneId: String?): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    format.timeZone = java.util.TimeZone.getTimeZone(zoneId ?: AppGraph.timeZone.id)
    return format.format(Date(timestamp))
}

private fun offsetLabel(minutes: Int): String {
    val sign = if (minutes >= 0) "+" else "-"
    val absolute = kotlin.math.abs(minutes)
    return "$sign${absolute / 60}:${(absolute % 60).toString().padStart(2, '0')}"
}

private const val HOLD_GUIDE_PREFS = "shinefs_compass_guide"
private const val HOLD_GUIDE_COMPLETED = "hold_pose_guide_completed"
