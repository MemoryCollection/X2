package cn.wi6.x2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cn.wi6.x2.ui.MainScreen
import cn.wi6.x2.ui.PermissionRequestScreen // 保持原函数名
import cn.wi6.x2.ui.theme.X2Theme
import cn.wi6.x2.utils.Permission
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private var permissionCheckJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runOnUiThread { setupMainContent() }
    }

    // 加载主界面
    private fun setupMainContent() {
        setContent {
            X2Theme {
                MainScreen(activity = this)
            }
        }
    }

    override fun onDestroy() {
        permissionCheckJob?.cancel()
        super.onDestroy()
    }
}