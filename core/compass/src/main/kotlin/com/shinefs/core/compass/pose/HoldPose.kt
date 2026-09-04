package com.shinefs.core.compass.pose

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.min

/** 手机当前持握姿态；姿态本身不参与磁北计算，只决定采用哪套坐标解释。 */
enum class HoldPose(val label: String) {
    FLAT("平放"),
    UPRIGHT("竖持"),
    TRANSITION("过渡"),
    INVALID("无效"),
}

/** 姿态检测的可追溯输出。角度沿用 Android orientation 的度数口径。 */
data class HoldPoseState(
    val pose: HoldPose = HoldPose.INVALID,
    val confidence: Float = 0f,
    val pitchDeg: Float = Float.NaN,
    val rollDeg: Float = Float.NaN,
    val stableMillis: Long = 0L,
    val screenNormalVerticalComponent: Float? = null,
    val updatedAtElapsedMillis: Long = 0L,
) {
    val valid: Boolean get() = pose == HoldPose.FLAT || pose == HoldPose.UPRIGHT
}

/**
 * 基于屏幕法向量/重力与 pitch、roll 的姿态检测器。
 *
 * screenNormalVerticalComponent 为屏幕法向量在世界竖直轴上的分量：
 * +1 约等于屏幕朝上平放，0 约等于屏幕竖直，负值代表屏幕朝下风险。
 * 当上层只有 pitch/roll 时，使用两者的合成倾角作降级判断。
 * 姿态切换需要持续 [settleMillis]，并对已确认姿态采用退出迟滞。
 */
