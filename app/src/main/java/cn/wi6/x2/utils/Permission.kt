package cn.wi6.x2.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ven.assists.AssistsCore

// 仅兼容 Android 14 (API 34) 及以上
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object Permission {

    // 基础权限列表（Android 14 及以上支持）
    private val BASE_PERMISSIONS = arrayOf(
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.VIBRATE,
        android.Manifest.permission.WAKE_LOCK,
        android.Manifest.permission.FOREGROUND_SERVICE
    )

    // Android 14 及以上需要的媒体和通知权限（延续 Android 13+ 的细分权限）
    private val ANDROID_14_PERMISSIONS = arrayOf(
        android.Manifest.permission.POST_NOTIFICATIONS, // Android 13+ 新增，14 仍需
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_AUDIO
    )

    // 特殊权限常量（Android 14 支持）
    const val OVERLAY_PERMISSION = "SYSTEM_ALERT_WINDOW"
    const val MANAGE_STORAGE_PERMISSION = "MANAGE_EXTERNAL_STORAGE"
    const val ACCESSIBILITY_PERMISSION = "ACCESSIBILITY_SERVICE"

    // 权限名称映射（仅保留 Android 14 相关权限）
    val PERMISSION_NAMES = mapOf(
        android.Manifest.permission.INTERNET to "网络访问",
        android.Manifest.permission.ACCESS_NETWORK_STATE to "网络状态",
        android.Manifest.permission.VIBRATE to "震动",
        android.Manifest.permission.WAKE_LOCK to "唤醒锁",
        android.Manifest.permission.FOREGROUND_SERVICE to "前台服务",
        android.Manifest.permission.POST_NOTIFICATIONS to "通知推送",
        android.Manifest.permission.READ_MEDIA_IMAGES to "读取图片",
        android.Manifest.permission.READ_MEDIA_VIDEO to "读取视频",
        android.Manifest.permission.READ_MEDIA_AUDIO to "读取音频",
        OVERLAY_PERMISSION to "悬浮窗",
        MANAGE_STORAGE_PERMISSION to "全部文件访问",
        ACCESSIBILITY_PERMISSION to "无障碍服务"
    )

    private const val PERMISSION_REQUEST_CODE = 1001
    private const val PACKAGE_URI_PREFIX = "package:"


    /**
     * 获取当前未被授予的权限列表（仅 Android 14+）
     */
    fun getNeededPermissions(activity: Activity): Array<String> {
        return (getRequiredPermissions().filterNot { isPermissionGranted(activity, it) } +
                getNeededSpecialPermissions(activity)).toTypedArray()
    }

    /**
     * 获取需要但未授予的特殊权限列表
     */
    private fun getNeededSpecialPermissions(activity: Activity): List<String> {
        val neededPermissions = mutableListOf<String>()

        if (!hasOverlayPermission(activity)) {
            neededPermissions.add(OVERLAY_PERMISSION)
        }
        if (!hasManageExternalStoragePermission()) {
            neededPermissions.add(MANAGE_STORAGE_PERMISSION)
        }
        if (!hasAccessibilityPermission()) {
            neededPermissions.add(ACCESSIBILITY_PERMISSION)
        }

        return neededPermissions
    }

    /**
     * 获取当前需要的所有权限列表（仅 Android 14+）
     */
    fun getRequiredPermissions(): Array<String> {
        return BASE_PERMISSIONS + ANDROID_14_PERMISSIONS
    }

    /**
     * 请求所有需要的权限
     */
    fun requestAllNeededPermissions(activity: Activity) {
        requestStandardPermissions(activity)
        requestSpecialPermissions(activity)
    }

    /**
     * 请求标准运行时权限（Android 14+ 运行时权限申请方式不变）
     */
    private fun requestStandardPermissions(activity: Activity) {
        val deniedPermissions = getRequiredPermissions().filterNot {
            isPermissionGranted(activity, it)
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                deniedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * 请求特殊权限（悬浮窗、存储管理、无障碍）
     */
    private fun requestSpecialPermissions(activity: Activity) {
        if (!hasOverlayPermission(activity)) {
            requestOverlayPermission(activity)
        }
        if (!hasManageExternalStoragePermission()) {
            requestManageFilesPermission(activity)
        }
        if (!hasAccessibilityPermission()) {
            requestAccessibilityPermission()
        }
    }

    /**
     * 检查单个权限是否已授予
     */
    fun isPermissionGranted(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }


    /**
     * 请求悬浮窗权限
     */
    fun requestOverlayPermission(activity: Activity) {
        // Android 14 无需低版本判断，直接使用最新API
        if (!Settings.canDrawOverlays(activity)) {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivityForResult(this, 1002)
            }
        }
    }

    /**
     * 检查管理外部存储权限（Android 14 逻辑不变）
     */
    fun hasManageExternalStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    /**
     * 请求管理外部存储权限
     */
    fun requestManageFilesPermission(activity: Activity) {
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("$PACKAGE_URI_PREFIX${activity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(this)
        }
    }

    /**
     * 检查无障碍服务是否启用
     */
    fun hasAccessibilityPermission(): Boolean {
        return AssistsCore.isAccessibilityServiceEnabled()
    }

    /**
     * 请求无障碍服务权限
     */
    fun requestAccessibilityPermission() {
        AssistsCore.openAccessibilitySetting()
    }

    /**
     * 获取所有权限状态（包括标准权限和特殊权限）
     */
    fun getPermissionsStatus(activity: Activity): Map<String, Boolean> {
        return buildMap {
            // 标准权限状态
            getRequiredPermissions().forEach { permission ->
                put(PERMISSION_NAMES[permission] ?: permission,
                    isPermissionGranted(activity, permission))
            }
            // 特殊权限状态
            put(PERMISSION_NAMES[OVERLAY_PERMISSION]!!, hasOverlayPermission(activity))
            put(PERMISSION_NAMES[MANAGE_STORAGE_PERMISSION]!!, hasManageExternalStoragePermission())
            put(PERMISSION_NAMES[ACCESSIBILITY_PERMISSION]!!, hasAccessibilityPermission())
        }
    }

    /**
     * 检查是否所有需要的权限都已授予
     */
    fun hasAllPermissions(activity: Activity): Boolean {
        return getRequiredPermissions().all { isPermissionGranted(activity, it) } &&
                hasOverlayPermission(activity) &&
                hasManageExternalStoragePermission() &&
                hasAccessibilityPermission()
    }

    // 在Permission.kt中补充以下方法
    /**
     * 检查是否有悬浮窗权限
     */
    fun hasOverlayPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else {
            true // 低版本默认认为有权限
        }
    }

    const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
}