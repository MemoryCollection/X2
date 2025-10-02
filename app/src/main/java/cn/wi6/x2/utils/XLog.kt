package cn.wi6.x2.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object XLog {
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
        return File(appContext.filesDir, LOG_FILE_NAME).apply {
            if (!parentFile.exists()) parentFile.mkdirs()
        }
    }

    // 写入日志到文件（追加模式）
    private suspend fun writeLog(level: String, msg: String) = withContext(Dispatchers.IO) {
        try {
            val logFile = getLogFile()
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.write("[${timeFormatter.format(Date())}][$level] $msg\n")
            }
        } catch (e: Exception) {
            Log.e("XLogger", "Write log failed", e)
        }
    }

    /**
     * 删除日志文件
     */
    suspend fun clearLogs(): Boolean = withContext(Dispatchers.IO) {
        try {
            getLogFile().delete()
        } catch (e: Exception) {
            Log.e("XLogger", "Clear logs failed", e)
            false
        }
    }

    /**
     * Debug级别日志
     */
    suspend fun d(msg: String) {
        Log.d("AppLog", msg)
        writeLog("D", msg)
    }

    /**
     * Error级别日志
     */
    suspend fun e(msg: String) {
        Log.e("AppLog", msg)
        writeLog("E", msg)
    }

    /**
     * Error级别日志（带异常）
     */
    suspend fun e(msg: String, e: Throwable) {
        val fullMsg = "$msg\n${Log.getStackTraceString(e)}"
        Log.e("AppLog", fullMsg)
        writeLog("E", fullMsg)
    }
}