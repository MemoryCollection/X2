package cn.wi6.x2.utils

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 极致简洁版随机延时工具
 * @param baseOrMin 基础时间或最小时间
 * @param max 最大时间
 * @param onProgress 进度回调
 * @return 延时时间
 *
 */
suspend fun randDelay(
    baseOrMin: Long? = null,
    max: Long? = null,
    onProgress: ((Int) -> Unit)? = null
): Long {
    val (min, maxVal) = when {
        // 情况1：两个参数都提供，表示范围
        baseOrMin != null && max != null -> {
            require(baseOrMin > 0) { "最小延时时间必须大于0" }
            require(max > baseOrMin) { "最大延时时间必须大于最小延时时间" }
            baseOrMin to max
        }

        // 情况2：只提供一个参数，表示基础时间±1秒
        baseOrMin != null -> {
            require(baseOrMin > 0) { "基础时间必须大于0" }
            val minVal = maxOf(100, baseOrMin - 1000)
            val maxVal = baseOrMin + 1000
            minVal to maxVal
        }

        // 情况3：无参数，默认1-2秒
        else -> 1000L to 2000L
    }

    val delayTime = Random.nextLong(min, maxVal)

    if (onProgress != null) {
        val startTime = System.currentTimeMillis()
        var elapsed = 0L
        val interval = 100L

        while (elapsed < delayTime) {
            val progress = (elapsed.toFloat() / delayTime * 100).toInt().coerceIn(0, 100)
            onProgress(progress)
            delay(interval)
            elapsed = System.currentTimeMillis() - startTime
        }
        onProgress(100)
    } else {
        delay(delayTime)
    }

    return delayTime
}