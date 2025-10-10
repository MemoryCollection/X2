package cn.wi6.x2.wechat

import cn.wi6.x2.utils.XLog
import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.wechat.group.groupAddressBookDetection
import cn.wi6.x2.wechat.sport.sportsLikes

/**
 * 微信运动点赞步骤
 */
suspend fun wechatExerciseSteps() {
    try {
        WeChatLaunch(homeDetection = false)
        randDelay(2000)
        sportsLikes()
    } catch (e: Exception) {
        e.message?.let { XLog.e(it) }

    }
}

/**
 * 微信群发步骤
 */
suspend fun wechatGroupSend() {
    try {
        WeChatLaunch(homeDetection = true)
        randDelay(2000)
        groupAddressBookDetection()
    } catch (e: Exception) {
        e.message?.let { XLog.e(it) }

    }
}