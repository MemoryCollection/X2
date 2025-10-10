## 数据库
~~~ sql
-- 群表：存储群基本信息
CREATE TABLE IF NOT EXISTS ChatGroup (
  id TEXT PRIMARY KEY,                 -- UUID 字符串
  currentName TEXT NOT NULL,           -- 当前名称
  originalName TEXT,                   -- 原始名称
  groupName TEXT,                      -- 分组名称
  lastSentAt INTEGER,                  -- 上一次发送消息时间（Unix 时间戳，秒）
  saveToContacts INTEGER NOT NULL,     -- 是否保存到通讯录（0 = 否，1 = 是）
  createdAt INTEGER NOT NULL,          -- 创建时间
  updatedAt INTEGER NOT NULL           -- 更新时间
);

-- 分组规则表
CREATE TABLE IF NOT EXISTS GroupRule (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  displayName TEXT NOT NULL,           -- 规则显示名称
  patterns TEXT NOT NULL,              -- 匹配模式，竖线分隔
  priority INTEGER DEFAULT 0,          -- 优先级
  createdAt INTEGER NOT NULL,
  updatedAt INTEGER NOT NULL
);

-- 白名单表
CREATE TABLE IF NOT EXISTS Whitelist (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,           -- 群名称（精确匹配）
  note TEXT,                           -- 备注
  createdAt INTEGER NOT NULL,
  updatedAt INTEGER NOT NULL
);

~~~