package cn.wi6.x2.wechat.sport

import cn.wi6.x2.utils.randClicks
import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.utils.XLog.e as XLogE
import cn.wi6.x2.utils.XLog.d as XLog
import cn.wi6.x2.wechat.WeChatConfig.LIST_OF_SPORTS
import cn.wi6.x2.wechat.WeChatConfig.SPORTS_HEARTS
import cn.wi6.x2.wechat.WeChatConfig.SPORT_FILTER_KEYWORD
import cn.wi6.x2.wechat.WeChatConfig.SPORT_MIN_STEPS_THRESHOLD
import cn.wi6.x2.wechat.WeChatConfig.SPORT_NAME
import cn.wi6.x2.wechat.WeChatConfig.SPORT_NUMBER
import cn.wi6.x2.wechat.scrollListForward
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.isVisible

// 运动点赞
suspend fun sportsLikes() {
    var skippedCount = 0
    var likedCount = 0
    var totalProcessed = 0
    val processedElements = mutableSetOf<String>()
    var shouldStop = false // 新增：终止循环的标志位（默认不终止）

    try {
        // 外层循环：滚动加载下一批元素（新增条件：!shouldStop，确保触发终止后停止）
        while (!shouldStop && findById(LIST_OF_SPORTS).firstOrNull() != null) {
            val allLikeHearts = findById(SPORTS_HEARTS)
            val sportNames = findById(SPORT_NAME)
            val exerciseNumbers = findById(SPORT_NUMBER)

            // 空判断：无元素则终止（避免无效循环）
            if (allLikeHearts.isEmpty()) {
                XLog("当前页面未获取到点赞按钮，终止循环")
                break
            }

            // 验证元素数量匹配
            require(allLikeHearts.size == sportNames.size && allLikeHearts.size == exerciseNumbers.size) {
                "UI元素数量不匹配：点赞按钮(${allLikeHearts.size})、运动名称(${sportNames.size})、运动数(${exerciseNumbers.size})"
            }

            totalProcessed += allLikeHearts.size

            // 内层遍历：处理当前批次元素（通过 shouldStop 控制是否终止）
            allLikeHearts.forEachIndexed { index, likeButton ->
                if (shouldStop) return@forEachIndexed // 若已触发终止，直接跳出内层遍历

                val sportName = sportNames[index].text.toString()
                val numberText = exerciseNumbers[index].text.toString()
                val uniqueKey = "$sportName-$numberText"

                // 跳过已处理的元素
                if (processedElements.contains(uniqueKey)) {
                    XLog("跳过已处理元素：$sportName（运动数：$numberText）")
                    skippedCount++
                    return@forEachIndexed
                }

                // 关键修改：检测到运动数＜1000，触发终止循环
                val exerciseNumber = numberText.filter { it.isDigit() }.toLongOrNull() ?: 0L
                when {
                    sportName.contains(SPORT_FILTER_KEYWORD) -> {
                        XLog("跳过（包含关键词）：$sportName（运动数：$numberText）")
                        skippedCount++
                        processedElements.add(uniqueKey)
                        return@forEachIndexed
                    }
                    // 2. 仅当不包含"吴东"且步数<1000时，才终止循环
                    exerciseNumber < SPORT_MIN_STEPS_THRESHOLD -> {
                        XLog("⚠️  遇到非吴东且运动数＜1000的元素：$sportName（运动数：$numberText），终止整个循环")
                        skippedCount++
                        processedElements.add(uniqueKey)
                        shouldStop = true // 标记终止
                        return@forEachIndexed
                    }
                }

                // 执行点赞（正常逻辑）()
                if(likeButton.isVisible()){
                    likeButton.randClicks()
                    likedCount++
                    processedElements.add(uniqueKey)
                    XLog("点赞成功：$sportName（运动数：$numberText）")
                    randDelay(500, 800)
                }
            }

            if (!shouldStop) {
                val canScroll = scrollListForward(LIST_OF_SPORTS)
                if (!canScroll) break
            }
        }

        // 循环结束日志（区分“正常完成”和“触发终止”）
        XLog("=== 点赞任务结束 ===")
        XLog("终止原因：${if (shouldStop) "遇到运动数＜1000的元素" else "无更多元素或滚动失败"}")
        XLog("总处理元素数：$totalProcessed")
        XLog("成功点赞数：$likedCount")
        XLog("跳过元素数：$skippedCount")

    } catch (e: IllegalArgumentException) {
        XLogE("点赞异常（数据不一致）：${e.message}")
    } catch (e: Exception) {
        XLogE("点赞异常（未知错误）：${e.message}")
    }
}
