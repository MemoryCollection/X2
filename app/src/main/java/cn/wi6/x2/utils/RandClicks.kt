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
 * 安全随机点击：自动生成偏移量并打印每次点击位置
 * @param minOffset 最小偏移量（像素）
 * @param maxOffsetPercent 最大偏移量占节点宽高的比例（0~1）
 * @param switchWindowIntervalDelay 切换窗口的延迟（ms）
 * @param duration 点击持续时间（ms）
 * @return true 表示点击成功，false 表示点击失败
 */
suspend fun AccessibilityNodeInfo.randClicks(
    minOffset: Int = 10,
    maxOffsetPercent: Float = 0.8f,
    switchWindowIntervalDelay: Long = 250,
    duration: Long = 25
): Boolean {
    val nodeRect = Rect()
    this.getBoundsInScreen(nodeRect)
    val nodeWidth = nodeRect.width()
    val nodeHeight = nodeRect.height()

    if (nodeWidth <= 0 || nodeHeight <= 0) {
        e("点击失败：节点尺寸无效（宽=$nodeWidth, 高=$nodeHeight）")
        return false
    }

    // 计算 X 偏移
    val actualXOffset = if (nodeWidth <= minOffset * 2) {
        nodeWidth / 2f
    } else {
        val maxOffset = ((nodeWidth - 2) * maxOffsetPercent).toInt().coerceAtMost(nodeWidth - 2)
        Random.nextInt(minOffset, maxOffset + 1).toFloat()
    }

    // 计算 Y 偏移
    val actualYOffset = if (nodeHeight <= minOffset * 2) {
        nodeHeight / 2f
    } else {
        val maxOffset = ((nodeHeight - 2) * maxOffsetPercent).toInt().coerceAtMost(nodeHeight - 2)
        Random.nextInt(minOffset, maxOffset + 1).toFloat()
    }

    // 最终点击坐标，缩紧边界 1px
    val clickX = (nodeRect.left + actualXOffset).coerceIn(
        nodeRect.left.toFloat() + 1,
        nodeRect.right.toFloat() - 2
    )
    val clickY = (nodeRect.top + actualYOffset).coerceIn(
        nodeRect.top.toFloat() + 1,
        nodeRect.bottom.toFloat() - 2
    )

    // 打印点击信息
    d( "点击坐标：X=$clickX, Y=$clickY | 偏移：X=$actualXOffset, Y=$actualYOffset | 节点范围=$nodeRect")

    return runCatching {
        runMain { AssistsWindowManager.nonTouchableByAll() }
        delay(switchWindowIntervalDelay + Random.nextLong(5, 15))

        val clickResult = AssistsCore.gesture(
            startLocation = floatArrayOf(clickX, clickY),
            endLocation = floatArrayOf(clickX, clickY),
            startTime = 0,
            duration = duration
        )

        delay(switchWindowIntervalDelay + Random.nextLong(5, 15))
        runMain { AssistsWindowManager.touchableByAll() }

        if (!clickResult) e("点击失败：手势执行未成功")
        clickResult
    }.getOrDefault(false).also {
        if (!it) e("点击失败：发生异常")
    }
}
