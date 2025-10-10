package cn.wi6.x2.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ven.assists.AssistsCore

object Permission {

    // 基础权限列表
    private val BASE_PERMISSIONS = arrayOf(
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.VIBRATE,
        android.Manifest.permission.WAKE_LOCK,
        android.Manifest.permission.FOREGROUND_SERVICE
    )

    // Android 13+ 需要的新权限
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val TIRAMISU_PERMISSIONS = arrayOf(
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_AUDIO
    )

    // 特殊权限常量
    const val OVERLAY_PERMISSION = "SYSTEM_ALERT_WINDOW"
    const val MANAGE_STORAGE_PERMISSION = "MANAGE_EXTERNAL_STORAGE"
    const val ACCESSIBILITY_PERMISSION = "ACCESSIBILITY_SERVICE"

    // 权限名称映射
    val PERMISSION_NAMES = mapOf(
        android.Manifest.permission.INTERNET to "网络访问",
        android.Manifest.permission.ACCESS_NETWORK_STATE to "网络状态",
        android.Manifest.permission.VIBRATE to "震动",
        android.Manifest.permission.WAKE_LOCK to "唤醒锁",
        android.Manifest.permission.FOREGROUND_SERVICE to "前台服务",
        android.Manifest.permission.POST_NOTIFICATIONS to "通知推送",
        android.Manifest.permission.ACCESS_NOTIFICATION_POLICY to "通知策略",
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
     * 获取当前未被授予的权限列表
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
     * 获取当前需要的所有权限列表（根据API级别动态调整）
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BASE_PERMISSIONS + TIRAMISU_PERMISSIONS
        } else {
            BASE_PERMISSIONS
        }
    }

    /**
     * 请求所有需要的权限
     */
    fun requestAllNeededPermissions(activity: Activity) {
        requestStandardPermissions(activity)
        requestSpecialPermissions(activity)
    }

    /**
     * 请求标准运行时权限
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
     * 检查悬浮窗权限
     */
    fun hasOverlayPermission(activity: Activity): Boolean {
        return Settings.canDrawOverlays(activity)
    }

    /**
     * 请求悬浮窗权限
     */
    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查是否已授予权限
            if (!Settings.canDrawOverlays(activity)) {
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    activity.startActivityForResult(this, 1002) // 添加请求码便于回调处理
                }
            }
        }
    }

    /**
     * 检查管理外部存储权限
     */
    fun hasManageExternalStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                android.os.Environment.isExternalStorageManager()
    }

    /**
     * 请求管理外部存储权限
     */
    fun requestManageFilesPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("$PACKAGE_URI_PREFIX${activity.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(this)
            }
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
}