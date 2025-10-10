package cn.wi6.x2.ui

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.wi6.x2.App
import cn.wi6.x2.utils.ToastUtil
import com.ven.assists.AssistsCore
import kotlinx.coroutines.*
import androidx.core.content.ContextCompat
import cn.wi6.x2.utils.Permission
import cn.wi6.x2.utils.XLog

enum class Screen {
    MAIN, DATABASE, PERMISSION_REQUEST
}

enum class OperationStatus {
    IDLE, LIKING, GROUP_SENDING, WAITING_PERMISSIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    activity: Activity
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态管理
    var currentScreen by remember { mutableStateOf(Screen.PERMISSION_REQUEST) }
    var operationStatus by remember { mutableStateOf(OperationStatus.IDLE) }
    val wechatVersion by remember { mutableStateOf(getWeChatVersion(context)) }

    // 权限管理
    var permissionsState by remember { mutableStateOf(Permission.getPermissionsStatus(activity)) }
    var accessibilityEnabled by remember { mutableStateOf(AssistsCore.isAccessibilityServiceEnabled()) }
    val allPermissionsGranted by derivedStateOf {
        permissionsState.all { it.value } && accessibilityEnabled
    }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsState = Permission.getPermissionsStatus(activity)
        if (results.any { !it.value }) {
            ToastUtil.showShort("部分权限未授予，功能可能受限")
        } else {
            if (allPermissionsGranted) {
                currentScreen = Screen.MAIN
            }
        }
    }

    // 定时检查权限和无障碍服务状态
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.PERMISSION_REQUEST) {
            while (true) {
                delay(2000)
                permissionsState = Permission.getPermissionsStatus(activity)
                accessibilityEnabled = AssistsCore.isAccessibilityServiceEnabled()

                if (allPermissionsGranted) {
                    currentScreen = Screen.MAIN
                    break
                }
            }
        }
    }

    BackHandler(currentScreen == Screen.DATABASE) {
        currentScreen = Screen.MAIN
    }

    val operationStatusText by derivedStateOf {
        when (operationStatus) {
            OperationStatus.LIKING -> "正在执行运动点赞..."
            OperationStatus.GROUP_SENDING -> "正在执行群发操作..."
            OperationStatus.WAITING_PERMISSIONS -> "等待权限授予..."
            OperationStatus.IDLE -> if (allPermissionsGranted) "就绪" else "需要授予权限"
        }
    }

    fun handlePermissionRequest() {
        operationStatus = OperationStatus.WAITING_PERMISSIONS
        val neededPermissions = Permission.getNeededPermissions(activity)

        if (neededPermissions.isNotEmpty()) {
            val standardPermissions = neededPermissions.filter {
                it != Permission.OVERLAY_PERMISSION &&
                        it != Permission.MANAGE_STORAGE_PERMISSION &&
                        it != Permission.ACCESSIBILITY_PERMISSION
            }.toTypedArray()

            if (standardPermissions.isNotEmpty()) {
                permissionLauncher.launch(standardPermissions)
            }
        }

        if (!Permission.hasOverlayPermission(activity)) {
            Permission.requestOverlayPermission(activity)
        }

        if (!Permission.hasManageExternalStoragePermission()) {
            Permission.requestManageFilesPermission(activity)
        }

        if (!accessibilityEnabled) {
            Permission.requestAccessibilityPermission()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "微信助手X2",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (currentScreen == Screen.MAIN) {
                        TextButton(onClick = { currentScreen = Screen.DATABASE }) {
                            Text("数据库")
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentScreen) {
                    Screen.PERMISSION_REQUEST -> PermissionRequestScreen(
                        permissionStatus = permissionsState,
                        onRequestPermissions = ::handlePermissionRequest,
                        onSkip = { currentScreen = Screen.MAIN }
                    )

                    Screen.MAIN -> MainContent(
                        wechatVersion = wechatVersion,
                        operationStatusText = operationStatusText,
                        permissions = permissionsState,
                        allPermissionsGranted = allPermissionsGranted,
                        operationStatus = operationStatus,
                        onStartLiking = {
                            if (operationStatus == OperationStatus.IDLE) {
                                if (allPermissionsGranted) {
                                    operationStatus = OperationStatus.LIKING
                                    scope.launch {
                                        try {
                                            cn.wi6.x2.wechat.wechatExerciseSteps()
                                        } catch (e: Exception) {
                                            ToastUtil.showLong("运动点赞失败: ${e.message}")
                                            XLog.e("运动点赞异常: ${e.message}")
                                        } finally {
                                            operationStatus = OperationStatus.IDLE
                                        }
                                    }
                                } else {
                                    ToastUtil.showShort("请先授予所有必要权限")
                                    currentScreen = Screen.PERMISSION_REQUEST
                                }
                            }
                        },
                        onStartGroupSending = {
                            if (operationStatus == OperationStatus.IDLE) {
                                if (allPermissionsGranted) {
                                    operationStatus = OperationStatus.GROUP_SENDING
                                    scope.launch {
                                        try {
                                            cn.wi6.x2.wechat.wechatGroupSend()
                                        } catch (e: Exception) {
                                            ToastUtil.showLong("群发操作失败: ${e.message}")
                                            XLog.e("群发操作异常: ${e.message}")
                                        } finally {
                                            operationStatus = OperationStatus.IDLE
                                        }
                                    }
                                } else {
                                    ToastUtil.showShort("请先授予所有必要权限")
                                    currentScreen = Screen.PERMISSION_REQUEST
                                }
                            }
                        },
                        onCancelAll = {
                            scope.coroutineContext.cancelChildren()
                            operationStatus = OperationStatus.IDLE
                        },
                        onRequestPermissions = {
                            currentScreen = Screen.PERMISSION_REQUEST
                        }
                    )

                    Screen.DATABASE -> DatabaseScreen(
                        context = context,
                        onBack = { currentScreen = Screen.MAIN }
                    )
                }
            }
        }
    )
}

@Composable
private fun MainContent(
    wechatVersion: String,
    operationStatusText: String,
    permissions: Map<String, Boolean>,
    allPermissionsGranted: Boolean,
    operationStatus: OperationStatus,
    onStartLiking: () -> Unit,
    onStartGroupSending: () -> Unit,
    onCancelAll: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 状态信息
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("微信版本: $wechatVersion", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("状态: $operationStatusText", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 权限状态提示
        if (!allPermissionsGranted) {
            PermissionStatusCard(permissions)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("补全权限")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 功能按钮区域
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartLiking,
                enabled = operationStatus == OperationStatus.IDLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("执行运动点赞业务")
            }

            Button(
                onClick = onStartGroupSending,
                enabled = operationStatus == OperationStatus.IDLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("执行群群发操作")
            }

            Button(
                onClick = onCancelAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消所有操作")
            }
        }
    }
}

@Composable
private fun PermissionStatusCard(permissions: Map<String, Boolean>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "未授予的权限：",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            permissions.filter { !it.value }.forEach { (name, _) ->
                Text(
                    "• $name",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun getWeChatVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo("com.tencent.mm", 0)
        packageInfo.versionName ?: "未知"
    } catch (e: Exception) {
        "未安装"
    }
}