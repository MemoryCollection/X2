package cn.wi6.x2.wechat

object WeChatConfig {

    const val PACKAGE_NAME = "com.tencent.mm"

    //------- 微信运动 -------
    // 运动点赞
    const val SPORTS_HEARTS = "com.tencent.mm:id/dj4"
    // 运动点赞名称
    const val SPORT_NAME = "com.tencent.mm:id/djh"
    // 运动点赞数量
    const val SPORT_NUMBER = "com.tencent.mm:id/dic"
    // 运动列表
    const val LIST_OF_SPORTS = "com.tencent.mm:id/dja"
    // 单个关键词过滤
    const val SPORT_FILTER_KEYWORD = "吴东"
    // 最小运动步数阈值
    const val SPORT_MIN_STEPS_THRESHOLD = 1000

    //------- 群群发 -------
    // 首页更多功能
    const val HOME_MORE_FEATURES = "com.tencent.mm:id/jga"
    // 更多群聊 - 选择第一个
    const val START_A_GROUP_CHAT = "发起群聊"
    // 选择一个已有的群
    const val SELECT_AN_EXISTING_GROUP = "选择一个已有的群"
    // 列表 - 群名称
    const val LIST_GROUP_NAME = "com.tencent.mm:id/gtz"
    // 列表 - 群人数
    const val LIST_GROUP_NUMBER = "com.tencent.mm:id/gtx"
    // 列表 - 框架
    const val LIST_FRAMEWORK = "com.tencent.mm:id/gu0"

    const val GROUP_RULES = """
                    医疗|医疗|医疗群|医疗器械群
                    旅游|旅游|旅游群
                    工商|工商|工商群
    """

    const val WHITELIST = "亿企优服|禁广群|WeChatFerry猴|锦州|Assists|比特金矿37群"

    const val LEAST_GROUP_SIZE = 80

}

