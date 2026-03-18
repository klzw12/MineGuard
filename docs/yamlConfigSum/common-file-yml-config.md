# common-file模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 文件存储模块配置
mineguard:
  # 文件存储配置
  file:
    # 存储类型，默认值：ALIYUN_OSS
    # 可选值：ALIYUN_OSS, LOCAL, S3
    storage-type: "ALIYUN_OSS"
    # 文件大小限制（MB），默认值：100
    max-file-size: 100
    # 允许的文件类型
    allowed-types:
      - "image/jpeg"
      - "image/png"
      - "image/gif"
      - "application/pdf"
      - "application/msword"
      - "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    # 基础路径
    base-path: "mineguard"
    # 临时文件路径
    temp-path: "/tmp/mineguard"

  # 阿里云OSS配置
  aliyun:
    oss:
      # 端点
      endpoint: "oss-cn-hangzhou.aliyuncs.com"
      # 访问密钥ID
      access-key-id: "your-access-key-id"
      # 访问密钥 secret
      access-key-secret: "your-access-key-secret"
      # 存储桶名称
      bucket-name: "mineguard-files"
      # 区域
      region: "cn-hangzhou"
      # 启用路径样式访问，默认值：false
      path-style-access: false
      # 连接超时时间（毫秒），默认值：5000
      connect-timeout: 5000
      # 读取超时时间（毫秒），默认值：10000
      read-timeout: 10000

  # 百度AI配置
  baidu:
    ai:
      # API密钥
      api-key: "your-api-key"
      # 密钥
      secret-key: "your-secret-key"
      # 身份证识别接口URL
      idcard-url: "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard"
      # 驾驶证识别接口URL
      driving-license-url: "https://aip.baidubce.com/rest/2.0/ocr/v1/driving_license"
      # 行驶证识别接口URL
      vehicle-license-url: "https://aip.baidubce.com/rest/2.0/ocr/v1/vehicle_license"
      # 车牌识别接口URL
      license-plate-url: "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate"
      # 通用文字识别接口URL
      general-url: "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic"

# 日志配置
logging:
  level:
    # 文件模块日志级别
    "com.klzw.common.file": "info"
    # 阿里云OSS日志级别
    "com.aliyun.oss": "warn"
    # 百度AI日志级别
    "com.baidu.aip": "warn"
```

## 2. 最小配置示例

```yaml
# 文件存储模块配置（最小配置）
mineguard:
  # 文件存储配置（可选，使用默认值）
  # file:
  #   storage-type: "ALIYUN_OSS"
  #   max-file-size: 100

  # 阿里云OSS配置（必须）
  aliyun:
    oss:
      endpoint: "oss-cn-hangzhou.aliyuncs.com"
      access-key-id: "your-access-key-id"
      access-key-secret: "your-access-key-secret"
      bucket-name: "mineguard-files"
      region: "cn-hangzhou"

# 日志配置
logging:
  level:
    "com.klzw.common.file": "info"
```
