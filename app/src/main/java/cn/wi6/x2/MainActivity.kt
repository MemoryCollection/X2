package cn.wi6.x2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.wi6.x2.ui.theme.X2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            X2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionStatusScreen(this)
                }
            }
        }
    }
}

@Composable
fun PermissionStatusScreen(activity: ComponentActivity) {
    var permissions by remember { mutableStateOf(PermissionUtils.getPermissionsStatus(activity)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "当前权限状态",
            style = MaterialTheme.typography.titleLarge
        )

        // 遍历权限状态
        permissions.forEach { (name, granted) ->
            Text(
                text = "$name: ${if (granted) "已开启 ✅" else "未开启 ❌"}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            PermissionUtils.requestAllNeededPermissions(activity)
            permissions = PermissionUtils.getPermissionsStatus(activity)
        }) {
            Text("检查并申请权限")
        }
    }
}


