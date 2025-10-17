package cn.wi6.x2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.wi6.x2.utils.GroupDatabase
import cn.wi6.x2.utils.GroupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


/**
 * 数据库页面：表格显示所有群信息
 */
@Composable
fun DatabaseScreen(context: Context, onBack: () -> Unit) { // 接收返回回调
    val db = remember { cn.wi6.x2.utils.GroupDatabase(context) }
    var groups by remember { mutableStateOf(listOf<cn.wi6.x2.utils.GroupInfo>()) }

    // 异步加载数据库数据
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val all = db.getAllGroups()
            withContext(Dispatchers.Main) { groups = all }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

        Row(modifier = Modifier.padding(8.dp)) {
            Button(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("返回主页面")
            }
        }

        Text("数据库群信息", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
        Divider()
        // 表头
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            listOf("当前群名", "分组", "人数", "保存通讯录").forEach { col ->
                Text(col, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
        }
        Divider()
        // 数据行
        LazyColumn {
            items(groups) { group ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(group.currentName, modifier = Modifier.weight(1f))
                    Text(group.groupName ?: "未分", modifier = Modifier.weight(1f))
                    Text("-", modifier = Modifier.weight(1f)) // 人数字段可以后续扩展
                    Text(if (group.saveToContacts == 1) "是" else "否", modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
    }
}