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
import com.ven.assists.AssistsCore.findByTags
import com.ven.assists.AssistsCore.findByText

// 群聊通讯录检测
suspend fun groupAddressBookDetection() {
    val groupsToUpsert = mutableListOf<cn.wi6.x2.utils.GroupInfo>()
    val db = cn.wi6.x2.utils.GroupDatabase(globalContext)
    var shouldStop = false

    try {

        findByText("群聊").firstOrNull()?.randClicks()
        randDelay(2000)

        do {
            val groupNames = findById("com.tencent.mm:id/cg1")

            if (groupNames.isEmpty()) {
                XLog("当前页面未获取到群名称")
                shouldStop = true
            } else {

                groupNames.forEachIndexed { index, groupNameElement ->
                    val groupNameText = groupNameElement.text.toString().trim()

                    // 仅检查本次扫描列表是否重复
                    if (groupsToUpsert.any { it.currentName == groupNameText }) {
                        XLogE("⚠ 检测到群名重复：$groupNameText，本次已存在，跳过")
                        return@forEachIndexed
                    }
                    val groupInfo = createGroupInfo(currentName = groupNameText, saveToContacts = 1)
                    groupsToUpsert.add(groupInfo)

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

