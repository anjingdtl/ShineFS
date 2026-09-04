package com.shinefs.core.compass

import com.shinefs.core.compass.pose.HoldPose
import com.shinefs.core.compass.pose.HoldPoseState

/** 起卦前轻量门禁的结构化结果；UI 应显示 reasons 中的具体原因。 */
data class PreCastReadiness(
    val validPose: Boolean = false,
    val stable: Boolean = false,
    val magneticOk: Boolean = false,
    val sensorAccuracyOk: Boolean = false,
    val ready: Boolean = false,
    val reasons: List<String> = emptyList(),
) {
    val primaryReason: String get() = reasons.firstOrNull() ?: "等待传感器读数…"
}

/** 起卦前动态引导的纯状态文案；不把 UI 判断散落到传感器回调中。 */
data class PreCastGuidance(
    val headline: String,
    val detail: String,
    val ready: Boolean,
)

object PreCastGuidanceResolver {
    fun resolve(compass: CompassState, pose: HoldPoseState, readiness: PreCastReadiness): PreCastGuidance {
        if (readiness.ready) {
            return PreCastGuidance(
                headline = "姿态正确，已自动通过",
                detail = "保持当前姿势即可定盘",
                ready = true,
            )
        }

        if (compass.magneticInterference) {
            return PreCastGuidance(
                headline = "请远离磁场干扰",
                detail = "远离磁铁、金属桌面、音箱或无线充电器后再保持稳定",
                ready = false,
            )
        }

        if (!readiness.validPose) {
            return when (pose.pose) {
                HoldPose.TRANSITION -> PreCastGuidance(
                    headline = "请继续调整持握姿态",
                    detail = "将手机平放屏幕朝上，或竖直持握手机顶部",
                    ready = false,
                )
                HoldPose.INVALID -> PreCastGuidance(
                    headline = "当前姿态无效",
                    detail = "请让屏幕朝上平放，或竖直持握并避免屏幕朝下",
                    ready = false,
                )
                else -> PreCastGuidance(
                    headline = "请调整持握姿态",
                    detail = "将手机平放或竖直持握",
                    ready = false,
                )
            }
        }

        if (!readiness.stable) {
            return PreCastGuidance(
                headline = "请保持稳定",
                detail = "保持 ${pose.pose.label}，等待罗盘读数稳定",
                ready = false,
            )
        }

        if (!readiness.magneticOk) {
            return PreCastGuidance(
                headline = "等待磁场读数",
                detail = "请保持手机远离磁性物体",
                ready = false,
            )
        }

        if (!readiness.sensorAccuracyOk) {
            return PreCastGuidance(
                headline = "请先校正传感器",
                detail = "在空中缓慢画“8”字，等准确度恢复后自动通过",
                ready = false,
            )
        }

        return PreCastGuidance(
            headline = readiness.primaryReason,
            detail = "完成后定盘按钮会自动可用",
            ready = false,
        )
    }
}

object PreCastReadinessEvaluator {
    fun evaluate(compass: CompassState, pose: HoldPoseState): PreCastReadiness {
        val validPose = pose.pose == HoldPose.FLAT || pose.pose == HoldPose.UPRIGHT
        val stable = compass.stability == StabilityLevel.GOOD
        val magneticOk = !compass.magneticInterference && compass.magneticMagnitudeUt != null
        val sensorAccuracyOk = compass.orientationAccuracy !in setOf(
            SensorAccuracy.NO_CONTACT,
            SensorAccuracy.UNRELIABLE,
        ) && compass.magneticAccuracy !in setOf(
            SensorAccuracy.NO_CONTACT,
            SensorAccuracy.UNRELIABLE,
        )
        val reasons = buildList {
            if (!validPose) add("请调整持握姿态")
            if (!stable) add("请保持稳定")
            if (!magneticOk) add(if (compass.magneticInterference) "磁场干扰" else "等待磁场读数")
            if (!sensorAccuracyOk) add("传感器精度不足")
        }
        return PreCastReadiness(
            validPose = validPose,
            stable = stable,
            magneticOk = magneticOk,
            sensorAccuracyOk = sensorAccuracyOk,
            ready = validPose && stable && magneticOk && sensorAccuracyOk,
            reasons = reasons,
        )
    }
}
