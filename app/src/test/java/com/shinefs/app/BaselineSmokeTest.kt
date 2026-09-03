package com.shinefs.app

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cycle 00 测试基线：仅验证 JVM 单元测试通道可用。
 * 业务规则测试自 Cycle 01 起进入 core/yijing 模块，本测试届时可移除。
 */
class BaselineSmokeTest {

    @Test
    fun junitHarnessRunsOnJvm() {
        assertEquals(2, 1 + 1)
    }
}
