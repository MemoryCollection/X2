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

        // 1. 初始化不依赖权限的组件（日志系统）
        XLog.init(globalContext)
//
//        // 2. 检查核心权限，延迟初始化依赖权限的组件
//        checkCriticalPermissions()
    }

//    // 检查核心权限（管理外部存储等）
//    private fun checkCriticalPermissions() {
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(500) // 等待系统初始化
//
//            if (Permission.hasManageExternalStoragePermission()) {
//                // 权限已就绪，初始化数据库
//                isPermissionReady = true
//                GroupDatabase.initDatabase(globalContext)
//            } else {
//                // 权限未就绪，提示用户（在MainActivity中会引导授权）
//                ToastUtil.showLong("请授予文件访问权限以正常使用应用")
//            }
//        }
//    }
//
//    // 权限授予后手动初始化数据库（供MainActivity调用）
//    fun initDatabaseAfterPermission() {
//        if (!isPermissionReady && Permission.hasManageExternalStoragePermission()) {
//            GroupDatabase.initDatabase(globalContext)
//            isPermissionReady = true
//        }
//    }
}