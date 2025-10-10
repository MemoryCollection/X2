package cn.wi6.x2.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import cn.wi6.x2.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object XLog {
    var TAG = "X2"
    // 固定日志文件名
    private const val LOG_FILE_NAME = "app_log.txt"

    // 保存Application Context
    private lateinit var appContext: Context

    // 时间格式化器
    private val timeFormatter = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 初始化（在Application中调用）
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // 获取日志文件
    private fun getLogFile(): File {
        // 从公共目录获取日志文件
        return File(FileUtils.getPublicAppDir(), LOG_FILE_NAME).apply {
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
        }
    }


    private suspend fun writeLog(level: String, msg: String) = withContext(Dispatchers.IO) {
        try {
            if (!FileUtils.isExternalStorageWritable()) {
                Log.e(TAG, "外部存储不可写，无法保存日志")
                return@withContext
            }
            val logFile = getLogFile()
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.write("[${timeFormatter.format(Date())}][$level] $msg\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "写入日志失败", e)
        }
    }

    /**
     * 删除日志文件
     */
    suspend fun clearLogs(): Boolean = withContext(Dispatchers.IO) {
        try {
            getLogFile().delete()
        } catch (e: Exception) {
            Log.e(TAG, "Clear logs failed", e)
            false
        }
    }

    /**
     * Debug级别日志
     */
    suspend fun d(msg: String) {
        Log.d(TAG, msg)
        writeLog("D", msg)
    }

    /**
     * Error级别日志
     */
    suspend fun e(msg: String) {
        Log.e(TAG, msg)
        writeLog("E", msg)
    }

    /**
     * Error级别日志（带异常）
     */
    suspend fun e(msg: String, e: Throwable) {
        val fullMsg = "$msg\n${Log.getStackTraceString(e)}"
        Log.e(TAG, fullMsg)
        writeLog("E", fullMsg)
    }
}


object ToastUtil {
    fun showShort(message: String) {
        showToast(message, Toast.LENGTH_SHORT)
    }

    fun showLong(message: String) {
        showToast(message, Toast.LENGTH_LONG)
    }

    private fun showToast(message: String, duration: Int) {
        if (message.isEmpty()) return

        // 确保在主线程显示
        Handler(Looper.getMainLooper()).post {
            // 直接使用 App.globalContext（属性访问，而非后备字段）
            Toast.makeText(App.globalContext, message, duration).show()
        }
    }
}