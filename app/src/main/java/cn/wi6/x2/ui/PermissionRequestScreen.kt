package cn.wi6.x2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 权限请求界面：引导用户授予必要权限
 * @param permissionStatus 权限状态映射（权限名称 -> 是否授予）
 * @param onRequestPermissions 点击请求权限的回调
 * @param onSkip 跳过权限请求的回调（必须提供）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestScreen(
    permissionStatus: Map<String, Boolean>,
    onRequestPermissions: () -> Unit,
    onSkip: () -> Unit // 改为非可选参数
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限说明
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "应用需要以下权限才能正常工作",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )

                // 权限列表
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        permissionStatus.forEach { (name, granted) ->
                            PermissionItem(name = name, granted = granted)
                            if (name != permissionStatus.keys.last()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "未授予的权限可能导致功能异常",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // 操作按钮 - 现在跳过按钮一定会显示
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = permissionStatus.any { !it.value }
                ) {
                    Text("授予权限")
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("跳过（功能可能受限）")
                }
            }
        }
    }
}

/**
 * 单个权限项展示
 */
@Composable
private fun PermissionItem(name: String, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            imageVector = if (granted) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.Error
            },
            contentDescription = if (granted) "已授予" else "未授予",
            tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}