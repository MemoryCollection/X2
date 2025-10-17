package cn.wi6.x2.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import cn.wi6.x2.utils.XLog.d
import cn.wi6.x2.utils.XLog.e
import com.ven.assists.AssistsCore
import com.ven.assists.utils.runMain
import com.ven.assists.window.AssistsWindowManager
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 安全随机点击（防抖 + 边界保护 + 小节点修正）
 * @param maxOffsetPercent 最大偏移百分比（默认 0.8f）
 * @param switchWindowIntervalDelay 窗口切换延迟（默认 250ms）
 * @param duration 点击持续时间（默认 25ms）
 * @param debounceInterval 防抖间隔（默认 500ms）
 * @param retryIfFail 是否重试失败点击（默认 true）
 */
suspend fun AccessibilityNodeInfo.randClicks(
    maxOffsetPercent: Float = 0.8f,
    switchWindowIntervalDelay: Long = 250,
    duration: Long = 25,
    debounceInterval: Long = 500,
    retryIfFail: Boolean = true
): Boolean {
    // 防抖逻辑
    System.currentTimeMillis().let { now ->
        if (now - SafeClick.lastClickTime < debounceInterval) {
            e("点击忽略：防抖触发（间隔=${now - SafeClick.lastClickTime}ms < $debounceInterval ms）")
            return false
        }
        SafeClick.lastClickTime = now
    }

    // 获取并校验节点矩形
    val rect = Rect().apply { getBoundsInScreen(this) }
    val (width, height) = rect.width() to rect.height()
    if (width <= 2 || height <= 2) {
        e("点击失败：节点尺寸无效（宽=$width, 高=$height）")
        return false
    }

    // 计算点击区域与随机偏移
    val isTiny = width < 40 || height < 40
    val safeBorder = 2
    val dynamicOffsetScale = if (isTiny) 0.3f else maxOffsetPercent
    val (maxXOffset, maxYOffset) = Pair(
        (width * dynamicOffsetScale).toInt().coerceAtLeast(1),
        (height * dynamicOffsetScale).toInt().coerceAtLeast(1)
    )

    // 计算随机点击坐标（带边界保护）
    val (cx, cy) = (rect.left + width / 2f) to (rect.top + height / 2f)
    var clickX = cx + Random.nextInt(-maxXOffset / 2, maxXOffset / 2)
    var clickY = cy + Random.nextInt(-maxYOffset / 2, maxYOffset / 2)

    // 边界修正
    val (clampedX, clampedY) = clickX.coerceIn(rect.left + safeBorder.toFloat(), rect.right - safeBorder.toFloat()) to
            clickY.coerceIn(rect.top + safeBorder.toFloat(), rect.bottom - safeBorder.toFloat())
    if (clickX != clampedX || clickY != clampedY) {
        d("⚠️ 点击修正：原坐标(${clickX}, ${clickY}) 限制至 (${clampedX}, ${clampedY})")
        clickX = clampedX
        clickY = clampedY
    }

    d("安全点击 => 坐标: X=$clickX, Y=$clickY | 节点=$rect | 小节点=$isTiny")

    // 点击执行函数
    suspend fun performClick(): Boolean {
        runMain { AssistsWindowManager.nonTouchableByAll() }
        delay(switchWindowIntervalDelay + Random.nextLong(5, 15))
        val ok = AssistsCore.gesture(
            startLocation = floatArrayOf(clickX, clickY),
            endLocation = floatArrayOf(clickX, clickY),
            startTime = 0,
            duration = duration
        )
        delay(switchWindowIntervalDelay + Random.nextLong(5, 15))
        runMain { AssistsWindowManager.touchableByAll() }
        return ok
    }

    // 执行点击并处理重试
    return runCatching { performClick() }.getOrElse {
        e("点击异常：${it.message}")
        false
    }.takeIf { it } ?: if (retryIfFail && isTiny) {
        delay(100)
        d("小节点点击失败，重试")
        performClick()
    } else {
        e("点击失败：手势执行无效")
        false
    }
}

/** 全局防抖控制器 */
object SafeClick {
    @Volatile
    var lastClickTime: Long = 0
}