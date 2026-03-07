# MinIO 存储约束规范

## 1. 概述

本文档定义了 MineGuard 项目中 MinIO 对象存储的桶和目录结构约束，确保文件存储的规范性、可管理性和可追溯性。

## 2. 桶管理规范

### 2.1 桶命名规则

| 配置项 | 说明 | 示例 |
| ------ | ---- | ---- |
| `defaultBucket` | 默认桶名称 | `mineguard` |
| `user` | 用户相关文件桶 | `mine-user` |
| `message` | 消息相关文件桶 | `mine-message` |
| `ai` | AI 相关文件桶 | `mine-ai` |
| `vehicle` | 车辆相关文件桶 | `mine-vehicle` |

### 2.2 功能桶说明

| 桶名称 | 用途 | 典型文件 |
| ------ | ---- | -------- |
| `mine-user` | 用户相关文件 | 用户头像、个人资料图片等 |
| `mine-message` | 消息相关文件 | 聊天图像、申诉证据等 |
| `mine-ai` | AI 相关文件 | AI 分析报告、AI 生成内容等 |
| `mine-vehicle` | 车辆相关文件 | 车辆照片、行驶证、驾驶证等 |
| `mineguard` | 默认桶 | 其他未分类文件 |

## 3. 目录结构规范

### 3.1 目录层级定义

```tree
桶/
└── 业务类型/
    └── 用户ID/
        └── 文件名
```

**路径格式：**

```txt
{业务类型}/{用户ID}/{UUID}.{扩展名}
```

**示例：**

```txt
avatar/user-123/550e8400-e29b-41d4-a716-446655440000.jpg
```

### 3.2 业务类型目录映射

| 业务类型目录 | 说明 | 典型文件 |
| ------------ | ---- | -------- |
| `avatar` | 用户头像 | avatar.jpg |
| `id-card` | 身份证 | id_card_front.jpg, id_card_back.jpg |
| `driving-license` | 驾驶证 | driving_license.jpg |
| `vehicle-license` | 行驶证 | vehicle_license.jpg |
| `vehicle-photo` | 车辆照片 | vehicle_photo_*.jpg |
| `chat-image` | 沟通图片 | chat_image_*.jpg |
| `other` | 其他 | other/* |

### 3.3 文件名规范

- **生成方式**：UUID + 原始文件扩展名
- **格式**：`{UUID}.{扩展名}`
- **示例**：`550e8400-e29b-41d4-a716-446655440000.jpg`

## 4. 路径验证规则

### 4.1 验证约束

| 验证项 | 规则 | 错误码 |
| ------ | ---- | ------ |
| 路径层级 | 必须3级（业务类型/用户ID/文件名） | FILE_PATH_INVALID (4019) |
| 业务类型 | 必须为有效目录名称 | FILE_PATH_INVALID (4019) |
| 用户ID | 不能为空 | FILE_PATH_INVALID (4019) |

### 4.2 验证示例

**有效路径：**

```txt
id-card/user-550e8400/550e8400-e29b-41d4-a716-446655440000.jpg
```

- 业务类型：id-card（身份证）✅
- 用户ID：user-550e8400 ✅
- 文件名：UUID格式 ✅

**无效路径：**

```txt
avatar.jpg
```

- 层级不足 ❌

```txt
invalid-type/user-123/550e8400-e29b-41d4-a716-446655440000.jpg
```

- 业务类型无效 ❌

## 5. API 使用规范

### 5.1 文件上传接口

```java
// 带用户ID
String uploadFile(String module, String folder, MultipartFile file)

// 从URL上传文件
String uploadFileFromUrl(String module, String url, String folder)
```

### 5.2 使用示例

```java
// 上传用户头像（指定模块和文件夹）
storageService.uploadFile("user", "avatar", file);
// 结果路径：avatar/{UUID}.jpg 存储在 mine-user 桶

// 上传聊天图片
storageService.uploadFile("message", "chat-image", file);
// 结果路径：chat-image/{UUID}.jpg 存储在 mine-message 桶

// 上传AI分析报告
storageService.uploadFile("ai", "report", file);
// 结果路径：report/{UUID}.pdf 存储在 mine-ai 桶
```

### 5.3 文件路径生成

```java
// 上传文件并获取存储路径
String objectName = storageService.uploadFile("user", "avatar", file);
// 结果：avatar/550e8400-e29b-41d4-a716-446655440000.jpg

// 生成文件URL
String url = storageService.getFileUrl("mine-user", objectName, true, 3600);
// 结果：http://minio-server/mine-user/avatar/550e8400-e29b-41d4-a716-446655440000.jpg
```

## 6. 配置示例

### 6.1 开发环境

```yaml
# MinIO配置
minio:
  url: http://192.168.110.128:9000
  access-key: admin
  secret-key: 1.qwklz5
  secure: false
  default-bucket: mineguard
  buckets:
    user: mine-user
    message: mine-message
    ai: mine-ai
    vehicle: mine-vehicle
  enabled: true
```

### 6.2 生产环境

```yaml
# MinIO配置
minio:
  url: https://minio.example.com
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  secure: true
  default-bucket: mineguard
  buckets:
    user: mine-user
    message: mine-message
    ai: mine-ai
  enabled: true
```

## 7. 错误码

| 错误码 | 说明 |
| ------ | ---- |
| 4001 | 存储连接错误 |
| 4004 | 存储桶不存在 |
| 4007 | 存储桶初始化失败 |
| 4008 | 文件不存在 |
| 4009 | 文件上传错误 |
| 4010 | 文件下载错误 |
| 4011 | 文件删除错误 |
| 4012 | 文件操作错误 |
| 4013 | 文件大小超出限制 |
| 4014 | 文件类型不允许 |
| 4015 | URL生成错误 |
| **4019** | **文件路径格式不正确** |

## 8. 目录结构示例

```txt
mine-user/                    # 用户相关文件桶
├── avatar/                   # 用户头像
│   ├── user-123/             # 用户ID
│   │   └── 550e8400-e29b-41d4-a716-446655440000.jpg
│   └── user-456/
│       └── 550e8400-e29b-41d4-a716-446655440001.jpg
└── id-card/                  # 身份证
    ├── user-123/
    │   ├── 550e8400-e29b-41d4-a716-446655440010.jpg
    │   └── 550e8400-e29b-41d4-a716-446655440011.jpg
    └── user-789/
        └── 550e8400-e29b-41d4-a716-446655440012.jpg

mine-message/                 # 消息相关文件桶
└── chat-image/               # 沟通图片
    ├── user-123/
    │   └── 550e8400-e29b-41d4-a716-446655440020.jpg
    └── user-456/
        └── 550e8400-e29b-41d4-a716-446655440021.jpg

mine-ai/                      # AI相关文件桶
└── report/                   # AI分析报告
    └── 550e8400-e29b-41d4-a716-446655440030.pdf

mineguard/                    # 默认桶
└── other/                    # 其他文件
    └── default/
        └── 550e8400-e29b-41d4-a716-446655440040.jpg
```

## 9. 变更记录

| 日期 | 变更内容 |
| ---- | ------- |
| 2026-03-05 | 初始版本，定义多桶设计和有意义的目录结构 |
