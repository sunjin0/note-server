# Mood Journal API 接口文档

## 基础信息

- **基础URL**: `https://api.moodjournal.app/v1`
- **协议**: HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8

## 认证方式

所有需要认证的接口需在请求头中包含：

```
Authorization: Bearer {access_token}
```

## 通用响应格式

### 成功响应
```json
{
  "success": true,
  "data": { ... }
}
```

### 错误响应
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述",
    "fieldErrors": {
      "fieldName": "字段错误信息"
    }
  }
}
```

## HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 验证错误 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

## 错误码列表

| 错误码 | 描述 |
|--------|------|
| AUTH_INVALID_CREDENTIALS | 用户名或密码错误 |
| AUTH_USER_EXISTS | 用户邮箱已存在 |
| AUTH_USERNAME_EXISTS | 用户名已存在 |
| AUTH_TOKEN_EXPIRED | 令牌已过期 |
| AUTH_INVALID_TOKEN | 无效的令牌 |
| AUTH_UNAUTHORIZED | 未授权访问 |
| VALIDATION_ERROR | 数据验证失败 |
| RATE_LIMIT_EXCEEDED | 请求频率超限 |
| SERVER_ERROR | 服务器错误 |
| SYNC_CONFLICT | 数据同步冲突 |

---

## 认证接口

### 1. 用户注册

**POST** `/auth/register`

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| username | string | 是 | 用户名，3-20字符 |
| email | string | 是 | 邮箱地址 |
| password | string | 是 | 密码，至少6字符 |
| confirmPassword | string | 是 | 确认密码 |
| nickname | string | 否 | 昵称 |

#### 请求示例
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "nickname": "John"
}
```

#### 成功响应 (201)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123456789",
      "username": "johndoe",
      "email": "john@example.com",
      "nickname": "John",
      "createdAt": "2024-01-15T08:30:00Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "expiresAt": "2024-01-22T08:30:00Z"
  }
}
```

#### 错误响应 (400)
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "请求数据验证失败",
    "fieldErrors": {
      "email": "邮箱格式不正确",
      "password": "密码至少需要6个字符"
    }
  }
}
```

---

### 2. 用户登录

**POST** `/auth/login`

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| identifier | string | 是 | 邮箱或用户名 |
| password | string | 是 | 密码 |
| rememberMe | boolean | 否 | 记住我（延长令牌有效期） |

#### 请求示例
```json
{
  "identifier": "john@example.com",
  "password": "SecurePass123!",
  "rememberMe": true
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123456789",
      "username": "johndoe",
      "email": "john@example.com",
      "nickname": "John",
      "avatar": "https://...",
      "createdAt": "2024-01-15T08:30:00Z",
      "lastLoginAt": "2024-01-15T10:00:00Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
    "expiresAt": "2024-01-22T10:00:00Z"
  }
}
```

#### 错误响应 (401)
```json
{
  "success": false,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "邮箱或密码错误"
  }
}
```

---

### 3. 用户登出

**POST** `/auth/logout`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "message": "登出成功"
  }
}
```

---

### 4. 刷新令牌

**POST** `/auth/refresh`

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| refreshToken | string | 是 | 刷新令牌 |

#### 请求示例
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "bmV3IHJlZnJlc2ggdG9rZW4...",
    "expiresAt": "2024-01-22T12:00:00Z"
  }
}
```

---

### 5. 获取当前用户信息

**GET** `/auth/me`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123456789",
      "username": "johndoe",
      "email": "john@example.com",
      "nickname": "John",
      "avatar": "https://...",
      "createdAt": "2024-01-15T08:30:00Z",
      "lastLoginAt": "2024-01-15T10:00:00Z"
    }
  }
}
```

---

### 6. 修改密码

**POST** `/auth/change-password`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| currentPassword | string | 是 | 当前密码 |
| newPassword | string | 是 | 新密码 |

#### 请求示例
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!"
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "message": "密码修改成功"
  }
}
```

---

## 数据同步接口

### 1. 执行数据同步

**POST** `/sync`

同步客户端数据到服务器并获取服务器更新。

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| deviceId | string | 是 | 设备唯一标识 |
| lastSyncAt | string | 否 | 上次同步时间（ISO 8601） |
| data | object | 是 | 同步数据 |
| data.entries | array | 是 | 日记条目数组 |
| data.settings | object | 是 | 应用设置 |
| data.customFactors | array | 是 | 自定义因素 |
| data.customTemplates | array | 是 | 自定义模板 |
| deletedIds | array | 是 | 已删除条目ID列表 |

