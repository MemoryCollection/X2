package cn.wi6.x2

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import cn.wi6.x2.ui.MainScreen
import cn.wi6.x2.ui.PermissionStatusScreen
import cn.wi6.x2.ui.theme.X2Theme
import com.ven.assists.AssistsCore
import android.provider.Settings

class MainActivity : ComponentActivity() {
    private var allPermissionsGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupContent()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun setupContent() {
        setContent {
            X2Theme {
                PermissionScreenContent()
            }
        }
    }

    @Composable
    private fun PermissionScreenContent() {
        if (allPermissionsGranted) {
            MainScreen()
        } else {
            PermissionStatusScreen(
                activity = this,
                onPermissionsUpdated = { checkPermissions() }
            )
        }
    }

    private fun checkPermissions() {
        allPermissionsGranted = checkAllPermissionsGranted()
        // 如果权限状态变化，需要重新设置内容
        if (!isFinishing && !isDestroyed) {
            setupContent()
        }
    }

    private fun checkAllPermissionsGranted(): Boolean {
        return Permission.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        } && Settings.canDrawOverlays(this) &&
                EnvironmentPermissionHelper.hasManageExternalStoragePermission() &&
                AssistsCore.isAccessibilityServiceEnabled()
    }
}