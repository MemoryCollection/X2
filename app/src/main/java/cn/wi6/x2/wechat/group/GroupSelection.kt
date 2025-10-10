package cn.wi6.x2.wechat.group

import cn.wi6.x2.App.Companion.globalContext
import cn.wi6.x2.utils.createGroupInfo
import cn.wi6.x2.utils.XLog.d as XLog
import cn.wi6.x2.utils.XLog.e as XLogE
import cn.wi6.x2.utils.randDelay
import cn.wi6.x2.utils.randClicks
import cn.wi6.x2.wechat.WeChatConfig
import cn.wi6.x2.wechat.WeChatConfig.HOME_MORE_FEATURES
import cn.wi6.x2.wechat.WeChatConfig.LIST_FRAMEWORK
import cn.wi6.x2.wechat.WeChatConfig.LIST_GROUP_NAME
import cn.wi6.x2.wechat.WeChatConfig.LIST_GROUP_NUMBER
import cn.wi6.x2.wechat.WeChatConfig.SELECT_AN_EXISTING_GROUP
import cn.wi6.x2.wechat.WeChatConfig.START_A_GROUP_CHAT
import cn.wi6.x2.wechat.getGroupName
import cn.wi6.x2.wechat.scrollListForward
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.findByText

// 群聊选择 查询已存在的群聊
suspend fun groupSelection() {
    val groupsToUpsert = mutableListOf<cn.wi6.x2.utils.GroupInfo>()
    val db = cn.wi6.x2.utils.GroupDatabase(globalContext)
    var shouldStop = false

    try {
        // 更多功能
        findById(HOME_MORE_FEATURES).firstOrNull()?.randClicks()
        randDelay(2000)
        // 发起群聊
        findByText(START_A_GROUP_CHAT).firstOrNull()?.randClicks()
        randDelay(2000)
        // 选择一个已有的群
        findByText(SELECT_AN_EXISTING_GROUP).firstOrNull()?.randClicks()
        randDelay(3000)

        do {
            val groupNames = findById(LIST_GROUP_NAME)
            val groupNumbers = findById(LIST_GROUP_NUMBER)

            if (groupNames.isEmpty()) {
                XLog("当前页面未获取到群名称")
                shouldStop = true
            } else {
                require(groupNames.size == groupNumbers.size) {
                    "UI元素数量不匹配：群名称(${groupNames.size})、群人数(${groupNumbers.size})"
                }

                groupNames.forEachIndexed { index, groupNameElement ->
                    val groupNameText = groupNameElement.text.toString().trim()
                    val groupNumber = groupNumbers[index].text.toString().filter { it.isDigit() }.toLongOrNull() ?: 0

                    if (groupNumber >= WeChatConfig.LEAST_GROUP_SIZE) {
                        // 仅检查本次扫描列表是否重复
                        if (groupsToUpsert.any { it.currentName == groupNameText }) {
                            XLogE("⚠ 检测到群名重复：$groupNameText，本次已存在，跳过")
                            return@forEachIndexed
                        }

                        XLog("群名称：$groupNameText 群人数：$groupNumber")
                        val groupCategory = getGroupName(groupNameText)
                        val groupInfo = createGroupInfo(currentName = groupNameText, groupName = groupCategory)
                        groupsToUpsert.add(groupInfo)
                    }
                }
            }

            // 滚动加载下一批
            if (!shouldStop) {
                val canScroll = scrollListForward(LIST_FRAMEWORK)
                if (!canScroll) break
            }
            randDelay(2000)

        } while (!shouldStop)

    } catch (e: Exception) {
        XLogE("群群发异常：${e.message}")
    } finally {
        // 不管怎样都执行数据库更新
        if (groupsToUpsert.isNotEmpty()) {
            db.upsertGroups(groupsToUpsert)
            XLog("已更新数据库，共 ${groupsToUpsert.size} 条群信息")
        }
    }
}

