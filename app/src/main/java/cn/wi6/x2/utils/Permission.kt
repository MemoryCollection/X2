package cn.wi6.x2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import com.ven.assists.AssistsCore


object Permission {

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    private const val REQUEST_CODE = 1001

    private val PERMISSION_NAMES = mapOf(
        Manifest.permission.INTERNET to "网络访问",
        Manifest.permission.ACCESS_NETWORK_STATE to "网络状态",
        Manifest.permission.VIBRATE to "震动",
        Manifest.permission.WAKE_LOCK to "唤醒锁",
        Manifest.permission.FOREGROUND_SERVICE to "前台服务",
        Manifest.permission.POST_NOTIFICATIONS to "通知推送",
        Manifest.permission.ACCESS_NOTIFICATION_POLICY to "通知策略",
        Manifest.permission.READ_MEDIA_IMAGES to "读取图片",
        Manifest.permission.READ_MEDIA_VIDEO to "读取视频",
        Manifest.permission.READ_MEDIA_AUDIO to "读取音频",
        "MANAGE_EXTERNAL_STORAGE" to "全部文件访问",
        "SYSTEM_ALERT_WINDOW" to "悬浮窗",
        "ACCESSIBILITY_SERVICE" to "无障碍服务"
    )

    // 普通权限申请
    fun requestAllNeededPermissions(activity: Activity) {
        val deniedPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, deniedPermissions.toTypedArray(), REQUEST_CODE)
        }

        // 其他特殊权限
        requestOverlayPermission(activity)
        requestManageFilesPermission(activity)
        requestAccessibilityPermission(activity)
    }

    // 悬浮窗权限
    private fun requestOverlayPermission(activity: Activity) {
        if (!Settings.canDrawOverlays(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                .apply { data = android.net.Uri.parse("package:${activity.packageName}") }
            activity.startActivity(intent)
        }
    }

    // 全部文件权限
    private fun requestManageFilesPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !EnvironmentPermissionHelper.hasManageExternalStoragePermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                .apply { data = android.net.Uri.parse("package:${activity.packageName}") }
            activity.startActivity(intent)
        }
    }

    // 无障碍权限（Assists 提供的辅助方法）
    private fun requestAccessibilityPermission(activity: Activity) {
        if (!AssistsCore.isAccessibilityServiceEnabled()) {
            AssistsCore.openAccessibilitySetting()
        }
    }

    // 获取所有权限状态
    fun getPermissionsStatus(activity: Activity): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()

        // 普通权限
        REQUIRED_PERMISSIONS.forEach {
            map[PERMISSION_NAMES[it] ?: it] =
                ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }

        // 特殊权限
        map[PERMISSION_NAMES["SYSTEM_ALERT_WINDOW"]!!] = Settings.canDrawOverlays(activity)
        map[PERMISSION_NAMES["MANAGE_EXTERNAL_STORAGE"]!!] = EnvironmentPermissionHelper.hasManageExternalStoragePermission()
        map[PERMISSION_NAMES["ACCESSIBILITY_SERVICE"]!!] = AssistsCore.isAccessibilityServiceEnabled()

        return map
    }
}

object EnvironmentPermissionHelper {
    fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true
        }
    }
}
