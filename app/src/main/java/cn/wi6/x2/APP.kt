package cn.wi6.x2

import android.app.Application
import android.content.Context
import cn.wi6.x2.utils.XLog
import com.ven.assists.AssistsCore

class App : Application() {
    companion object {
        lateinit var globalContext: Context
            private set
        var isPermissionReady = false // 标记权限是否就绪
    }

    override fun onCreate() {
        super.onCreate()
        AssistsCore.init(this)
        globalContext = this
        XLog.init(globalContext)

//
    }

}