package cn.wi6.x2.wechat

import cn.wi6.x2.utils.XLog.clearLogs
import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.wechat.sport.SportsLikes

/**
 * 微信运动点赞步骤
 */
suspend fun wechatExerciseSteps() {
    clearLogs()
    WeChatLaunch(homeDetection = false)
    randDelay(2000)
    SportsLikes()
}