#### 请求示例
```json
{
  "deviceId": "device_abc123",
  "lastSyncAt": "2024-01-15T08:00:00Z",
  "data": {
    "entries": [
      {
        "id": "entry_001",
        "date": "2024-01-15",
        "mood": "great",
        "journal": "<p>今天心情很好...</p>",
        "factors": ["work", "exercise"],
        "photos": [],
        "createdAt": "2024-01-15T08:30:00Z",
        "updatedAt": "2024-01-15T08:30:00Z"
      }
    ],
    "settings": {
      "encrypted": false,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    "customFactors": [
      {
        "id": "factor_custom_1",
        "label": "宠物",
        "emoji": "🐱",
        "isCustom": true
      }
    ],
    "customTemplates": []
  },
  "deletedIds": []
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "serverTimestamp": "2024-01-15T10:00:00Z",
    "entries": [
      {
        "id": "entry_002",
        "date": "2024-01-14",
        "mood": "good",
        "journal": "<p>昨天也不错...</p>",
        "factors": ["family"],
        "photos": [],
        "createdAt": "2024-01-14T20:00:00Z",
        "updatedAt": "2024-01-14T20:00:00Z"
      }
    ],
    "settings": {
      "encrypted": false,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    "customFactors": [],
    "customTemplates": [],
    "deletedIds": [],
    "conflicts": [
      {
        "entryId": "entry_001",
        "resolution": "server",
        "mergedEntry": null
      }
    ]
  }
}
```

---

### 2. 获取同步状态

**GET** `/sync/status`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "lastSyncAt": "2024-01-15T08:00:00Z",
    "pendingChanges": 5
  }
}
```

---

### 3. 解决同步冲突

**POST** `/sync/resolve-conflict`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| entryId | string | 是 | 冲突条目ID |
| resolution | string | 是 | 解决方式：server/client/merged |
| mergedEntry | object | 否 | 合并后的条目（resolution为merged时必需） |

#### 请求示例
```json
{
  "entryId": "entry_001",
  "resolution": "merged",
  "mergedEntry": {
    "id": "entry_001",
    "date": "2024-01-15",
    "mood": "good",
    "journal": "<p>合并后的内容...</p>",
    "factors": ["work", "family"],
    "photos": [],
    "createdAt": "2024-01-15T08:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "message": "冲突已解决"
  }
}
```

---

## 用户资料接口

### 1. 获取用户资料

**GET** `/user/profile`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123456789",
      "username": "johndoe",
      "email": "john@example.com",
      "nickname": "John",
      "avatar": "https://...",
      "createdAt": "2024-01-15T08:30:00Z",
      "lastLoginAt": "2024-01-15T10:00:00Z"
    }
  }
}
```

---

### 2. 更新用户资料

**PUT** `/user/profile`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 请求参数

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| avatar | string | 否 | 头像URL |

#### 请求示例
```json
{
  "nickname": "Johnny",
  "avatar": "https://example.com/avatar.jpg"
}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "user_123456789",
      "username": "johndoe",
      "email": "john@example.com",
      "nickname": "Johnny",
      "avatar": "https://example.com/avatar.jpg",
      "createdAt": "2024-01-15T08:30:00Z",
      "lastLoginAt": "2024-01-15T10:00:00Z"
    }
  }
}
```

---

### 3. 删除账户

**DELETE** `/user/account`

#### 请求头
```
Authorization: Bearer {access_token}
```

#### 成功响应 (200)
```json
{
  "success": true,
  "data": {
    "message": "账户已删除"
  }
}
```

---

## 数据模型定义

### User（用户）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | string | 用户唯一标识 |
| username | string | 用户名 |
| email | string | 邮箱地址 |
| nickname | string | 昵称（可选） |
| avatar | string | 头像URL（可选） |
| createdAt | string | 创建时间（ISO 8601） |
| lastLoginAt | string | 最后登录时间（ISO 8601） |

### MoodEntry（日记条目）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | string | 条目唯一标识 |
| date | string | 日期（YYYY-MM-DD） |
| mood | string | 心情类型：great/good/okay/sad/angry |
| journal | string | 日记内容（HTML格式） |
| factors | array | 影响因素ID数组 |
| photos | array | 照片URL数组 |
| createdAt | string | 创建时间（ISO 8601） |
| updatedAt | string | 更新时间（ISO 8601） |
| journalEncrypted | boolean | 是否加密（可选） |

### FactorOption（影响因素）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | string | 因素唯一标识 |
| label | string | 显示名称 |
| emoji | string | Emoji图标 |
| isCustom | boolean | 是否自定义 |

### JournalTemplate（日记模板）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | string | 模板唯一标识 |
| category | string | 分类：work/study/travel/health/life/custom |
| titleKey | string | 标题i18n键 |
| contentKey | string | 内容i18n键 |
| isCustom | boolean | 是否自定义 |
| createdAt | string | 创建时间（ISO 8601） |

---

## 限流规则

| 接口 | 限流 |
|------|------|
| 注册 | 5次/小时/IP |
| 登录 | 10次/分钟/IP |
| 其他接口 | 100次/分钟/用户 |

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2024-01-15 | 初始版本 |
