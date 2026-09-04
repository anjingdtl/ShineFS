package com.shinefs.app.sensor

import android.content.Context
import android.view.WindowManager

/** 读取当前 Display Rotation；不以 Activity 锁定的 portrait 配置替代真实屏幕旋转。 */
fun interface DisplayRotationProvider {
    fun currentRotation(): Int
}

class AndroidDisplayRotationProvider(context: Context) : DisplayRotationProvider {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @Suppress("DEPRECATION")
    override fun currentRotation(): Int = windowManager.defaultDisplay.rotation
}
