# common-database模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 数据库模块配置
mineguard:
  database:
    # 是否启用数据库模块，默认值：true
    enabled: true
    
    # MyBatis-Plus 配置
    mybatis-plus:
      # 是否溢出分页（true=溢出到第一页，false=保持当前页），默认值：true
      overflow: true
      # Mapper XML 文件位置
      mapper-locations: "classpath*:mapper/**/*.xml"
      # 类型别名包路径
      type-aliases-package: "com.klzw.**.domain"
    
    # Druid 连接池配置
    druid:
      # 初始连接数，默认值：3
      initial-size: 3
      # 最小空闲连接数，默认值：3
      min-idle: 3
      # 最大活跃连接数，默认值：10
      max-active: 10
      # 最大等待时间（毫秒），默认值：60000
      max-wait: 60000
      # 间隔多久进行一次检测（毫秒），默认值：60000
      time-between-eviction-runs-millis: 60000
      # 连接最小生存时间（毫秒），默认值：300000
      min-evictable-idle-time-millis: 300000
      # 验证查询语句
      validation-query: "SELECT 1"
      # 空闲时验证，默认值：true
      test-while-idle: true
      # 借用时验证，默认值：false
      test-on-borrow: false
      # 归还时验证，默认值：false
      test-on-return: false
      # 启用PSCache，默认值：true
      pool-prepared-statements: true
      # PSCache大小，默认值：20
      max-pool-prepared-statement-per-connection-size: 20
      # 过滤器
      filters: "stat,wall,slf4j"
      # 连接属性
      connection-properties: "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000"
      # 监控页面用户名
      stat-view-username: "admin"
      # 监控页面密码
      stat-view-password: ""
      # 监控页面允许访问的IP
      stat-view-allow: ""
    
    # 动态数据源配置
    dynamic:
      # 是否启用动态数据源，默认值：false
      enabled: false
      # 主数据源名称，默认值："master"
      primary: "master"
      # 从数据源名称列表
      slaves: ["slave"]
    
    # 数据源配置
    datasource:
      # 主数据源
      master:
        url: "jdbc:mysql://localhost:3306/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai"
        username: "root"
        password: "root"
        driver-class-name: "com.mysql.cj.jdbc.Driver"
      # 从数据源（仅动态数据源启用时生效）
      slave:
        url: "jdbc:mysql://localhost:3307/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai"
        username: "root"
        password: "root"
        driver-class-name: "com.mysql.cj.jdbc.Driver"

# MyBatis-Plus 全局配置
mybatis-plus:
  # Mapper XML 文件位置
  mapper-locations: "classpath*:mapper/**/*.xml"
  # 类型别名包路径
  type-aliases-package: "com.klzw.service.**.entity"
  # 配置
  configuration:
    # 下划线转驼峰，默认值：true
    map-underscore-to-camel-case: true
  # 全局配置
  global-config:
    # 数据库配置
    db-config:
      # ID 类型，默认值："assign_uuid"
      id-type: "assign_uuid"
      # 逻辑删除字段
      logic-delete-field: "deleted"
      # 逻辑删除值
      logic-delete-value: 1
      # 逻辑未删除值
      logic-not-delete-value: 0
```

## 2. 最小配置示例

```yaml
# 数据库模块配置（最小配置）
mineguard:
  database:
    # 数据源配置（必需）
    datasource:
      master:
        url: "jdbc:mysql://localhost:3306/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai"
        username: "root"
        password: "root"
        driver-class-name: "com.mysql.cj.jdbc.Driver"
      # 从数据源（仅启用动态数据源时需要）
      # slave:
      #   url: "jdbc:mysql://localhost:3307/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai"
      #   username: "root"
      #   password: "root"
      #   driver-class-name: "com.mysql.cj.jdbc.Driver"
    
    # 动态数据源（可选，默认 false）
    # dynamic:
    #   enabled: true

# MyBatis-Plus 全局配置
mybatis-plus:
  mapper-locations: "classpath*:mapper/**/*.xml"
  type-aliases-package: "com.klzw.service.**.entity"
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: "assign_uuid"
      logic-delete-field: "deleted"
      logic-delete-value: 1
      logic-not-delete-value: 0
```