class HoldPoseDetector(
    private val flatEnterDeg: Float = 20f,
    private val flatExitDeg: Float = 30f,
    private val uprightEnterDeg: Float = 70f,
    private val uprightExitDeg: Float = 60f,
    private val settleMillis: Long = 800L,
    private val minGravityMagnitude: Float = 6f,
    private val maxGravityMagnitude: Float = 14f,
    private val screenDownThreshold: Float = -0.35f,
    private val violentDeltaDeg: Float = 45f,
    private val violentWindowMillis: Long = 250L,
) {
    init {
        require(flatEnterDeg in 0f..flatExitDeg)
        require(flatExitDeg < uprightExitDeg)
        require(uprightExitDeg <= uprightEnterDeg)
        require(settleMillis >= 0L)
        require(minGravityMagnitude > 0f && maxGravityMagnitude > minGravityMagnitude)
        require(screenDownThreshold < 0f)
        require(violentDeltaDeg > 0f && violentWindowMillis > 0L)
    }

    private var committedPose = HoldPose.INVALID
    private var candidatePose = HoldPose.INVALID
    private var candidateSince = 0L
    private var lastTiltDeg = Float.NaN
    private var lastTiltAt = 0L

    @Synchronized
    fun update(
        pitchDeg: Float,
        rollDeg: Float,
        nowElapsedMillis: Long,
        screenNormalVerticalComponent: Float? = null,
        gravityMagnitude: Float? = null,
    ): HoldPoseState {
        val safeNow = nowElapsedMillis.coerceAtLeast(0L)
        val validAngles = pitchDeg.isFinite() && rollDeg.isFinite()
        val gravityValid = gravityMagnitude == null ||
            (gravityMagnitude.isFinite() && gravityMagnitude in minGravityMagnitude..maxGravityMagnitude)
        val normalValid = screenNormalVerticalComponent == null || screenNormalVerticalComponent.isFinite()

        val tiltDeg = when {
            !validAngles -> Float.NaN
            screenNormalVerticalComponent != null && normalValid -> {
                Math.toDegrees(
                    acos(screenNormalVerticalComponent.coerceIn(-1f, 1f).toDouble()),
                ).toFloat()
            }
            else -> hypot(pitchDeg.toDouble(), rollDeg.toDouble()).toFloat()
        }

        val violentChange = tiltDeg.isFinite() && lastTiltDeg.isFinite() &&
            safeNow > lastTiltAt && safeNow - lastTiltAt <= violentWindowMillis &&
            abs(tiltDeg - lastTiltDeg) >= violentDeltaDeg
        val measured = if (!validAngles || !gravityValid || !normalValid || !tiltDeg.isFinite() || violentChange) {
            HoldPose.INVALID
        } else if (screenNormalVerticalComponent != null &&
            screenNormalVerticalComponent <= screenDownThreshold
        ) {
            HoldPose.INVALID
        } else if (tiltDeg > 105f) {
            HoldPose.INVALID
        } else {
            when {
                tiltDeg <= flatEnterDeg -> HoldPose.FLAT
                tiltDeg >= uprightEnterDeg -> HoldPose.UPRIGHT
                else -> HoldPose.TRANSITION
            }
        }

        if (tiltDeg.isFinite()) {
            lastTiltDeg = tiltDeg
            lastTiltAt = safeNow
        }

        val target = targetForMeasured(measured, tiltDeg)
        val outputPose = settle(target, safeNow)
        val stableMillis = when {
            outputPose == HoldPose.INVALID -> 0L
            target == outputPose -> (safeNow - candidateSince).coerceAtLeast(0L)
            else -> (safeNow - candidateSince).coerceAtLeast(0L).coerceAtMost(settleMillis)
        }

        return HoldPoseState(
            pose = outputPose,
            confidence = confidence(outputPose, measured, tiltDeg, stableMillis),
            pitchDeg = pitchDeg,
            rollDeg = rollDeg,
            stableMillis = stableMillis,
            screenNormalVerticalComponent = screenNormalVerticalComponent,
            updatedAtElapsedMillis = safeNow,
        )
    }

    @Synchronized
    fun reset() {
        committedPose = HoldPose.INVALID
        candidatePose = HoldPose.INVALID
        candidateSince = 0L
        lastTiltDeg = Float.NaN
        lastTiltAt = 0L
    }

    private fun targetForMeasured(measured: HoldPose, tiltDeg: Float): HoldPose {
        if (measured == HoldPose.INVALID) return HoldPose.INVALID
        return when (committedPose) {
            HoldPose.FLAT -> when {
                tiltDeg <= flatExitDeg -> HoldPose.FLAT
                else -> measured
            }
            HoldPose.UPRIGHT -> when {
                tiltDeg >= uprightExitDeg -> HoldPose.UPRIGHT
                else -> measured
            }
            else -> measured
        }
    }

    private fun settle(target: HoldPose, now: Long): HoldPose {
        if (target == HoldPose.INVALID) {
            committedPose = HoldPose.INVALID
            candidatePose = HoldPose.INVALID
            candidateSince = now
            return HoldPose.INVALID
        }

        if (target == committedPose && target != HoldPose.TRANSITION) {
            candidatePose = target
            return target
        }

        if (target == HoldPose.TRANSITION) {
            candidatePose = HoldPose.TRANSITION
            candidateSince = now
            return HoldPose.TRANSITION
        }

        if (candidatePose != target) {
            candidatePose = target
            candidateSince = now
        }
        if (now - candidateSince >= settleMillis) {
            committedPose = target
            return target
        }
        return HoldPose.TRANSITION
    }

    private fun confidence(
        output: HoldPose,
        measured: HoldPose,
        tiltDeg: Float,
        stableMillis: Long,
    ): Float {
        if (output == HoldPose.INVALID || !tiltDeg.isFinite()) return 0f
        val geometric = when (output) {
            HoldPose.FLAT -> ((flatExitDeg - tiltDeg) / flatExitDeg).coerceIn(0f, 1f)
            HoldPose.UPRIGHT -> ((tiltDeg - uprightExitDeg) / (90f - uprightExitDeg)).coerceIn(0f, 1f)
            HoldPose.TRANSITION -> {
                val distance = min(abs(tiltDeg - flatEnterDeg), abs(uprightEnterDeg - tiltDeg))
                (1f - distance / ((uprightEnterDeg - flatEnterDeg) / 2f)).coerceIn(0f, 1f)
            }
            HoldPose.INVALID -> 0f
        }
        val temporal = if (output == measured || output == HoldPose.TRANSITION) {
            (stableMillis.toFloat() / settleMillis.coerceAtLeast(1L)).coerceIn(0f, 1f)
        } else {
            0f
        }
        return if (output == HoldPose.TRANSITION) {
            (0.5f * geometric + 0.5f * temporal).coerceIn(0f, 1f)
        } else {
            (0.7f * geometric + 0.3f * temporal).coerceIn(0f, 1f)
        }
    }
}
