package cn.wi6.x2.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.wi6.x2.Permission
import com.ven.assists.AssistsCore

@Composable
fun PermissionStatusScreen(activity: Activity, onPermissionsUpdated: () -> Unit) {
    var permissions by remember { mutableStateOf(Permission.getPermissionsStatus(activity)) }
    var accessibilityEnabled by remember { mutableStateOf(AssistsCore.isAccessibilityServiceEnabled()) }

    LaunchedEffect(Unit) {
        permissions = Permission.getPermissionsStatus(activity)
        accessibilityEnabled = AssistsCore.isAccessibilityServiceEnabled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "应用权限状态",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            permissions.forEach { (name, granted) ->
                PermissionStatusRow(name = name, granted = granted)
            }
            PermissionStatusRow(name = "无障碍服务", granted = accessibilityEnabled)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    Permission.requestAllNeededPermissions(activity)
                    permissions = Permission.getPermissionsStatus(activity)
                    onPermissionsUpdated()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("申请所有权限")
            }

            Button(
                onClick = {
                    AssistsCore.openAccessibilitySetting()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("设置无障碍服务")
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(name: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name)
        Icon(
            imageVector = if (granted) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (granted) "已授权" else "未授权",
            tint = if (granted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )
    }
}