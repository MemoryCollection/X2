package cn.wi6.x2.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.util.*

/**
 * 数据类：群信息（含群人数字段）
 */
data class GroupInfo(
    val id: String,               // UUID 主键（固定按 currentName 生成）
    val currentName: String,      // 当前名称（非空）
    val originalName: String? = null,  // 原始名称
    val groupName: String? = null,     // 分组名称
    val lastSentAt: Long? = null,      // 上一次发送消息时间（Unix 时间戳）
    val saveToContacts: Int? = null,   // 是否保存到通讯录（0 = 否, 1 = 是）
    val createdAt: Long? = null,       // 创建时间
    val updatedAt: Long? = null,       // 更新时间
    val memberCount: Int? = null       // 新增：群人数（null 表示未统计）
)

/**
 * SQLite 数据库辅助类
 * 数据库存放在公共存储目录 /storage/emulated/0/X2/
 */
class GroupDatabase(context: Context) :
    SQLiteOpenHelper(
        context,
        getDatabaseFilePath()?.absolutePath, // 权限不足时为null，避免崩溃
        null,
        DATABASE_VERSION
    )  {

    companion object {
        private const val DATABASE_NAME = "groups.db"
        private const val DATABASE_VERSION = 2 // 版本号升级为2（新增群人数列）
        private const val TABLE_NAME = "group_info"

        const val COLUMN_ID = "id"
        const val COLUMN_CURRENT_NAME = "currentName"
        const val COLUMN_ORIGINAL_NAME = "originalName"
        const val COLUMN_GROUP_NAME = "groupName"
        const val COLUMN_LAST_SENT_AT = "lastSentAt"
        const val COLUMN_SAVE_TO_CONTACTS = "saveToContacts"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_UPDATED_AT = "updatedAt"
        const val COLUMN_MEMBER_COUNT = "memberCount" // 新增：群人数列名

        /**
         * 获取数据库文件路径（公共存储目录）
         */
        private fun getDatabaseFilePath(): File? {
            val dir = FileUtils.getPublicAppDir() ?: return null
            return File(dir, DATABASE_NAME).apply {
                if (!exists()) createNewFile()
            }
        }

        /**
         * 初始化数据库，可在 Application 或启动 Activity 中调用
         */
        fun initDatabase(context: Context) {
            GroupDatabase(context).writableDatabase
        }

        /**
         * 根据 currentName 生成固定 UUID，保证唯一更新
         */
        fun generateIdByName(name: String): String {
            return UUID.nameUUIDFromBytes(name.toByteArray()).toString()
        }
    }

    /**
     * 创建表（版本2：含群人数列）
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_CURRENT_NAME TEXT NOT NULL,
                $COLUMN_ORIGINAL_NAME TEXT,
                $COLUMN_GROUP_NAME TEXT,
                $COLUMN_LAST_SENT_AT INTEGER,
                $COLUMN_SAVE_TO_CONTACTS INTEGER,
                $COLUMN_CREATED_AT INTEGER,
                $COLUMN_UPDATED_AT INTEGER,
                $COLUMN_MEMBER_COUNT INTEGER  -- 新增：群人数列（支持空值）
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    /**
     * 数据库升级（版本1 -> 2：新增群人数列，保留原有数据）
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 按版本号增量升级：仅当从版本1升级时执行新增列操作
        if (oldVersion < 2) {
            // 注意：SQLite ALTER TABLE ADD COLUMN 不支持 IF NOT EXISTS，
            // 但通过版本判断（oldVersion < 2）可确保仅执行一次，避免重复添加报错
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_MEMBER_COUNT INTEGER")
        }
        // 后续版本升级可扩展：
        // if (oldVersion < 3) {
        //     版本2 -> 3 的修改逻辑
        // }
    }

    /**
     * 插入群信息（含群人数），失败返回 -1
     */
    fun insertGroup(group: GroupInfo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, group.id)
            put(COLUMN_CURRENT_NAME, group.currentName)
            put(COLUMN_ORIGINAL_NAME, group.originalName)
            put(COLUMN_GROUP_NAME, group.groupName)
            put(COLUMN_LAST_SENT_AT, group.lastSentAt)
            put(COLUMN_SAVE_TO_CONTACTS, group.saveToContacts)
            put(COLUMN_CREATED_AT, group.createdAt)
            put(COLUMN_UPDATED_AT, group.updatedAt)
            put(COLUMN_MEMBER_COUNT, group.memberCount) // 新增：插入群人数
        }
        return db.insert(TABLE_NAME, null, values)
    }

    /**
     * 更新群信息（含群人数），返回更新行数
     */
    fun updateGroup(group: GroupInfo): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CURRENT_NAME, group.currentName)
            put(COLUMN_ORIGINAL_NAME, group.originalName)
            put(COLUMN_GROUP_NAME, group.groupName)
            put(COLUMN_LAST_SENT_AT, group.lastSentAt)
            put(COLUMN_SAVE_TO_CONTACTS, group.saveToContacts)
            put(COLUMN_UPDATED_AT, group.updatedAt)
            put(COLUMN_MEMBER_COUNT, group.memberCount) // 新增：更新群人数
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(group.id))
    }

    /**
     * 删除群信息，返回删除行数
     */
    fun deleteGroup(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id))
    }

    /**
     * 查询单个群信息（含群人数）
     */
    fun getGroup(id: String): GroupInfo? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return cursor.use { if (it.moveToFirst()) cursorToGroup(it) else null }
    }

    /**
     * 查询所有群信息（含群人数），按创建时间倒序
     */
    fun getAllGroups(): List<GroupInfo> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )
        val list = mutableListOf<GroupInfo>()
        cursor.use {
            while (it.moveToNext()) list.add(cursorToGroup(it))
        }
        return list
    }

    /**
     * 批量插入或更新（仅更新非空字段），协程安全
     */
    fun upsertGroups(groups: List<GroupInfo>) {
        writableDatabase.use { db ->
            db.beginTransaction()
            try {
                val currentTime = System.currentTimeMillis()
                for (group in groups) {
                    val fixedId = generateIdByName(group.currentName)
                    // 1. 先查询当前记录是否存在
                    val existingGroup = getGroupFromDb(db, fixedId)

                    // 2. 动态构建更新字段（仅包含非空值）
                    val contentValues = ContentValues().apply {
                        // currentName 是非空字段，必须更新
                        put(COLUMN_CURRENT_NAME, group.currentName)

                        // 其他字段仅在非空时更新
                        group.originalName?.let { put(COLUMN_ORIGINAL_NAME, it) }
                        group.groupName?.let { put(COLUMN_GROUP_NAME, it) }
                        group.lastSentAt?.let { put(COLUMN_LAST_SENT_AT, it) }
                        group.saveToContacts?.let { put(COLUMN_SAVE_TO_CONTACTS, it) }
                        group.memberCount?.let { put(COLUMN_MEMBER_COUNT, it) }

                        // 更新时间始终刷新
                        put(COLUMN_UPDATED_AT, currentTime)

                        // 创建时间：新增时设置，更新时保留原有
                        if (existingGroup == null) {
                            put(COLUMN_CREATED_AT, group.createdAt ?: currentTime)
                        }
                    }

                    // 3. 执行插入或更新
                    if (existingGroup == null) {
                        // 新增：必须包含主键
                        contentValues.put(COLUMN_ID, fixedId)
                        db.insert(TABLE_NAME, null, contentValues)
                    } else {
                        // 更新：仅更新非空字段
                        db.update(
                            TABLE_NAME,
                            contentValues,
                            "$COLUMN_ID = ?",
                            arrayOf(fixedId)
                        )
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    /**
     * 从数据库查询指定ID的群信息（用于内部判断记录是否存在）
     */
    private fun getGroupFromDb(db: SQLiteDatabase, id: String): GroupInfo? {
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return cursor.use { if (it.moveToFirst()) cursorToGroup(it) else null }
    }

    /**
     * Cursor 转 GroupInfo（含群人数字段）
     */
    private fun cursorToGroup(cursor: Cursor): GroupInfo {
        return GroupInfo(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            currentName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_NAME)),
            originalName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGINAL_NAME)),
            groupName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)),
            lastSentAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(COLUMN_LAST_SENT_AT)),
            saveToContacts = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(COLUMN_SAVE_TO_CONTACTS)),
            createdAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
            memberCount = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(COLUMN_MEMBER_COUNT)) // 新增：读取群人数
        )
    }

    /**
     * 扩展函数：Cursor 获取 Long 可空值
     */
    private fun Cursor.getLongOrNull(columnIndex: Int): Long? =
        if (isNull(columnIndex)) null else getLong(columnIndex)

    /**
     * 扩展函数：Cursor 获取 Int 可空值
     */
    private fun Cursor.getIntOrNull(columnIndex: Int): Int? =
        if (isNull(columnIndex)) null else getInt(columnIndex)
}

/**
 * 根据群名和分组生成 GroupInfo 对象（含群人数参数）
 * @param currentName 当前群名
 * @param originalName 原始群名
 * @param groupName 分组
 * @param lastSentAt 上次发送时间
 * @param saveToContacts 是否保存到通讯录 0-否 1-是
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @param memberCount 群人数（null 表示未统计）
 */
fun createGroupInfo(
    currentName: String,
    originalName: String? = null,
    groupName: String? = null,
    lastSentAt: Long? = null,
    saveToContacts: Int = 0,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long? = null,
    memberCount: Int? = null // 新增：群人数参数
): GroupInfo {
    val currentTime = System.currentTimeMillis()
    return GroupInfo(
        id = GroupDatabase.generateIdByName(currentName), // 按群名唯一
        currentName = currentName,
        originalName = originalName,
        groupName = groupName,
        lastSentAt = lastSentAt,
        saveToContacts = saveToContacts,
        createdAt = createdAt,
        updatedAt = updatedAt ?: currentTime,
        memberCount = memberCount
    )
}