package com.shinefs.core.divination.context

import com.shinefs.core.calendar.model.YijingTimeContext
import com.shinefs.core.compass.NorthReference
import com.shinefs.core.compass.SensorAccuracyState
import com.shinefs.core.yijing.model.Trigram

/**
 * 时空合参上下文（V2.0 方案 §7）：时间 + 空间 + 事件。
 * 空间数据**不修改时间卦**（方案 §10）；event 预留给物象占（后天端法）。
 */
data class YijingMomentContext(
    val time: YijingTimeContext,
    val space: YijingSpaceContext?,
    val event: DivinationEvent?,
    val capturedAt: Long,
)

/** 空间上下文：罗盘锁定读数的结构化产物（方位角 + 二十四山 + 方位卦 + 坐向）。 */
data class YijingSpaceContext(
    val rawAzimuth: Float?,
    val smoothedAzimuth: Float?,
    val northReference: NorthReference,
    val facingMountain: String?,
    val sittingMountain: String?,
    val directionTrigram: Trigram?,
    val sensorAccuracy: SensorAccuracyState?,
    val stable: Boolean,
    val magneticInterference: Boolean,
)

/** 占测事件（物象占用）：版本化类象表条目 id + 展示名。 */
data class DivinationEvent(
    val classImageId: String,
    val label: String,
)
