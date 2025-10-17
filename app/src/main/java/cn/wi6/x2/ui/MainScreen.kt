package cn.wi6.x2.ui

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.wi6.x2.utils.Permission
import cn.wi6.x2.utils.ToastUtil
import cn.wi6.x2.utils.XLog
import com.ven.assists.AssistsCore
import kotlinx.coroutines.*

enum class Screen {
    MAIN, DATABASE // 移除 PERMISSION_REQUEST 屏幕
}

enum class OperationStatus {
    IDLE, LIKING, GROUP_SENDING, WAITING_PERMISSIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(activity: Activity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 初始直接进入主界面
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    var operationStatus by remember { mutableStateOf(OperationStatus.IDLE) }

    val wechatVersion = remember { getWeChatVersion(context) }

    // 权限状态管理
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
        }
    }

    // 权限检测Job：控制高频检测的启动与取消
    var permissionCheckJob by remember { mutableStateOf<Job?>(null) }

    // 权限申请逻辑
    fun handlePermissionRequest() {
        operationStatus = OperationStatus.WAITING_PERMISSIONS
        val needed = Permission.getNeededPermissions(activity)
        val standard = needed.filterNot {
            it in listOf(
                Permission.OVERLAY_PERMISSION,
                Permission.MANAGE_STORAGE_PERMISSION,
                Permission.ACCESSIBILITY_PERMISSION
            )
        }

        // 申请普通权限
        if (standard.isNotEmpty()) permissionLauncher.launch(standard.toTypedArray())
        // 申请特殊权限（跳转系统设置）
        if (!Permission.hasOverlayPermission(activity)) Permission.requestOverlayPermission(activity)
        if (!Permission.hasManageExternalStoragePermission()) Permission.requestManageFilesPermission(activity)
        if (!accessibilityEnabled) Permission.requestAccessibilityPermission()

        // 启动高频检测（1秒/次）
        if (permissionCheckJob?.isActive != true) {
            permissionCheckJob = scope.launch(Dispatchers.Main) {
                while (isActive) {
                    permissionsState = Permission.getPermissionsStatus(activity)
                    accessibilityEnabled = AssistsCore.isAccessibilityServiceEnabled()

                    // 全授权后取消检测
                    if (allPermissionsGranted) {
                        XLog.d("✅ 所有权限已授予，停止高频检测")
                        operationStatus = OperationStatus.IDLE
                        cancel()
                    }

                    delay(1000)
                }
            }
        }
    }

    // 取消所有任务（含权限检测Job）
    fun cancelAllJobs() {
        scope.coroutineContext.job.children.forEach { it.cancel() }
        permissionCheckJob?.cancel()
        operationStatus = OperationStatus.IDLE
    }

    // 页面销毁时取消检测Job，避免内存泄漏
    DisposableEffect(Unit) {
        onDispose {
            permissionCheckJob?.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("微信助手X2", fontWeight = FontWeight.Bold) },
                actions = {
                    if (currentScreen == Screen.MAIN) {
                        TextButton(onClick = { currentScreen = Screen.DATABASE }) {
                            Text("数据库")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.MAIN -> MainContent(
                    wechatVersion = wechatVersion,
                    operationStatusText = when (operationStatus) {
                        OperationStatus.LIKING -> "正在执行运动点赞..."
                        OperationStatus.GROUP_SENDING -> "正在执行群发操作..."
                        OperationStatus.WAITING_PERMISSIONS -> "等待权限授予（1秒/次检测）..."
                        OperationStatus.IDLE -> if (allPermissionsGranted) "就绪" else "需要授予权限"
                    },
                    permissions = permissionsState,
                    allPermissionsGranted = allPermissionsGranted,
                    operationStatus = operationStatus,
                    onStartLiking = {
                        if (operationStatus == OperationStatus.IDLE && allPermissionsGranted) {
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
                        } else if (!allPermissionsGranted) {
                            ToastUtil.showShort("请先点击「申请权限」按钮完成授权")
                        }
                    },
                    onStartGroupSending = {
                        if (operationStatus == OperationStatus.IDLE && allPermissionsGranted) {
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
                        } else if (!allPermissionsGranted) {
                            ToastUtil.showShort("请先点击「申请权限」按钮完成授权")
                        }
                    },
                    onCancelAll = ::cancelAllJobs,
                    onRequestPermissions = ::handlePermissionRequest
                )

                Screen.DATABASE -> DatabaseScreen(
                    context = context,
                    onBack = { currentScreen = Screen.MAIN }
                )
            }
        }
    }
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
        // 版本与状态显示
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("微信版本: $wechatVersion", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("状态: $operationStatusText", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 未全授权时显示：未授权权限列表 + 申请权限按钮
        if (!allPermissionsGranted) {
            PermissionStatusCard(permissions = permissions)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("申请所有必要权限")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 核心功能按钮（关键变更：取消按钮改为红色）
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartLiking,
                enabled = operationStatus == OperationStatus.IDLE && allPermissionsGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("执行运动点赞业务")
            }

            Button(
                onClick = onStartGroupSending,
                enabled = operationStatus == OperationStatus.IDLE && allPermissionsGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("执行群群发操作")
            }

            // 取消所有操作按钮：设置为红色（使用主题错误色，符合Material设计规范）
            Button(
                onClick = onCancelAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error, // 红色系背景
                    contentColor = MaterialTheme.colorScheme.onError  // 白色文字（确保对比度）
                )
            ) {
                Text("暂停微信任务")
            }
        }
    }
}

@Composable
private fun PermissionStatusCard(permissions: Map<String, Boolean>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("未授予的权限：", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            permissions.filter { !it.value }.forEach { (name, _) ->
                Text("• $name", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun getWeChatVersion(context: Context): String {
    return try {
        val pkg = context.packageManager.getPackageInfo("com.tencent.mm", 0)
        pkg.versionName ?: "未知"
    } catch (_: Exception) {
        "未安装"
    }
}