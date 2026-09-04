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
