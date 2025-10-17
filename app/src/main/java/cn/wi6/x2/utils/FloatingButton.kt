package cn.wi6.x2.utils

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object FloatingControllerManager {
    @Volatile
    private var controller: FloatingButtonController? = null

    fun init(activity: Activity, lifecycle: Lifecycle) {
        if (controller == null) {
            synchronized(this) {
                controller = FloatingButtonController(activity, lifecycle)
            }
        }
    }

    fun getController(): FloatingButtonController {
        return controller ?: throw IllegalStateException("请先调用 init() 初始化")
    }
}

class FloatingButtonController(
    private val activity: Activity,
    private val lifecycle: Lifecycle
) : DefaultLifecycleObserver {
    private var floatingButton: TextView? = null
    private var windowManager: WindowManager? = null
    private var isShowing = false
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val appContext: Context get() = activity.applicationContext
    private val operationMutex = Mutex()

    init {
        lifecycle.addObserver(this)
    }

    fun show(): Job {
        return mainScope.launch {
            operationMutex.withLock {
                if (isShowing) {
                    XLog.d("悬浮按钮已显示")
                    return@launch
                }

                try {
                    windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    floatingButton = createFloatingButton()
                    val params = createWindowParams()

                    floatingButton?.let {
                        windowManager?.addView(it, params)
                        isShowing = true
                        XLog.d("悬浮按钮显示成功")
                    }
                } catch (e: Exception) {
                    XLog.e("显示失败：${e.message}", e)
                    resetState()
                }
            }
        }
    }

    fun hide(): Job {
        return mainScope.launch {
            operationMutex.withLock {
                if (!isShowing) {
                    XLog.d("悬浮按钮已隐藏，无需重复操作")
                    return@launch
                }

                try {
                    floatingButton?.let { windowManager?.removeView(it) }
                    XLog.d("悬浮按钮隐藏成功")
                } catch (e: Exception) {
                    XLog.e("隐藏失败：${e.message}", e)
                } finally {
                    resetState()
                }
            }
        }
    }

    /**
     * 核心修改：实现按钮背景全透明，仅保留文字和点击反馈
     */
    private fun createFloatingButton(): TextView {
        return TextView(activity).apply {
            text = "🔴" // 保留红色文字标识
            textSize = 24f
            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())

            // 关键修改1：设置背景全透明（移除之前的红色背景）
            setBackgroundColor(activity.getColor(android.R.color.transparent))

            // 保持点击能力和焦点状态
            isClickable = true
            isFocusable = true

            // 原有点击逻辑不变：隐藏按钮 + 执行停止任务
            setOnClickListener {
                mainScope.launch {
                    stopAllTasks()
                }
            }
        }
    }

    private fun createWindowParams(): WindowManager.LayoutParams {
        val type =
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            x = 5.dpToPx()
            y = 0
            windowAnimations = android.R.style.Animation_Toast
            alpha = 1.0f // 文字完全不透明，背景透明
        }
    }

    /**
     * 停止所有任务
     */
    private suspend fun stopAllTasks() {
        XLog.d("======= 执行停止所有任务 =======")
        XLog.d("提示：此函数已支持调用 suspend 操作，后期可直接编辑逻辑")
    }

    private fun resetState() {
        floatingButton?.setOnClickListener(null)
        floatingButton = null
        isShowing = false
    }

    private fun Int.dpToPx(): Int {
        return try {
            val density = appContext.resources.displayMetrics.density
            (this * density + 0.5f).toInt()
        } catch (e: Exception) {
            0
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mainScope.launch { hide() }
        windowManager = null
        lifecycle.removeObserver(this)
    }
}