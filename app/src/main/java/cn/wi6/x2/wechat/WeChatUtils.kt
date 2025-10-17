package cn.wi6.x2.wechat

import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.wechat.WeChatConfig.GROUP_RULES
import cn.wi6.x2.wechat.WeChatConfig.WHITELIST
import com.ven.assists.AssistsCore.findByTags
import com.ven.assists.AssistsCore.scrollForward
import cn.wi6.x2.utils.XLog.d as XLog

/**
 * 根据群聊名称获取组名
 * @param chatName 群聊名称
 * @return 组名
 */
fun getGroupName(chatName: String): String? {
    // 白名单处理
    val whiteListKeywords = WHITELIST.split("|")
    if (whiteListKeywords.any { chatName.contains(it) }) {
        return null
    }

    // 分组规则处理
    val lines = GROUP_RULES.trimIndent().lines()
    for (line in lines) {
        val parts = line.split("|").map { it.trim() }
        if (parts.size < 2) continue
        val groupName = parts[0]
        val keywords = parts.subList(1, parts.size)
        if (keywords.any { chatName.contains(it) }) {
            return groupName
        }
    }

    // 没匹配到返回“未分”
    return "未分"
}


/**
 * 尝试滚动指定列表，若已到底部返回 false
 * @param listId 列表控件 ID
 * @return true 表示成功滚动加载下一批，false 表示已到底部
 */
suspend fun scrollListForward(listId: String): Boolean {
    // 从列表中取第一个非空的ListView
    val listView = findByTags("android.widget.ListView").firstOrNull()

    // 区分“未找到列表”和“滚动失败”两种情况
    val scrollSuccess = if (listView != null) {
        listView.scrollForward() // 假设scrollForward()返回Boolean
    } else {
        XLog("未找到ListView实例，无法滚动")
        false
    }

    // 仅在找到列表时记录滚动状态
    if (listView != null) {
        XLog("页面滚动状态：${if (scrollSuccess) "成功（加载下一批）" else "失败（已到最底部）"}")
    }

    randDelay()
    return scrollSuccess
}

