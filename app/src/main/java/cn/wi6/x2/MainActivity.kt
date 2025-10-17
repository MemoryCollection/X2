package cn.wi6.x2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import cn.wi6.x2.ui.MainScreen
import cn.wi6.x2.ui.theme.X2Theme
import cn.wi6.x2.utils.FloatingControllerManager
import cn.wi6.x2.utils.Permission

class MainActivity : ComponentActivity() {
    // 悬浮窗权限请求回调（配合你的 Permission 类）
    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // 权限请求返回后，检查是否已授予
            if (Permission.hasOverlayPermission(this)) {
                // 权限授予后延迟显示悬浮窗
                window.decorView.postDelayed({
                    FloatingControllerManager.getController().show()
                }, 300)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FloatingControllerManager.init(this, lifecycle)
        //FloatingControllerManager.getController().show()
        setContent {
            X2Theme {
                MainScreen(activity = this)
            }
        }
    }


}