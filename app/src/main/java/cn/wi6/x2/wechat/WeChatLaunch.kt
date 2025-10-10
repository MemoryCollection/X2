package cn.wi6.x2.wechat

import cn.wi6.x2.utils.ToastUtil
import cn.wi6.x2.utils.XLog.clearLogs
import com.blankj.utilcode.util.AppUtils.launchApp
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.click
import com.ven.assists.AssistsCore.getBoundsInScreen
import kotlinx.coroutines.delay

suspend fun WeChatLaunch(homeDetection: Boolean = true, maxRetries: Int = 5) {

    if (!AssistsCore.isAccessibilityServiceEnabled()) {
        ToastUtil.showLong("请先开启无障碍服务")
        throw Exception("请先开启无障碍服务")
    }
    clearLogs()

    launchApp(WeChatConfig.PACKAGE_NAME)
    delay(2000L)
    if (!homeDetection) {
        return
    }
    var retryCount = 0
    while (retryCount < maxRetries) {
        // 方案1：文字定位
        AssistsCore.findByText("通讯录").firstOrNull()?.let { wechatText ->
            val screen = wechatText.getBoundsInScreen()
            if (screen.left > AssistsCore.getX(1080, 340) &&
                screen.top > AssistsCore.getX(1920, 1850)) {
                wechatText.parent.parent?.click()
                return
            }
        }

        // 方案2：包名检查
        if (AssistsCore.getPackageName() == WeChatConfig.PACKAGE_NAME) {
            AssistsCore.back()
            delay(1500)
            retryCount++
            continue
        }

        // 重试逻辑
        val retryDelay = 1000L * (retryCount + 1)
        delay(retryDelay)
        retryCount++
    }
    throw Exception("微信启动失败")
}
