package cn.wi6.x2

import android.app.Application
import cn.wi6.x2.utils.XLog
import com.ven.assists.base.BuildConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        XLog.init(this)
    }
}
