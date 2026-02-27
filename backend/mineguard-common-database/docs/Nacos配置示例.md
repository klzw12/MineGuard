# MineGuard 数据库配置 - Nacos配置中心

## 配置说明

本配置用于Nacos配置中心，统一管理所有服务模块的数据库连接信息。

## 配置前缀

所有数据库配置使用前缀：`mineguard.database`

## 配置项

### 1. MyBatis-Plus 配置

```yaml
mineguard:
  database:
    mybatis-plus:
      # 分页插件最大限制
      max-limit: 100
      # 分页插件是否溢出处理
      overflow: true
      # Mapper 扫描路径
      mapper-locations: classpath*:mapper/**/*.xml
      # 类型别名包路径
      type-aliases-package: com.klzw.**.domain
```

### 2. Druid 连接池配置

```yaml
mineguard:
  database:
    druid:
      # 初始连接数
      initial-size: 5
      # 最小空闲连接数
      min-idle: 5
      # 最大连接数
      max-active: 20
      # 获取连接超时时间（毫秒）
      max-wait: 60000
      # 连接有效性检查时间间隔（毫秒）
      time-between-eviction-runs-millis: 60000
      # 连接最大空闲时间（毫秒）
      min-evictable-idle-time-millis: 300000
      # 连接有效性检查 SQL
      validation-query: SELECT 1 FROM DUAL
      # 是否在空闲时检查连接有效性
      test-while-idle: true
      # 是否在获取连接时检查有效性
      test-on-borrow: false
      # 是否在归还连接时检查有效性
      test-on-return: false
      # 是否开启池化连接
      pool-prepared-statements: true
      # 池化连接最大数量
      max-pool-prepared-statement-per-connection-size: 20
      # 过滤器
      filters: stat,wall,log4j
      # 连接属性
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      # 监控页面访问账号
      stat-view-username: admin
      # 监控页面访问密码
      stat-view-password: ${DRUID_PASSWORD}
      # 监控页面访问允许IP（空表示允许所有）
      stat-view-allow: ""
```

### 3. 动态数据源配置

```yaml
mineguard:
  database:
    dynamic:
      # 是否启用动态数据源
      enabled: true
      # 主数据源名称
      primary: master
      # 从数据源名称列表
      slaves:
        - slave
```

### 4. Spring 数据源配置

```yaml
spring:
  datasource:
    # Druid 连接池配置
    druid:
      # 主数据源配置
      master:
        url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:mineguard}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${MYSQL_USERNAME:root}
        password: ${MYSQL_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver
      
      # 从数据源配置
      slave:
        url: jdbc:mysql://${MYSQL_SLAVE_HOST:localhost}:${MYSQL_SLAVE_PORT:3306}/${MYSQL_SLAVE_DATABASE:mineguard}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: ${MYSQL_SLAVE_USERNAME:root}
        password: ${MYSQL_SLAVE_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver
```

## 完整配置示例

### 开发环境

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.mysql.cj.jdbc.Driver
      slave:
        url: jdbc:mysql://localhost:3306/mineguard?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        username: root
        password: root
        driver-class-name: com.mysql.cj.jdbc.Driver

mineguard:
  database:
    mybatis-plus:
      max-limit: 100
      overflow: true
      mapper-locations: classpath*:mapper/**/*.xml
      type-aliases-package: com.klzw.**.domain
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,log4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      stat-view-username: admin
      stat-view-password: admin
      stat-view-allow: ""
    dynamic:
      enabled: true
      primary: master
      slaves:
        - slave
```

### 生产环境

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://${MYSQL_MASTER_HOST}:${MYSQL_MASTER_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
        username: ${MYSQL_USERNAME}
        password: ${MYSQL_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver
      slave:
        url: jdbc:mysql://${MYSQL_SLAVE_HOST}:${MYSQL_SLAVE_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
        username: ${MYSQL_SLAVE_USERNAME}
        password: ${MYSQL_SLAVE_PASSWORD}
        driver-class-name: com.mysql.cj.jdbc.Driver

mineguard:
  database:
    mybatis-plus:
      max-limit: 1000
      overflow: true
      mapper-locations: classpath*:mapper/**/*.xml
      type-aliases-package: com.klzw.**.domain
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 50
      filters: stat,wall,log4j
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      stat-view-username: ${DRUID_USERNAME}
      stat-view-password: ${DRUID_PASSWORD}
      stat-view-allow: ""
    dynamic:
      enabled: true
      primary: master
      slaves:
        - slave
```

## MySQL 主从复制配置说明

主从数据复制在MySQL数据库层面配置，应用层只负责读写分离：

### MySQL 主从复制配置（DBA配置）

1. **主库配置**（Master）
   - 配置binlog
   - 开启server-id
   - 配置复制用户权限

2. **从库配置**（Slave）
   - 配置master信息
   - 开启relay-log
   - 设置只读模式（可选）

3. **应用层职责**
   - 写操作（insert/update/delete）使用主数据源
   - 读操作（select/query）使用从数据源
   - 通过@DataSource注解可以手动指定数据源

## 使用说明

### 服务模块使用

服务模块无需配置数据库连接信息，只需：

1. 引入依赖：
```xml
<dependency>
    <groupId>com.klzw</groupId>
    <artifactId>mineguard-common-database</artifactId>
</dependency>
```

2. 在Nacos配置中心配置上述配置项

3. 使用读写分离：
```java
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    // 读操作 - 自动使用从数据源
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    // 写操作 - 自动使用主数据源
    public void saveUser(User user) {
        userMapper.insert(user);
    }
    
    // 手动指定数据源
    @DataSource("master")
    public User forceUseMaster(Long id) {
        return userMapper.selectById(id);
    }
}
```

### 监控页面访问

配置完成后，可通过以下URL访问Druid监控页面：
- URL: `http://localhost:8080/druid/index.html`
- 账号：配置中的 `stat-view-username`
- 密码：配置中的 `stat-view-password`

## 注意事项

1. **环境变量**：生产环境建议使用环境变量或K8s Secret管理敏感信息
2. **密码安全**：不要在配置文件中硬编码密码，使用环境变量引用
3. **连接池调优**：根据实际业务量调整连接池大小
4. **主从延迟**：MySQL主从复制存在延迟，强一致性场景需要使用主库
5. **配置热更新**：Nacos支持配置热更新，修改后无需重启服务
