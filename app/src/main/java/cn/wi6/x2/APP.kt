package cn.wi6.x2

import android.app.Application
import android.content.Context
import cn.wi6.x2.utils.GroupDatabase
import cn.wi6.x2.utils.Permission
import cn.wi6.x2.utils.ToastUtil
import cn.wi6.x2.utils.XLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class App : Application() {
    companion object {
        lateinit var globalContext: Context
            private set
        var isPermissionReady = false // 标记权限是否就绪
    }

    override fun onCreate() {
        super.onCreate()
        globalContext = this
        XLog.init(globalContext)
//
    }

}