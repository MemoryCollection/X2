package cn.wi6.x2.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.wi6.x2.wechat.wechatExerciseSteps
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wechatVersion by remember { mutableStateOf(getWeChatVersion(context)) }
    var isLiking by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "运动点赞助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 微信版本显示区域（始终显示）
        Text(
            text = "微信版本: $wechatVersion",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 执行运动点赞业务按钮
        Button(
            onClick = {
                if (!isLiking) {
                    isLiking = true
                    scope.launch {
                        wechatExerciseSteps()
                        isLiking = false
                    }
                }
            },
            enabled = !isLiking,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (isLiking) "正在执行..." else "执行运动点赞业务")
        }
    }
}

// 获取微信版本信息
fun getWeChatVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo("com.tencent.mm", 0)
        packageInfo.versionName ?: "未知版本"
    } catch (e: PackageManager.NameNotFoundException) {
        "微信未安装"
    }
}