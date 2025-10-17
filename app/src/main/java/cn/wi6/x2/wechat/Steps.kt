package cn.wi6.x2.wechat

import cn.wi6.x2.utils.FloatingControllerManager
import cn.wi6.x2.utils.XLog
import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.wechat.group.openTheGroupAddressBook
import cn.wi6.x2.wechat.group.update.groupSelection
import cn.wi6.x2.wechat.sport.sportsLikes

/**
 * 微信运动点赞步骤
 */
suspend fun wechatExerciseSteps() {
    try {
        FloatingControllerManager.getController().show()
        WeChatLaunch(homeDetection = false)
        randDelay(2000)
        sportsLikes()
    } catch (e: Exception) {
        e.message?.let { XLog.e(it) }
    } finally {
        try {
            FloatingControllerManager.getController().hide()
            XLog.d("悬浮窗已隐藏")
        } catch (e: Exception) {
            // 单独处理隐藏时的异常，避免影响主流程
            XLog.e("隐藏悬浮窗失败：${e.message}", e)
        }
    }
}

/**
 * 更新群消息信息
 */
suspend fun updateGroupDetails() {
    try {
        FloatingControllerManager.getController().show()
        WeChatLaunch(homeDetection = true)
        randDelay(2000)
        groupSelection()
        WeChatLaunch(homeDetection = true)
        randDelay(2000)
        openTheGroupAddressBook(whetherToUpdate = true)
    } catch (e: Exception) {
        e.message?.let { XLog.e(it) }
    } finally {
        try {
            FloatingControllerManager.getController().hide()
            XLog.d("悬浮窗已隐藏")
        } catch (e: Exception) {
            // 单独处理隐藏时的异常，避免影响主流程
            XLog.e("隐藏悬浮窗失败：${e.message}", e)
        }
    }
}