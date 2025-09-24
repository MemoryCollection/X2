package cn.wi6.x2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.wi6.x2.ui.theme.X2Theme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            X2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("文件工具测试")
                }
            }
        }

        // ==== 文件工具测试代码 ====
        val filePath = "${filesDir}${File.separator}test.txt"

        // 1. 添加内容
        FileUtils.appendLine(filePath, "第一行：Hello Android")
        FileUtils.appendLine(filePath, "第二行：Hello Kotlin")
        FileUtils.appendLine(filePath, "第三行：Android File Test")

        // 2. 修改第二行
        FileUtils.updateLine(filePath, 1, "第二行：修改后的 Kotlin")

        // 3. 删除第一行
        FileUtils.deleteLine(filePath, 0)

        // 4. 查找包含 "Android" 的行号
        val lineNumbers = FileUtils.findLineNumbers(filePath, "Android")

        // 5. 读取内容并打印
        val content = FileUtils.readLines(filePath)

        Log.d("FileTest", "文件路径: $filePath")
        Log.d("FileTest", "文件内容:\n${content.joinToString("\n")}")
        Log.d("FileTest", "包含 'Android' 的行号: $lineNumbers")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    X2Theme {
        Greeting("Android")
    }
}
