package cn.wi6.x2.utils

import android.os.Environment
import java.io.File

object FileUtils {
    // 公共存储目录：/storage/emulated/0/X2
    private const val APP_PUBLIC_DIR = "X2"

    /**
     * 获取应用公共存储目录
     * 路径：/storage/emulated/0/X2
     */
    fun getPublicAppDir(): File? {
        // 先检查存储权限和可写性
        if (!isExternalStorageWritable() || !Permission.hasManageExternalStoragePermission()) {
            return null
        }
        val externalDir = Environment.getExternalStorageDirectory()
        return File(externalDir, APP_PUBLIC_DIR).apply {
            if (!exists()) {
                mkdirs() // 递归创建目录
            }
        }
    }

    /**
     * 检查外部存储是否可写
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}