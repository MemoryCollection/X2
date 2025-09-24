import java.io.File

object FileUtils {

    /**
     * 确保文件存在（没有就自动创建，包括目录）
     */
    fun ensureFile(filePath: String): File {
        val file = File(filePath)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs() // 创建目录
        }
        if (!file.exists()) {
            file.createNewFile() // 创建文件
        }
        return file
    }

    /**
     * 读取文件内容（按行）
     */
    fun readLines(filePath: String): MutableList<String> {
        val file = ensureFile(filePath)
        return file.readLines().toMutableList()
    }

    /**
     * 添加一行内容
     */
    fun appendLine(filePath: String, line: String) {
        val file = ensureFile(filePath)
        file.appendText(line + System.lineSeparator())
    }

    /**
     * 修改指定行（下标从 0 开始）
     */
    fun updateLine(filePath: String, index: Int, newLine: String) {
        val lines = readLines(filePath)
        if (index in lines.indices) {
            lines[index] = newLine
            ensureFile(filePath).writeText(lines.joinToString(System.lineSeparator()))
        }
    }

    /**
     * 删除指定行
     */
    fun deleteLine(filePath: String, index: Int) {
        val lines = readLines(filePath)
        if (index in lines.indices) {
            lines.removeAt(index)
            ensureFile(filePath).writeText(lines.joinToString(System.lineSeparator()))
        }
    }

    /**
     * 查找包含指定文本的行号（从0开始），支持返回多个
     */
    fun findLineNumbers(filePath: String, keyword: String): List<Int> {
        val lines = readLines(filePath)
        return lines.mapIndexedNotNull { index, line ->
            if (line.contains(keyword)) index else null
        }
    }
}
