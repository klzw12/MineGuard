# MineGuard 容器配置说明

## 配置文件

- `docker-compose-prod.yml` - 正式环境 Docker Compose 配置文件（包含敏感信息，已添加到 .gitignore）
- `docker-compose-test.yml` - 测试环境 Docker Compose 配置文件
- `redis.conf` - Redis 服务配置文件（包含敏感信息，已添加到 .gitignore）
- `README.md` - 配置说明文档
- `.gitignore` - Git 忽略文件配置

## 配置说明

### 网络配置

- **子网**: `192.168.111.0/24`
- **网关**: `192.168.111.1`

### 服务 IP 分配

| 服务 | IP 地址 | 端口 | 重启策略 | 环境 |
| ---- | ------- | ---- | -------- | ---- |
| redis | 192.168.111.128 | 6379 | unless-stopped | 正式 |
| minio | 192.168.111.129 | 9000/9001 | unless-stopped | 正式 |
| rabbitmq | 192.168.111.130 | 5672/15672 | unless-stopped | 正式 |
| mongodb | 192.168.111.131 | 27017 | unless-stopped | 正式 |
| nacos | 192.168.111.132 | 8848/9848/9555 | unless-stopped | 正式 |
| redis-test | 192.168.111.133 | 6380 | no | 测试 |
| minio-test | 192.168.111.134 | 9002/9003 | no | 测试 |
| rabbitmq-test | 192.168.111.135 | 5673/15673 | no | 测试 |
| mongodb-test | 192.168.111.136 | 27018 | no | 测试 |

### 卷挂载

所有服务都使用之前创建的目录结构：

- `~/DockerFiles/MineGuard/{service_name}/{data,config,log}`

### 环境配置

- **正式环境**: 重启策略为 `unless-stopped`
- **测试环境**: 重启策略为 `no`
- **时区**: 统一设置为 `Asia/Shanghai`

## 使用方法

### 1. 准备目录结构

首先运行目录创建脚本：

- **Windows**: `../create_mineguard_directories.bat`
- **Linux**: `../create_mineguard_directories.sh`

### 2. 复制配置文件

将 `redis.conf` 复制到配置目录：

```bash
# Linux
cp redis.conf ~/DockerFiles/MineGuard/mineguard_redis/config/

# Windows
copy redis.conf %USERPROFILE%\DockerFiles\MineGuard\mineguard_redis\config\
```

### 3. 启动容器

```bash
# 启动正式环境服务
docker-compose -f docker-compose-prod.yml up -d

# 启动测试环境服务
docker-compose -f docker-compose-test.yml up -d
```

### 4. 停止容器

```bash
# 停止正式环境服务
docker-compose -f docker-compose-prod.yml down

# 停止测试环境服务
docker-compose -f docker-compose-test.yml down

# 停止指定服务
docker-compose -f docker-compose-prod.yml stop redis minio
```

### 5. 查看服务状态

```bash
# 查看正式环境服务状态
docker-compose -f docker-compose-prod.yml ps

# 查看测试环境服务状态
docker-compose -f docker-compose-test.yml ps
```

## 服务访问

### Redis

- **正式**: `redis://192.168.111.128:6379`
- **测试**: `redis://192.168.111.133:6380`
- **测试密码**: `testpass`
- **正式密码**: 请在容器配置中设置

### MinIO

- **正式**: `http://192.168.111.129:9000`
- **测试**: `http://192.168.111.134:9002`
- **控制台**: `http://192.168.111.129:9001`
- **账号**: 请在容器配置中设置
- **密码**: 请在容器配置中设置

### RabbitMQ

- **正式 AMQP**: `amqp://192.168.111.130:5672`
- **正式 管理控制台**: `http://192.168.111.130:15672`
- **测试 AMQP**: `amqp://192.168.111.135:5673`
- **测试 管理控制台**: `http://192.168.111.135:15673`
- **账号**: 请在容器配置中设置
- **密码**: 请在容器配置中设置

### MongoDB

- **正式**: `mongodb://192.168.111.131:27017`
- **测试**: `mongodb://192.168.111.136:27018`
- **账号**: 请在容器配置中设置
- **密码**: 请在容器配置中设置

### Nacos

- **地址**: `http://192.168.111.132:8848/nacos`
- **账号**: 请在容器配置中设置
- **密码**: 请在容器配置中设置

## 自定义配置

### 修改密码

1. **Redis**: 编辑 `redis.conf` 文件，修改 `requirepass` 配置
2. **MinIO**: 修改 `docker-compose-all.yml` 中的 `MINIO_ROOT_USER` 和 `MINIO_ROOT_PASSWORD`
3. **RabbitMQ**: 修改 `docker-compose-all.yml` 中的 `RABBITMQ_DEFAULT_USER` 和 `RABBITMQ_DEFAULT_PASS`
4. **MongoDB**: 修改 `docker-compose-all.yml` 中的 `MONGO_INITDB_ROOT_USERNAME` 和 `MONGO_INITDB_ROOT_PASSWORD`

### 修改内存限制

- **Redis**: 编辑 `redis.conf` 文件，修改 `maxmemory` 配置

### 修改网络配置

- 编辑 `docker-compose-all.yml` 文件，修改网络配置部分

## 注意事项

1. 确保 `~/DockerFiles/MineGuard` 目录结构存在
2. 相关端口未被占用
3. 防火墙允许相关端口访问
4. 生产环境建议修改默认密码
5. 测试环境服务重启策略为 `no`，不会自动重启

## 监控

```bash
# 查看正式环境容器日志
docker-compose -f docker-compose-prod.yml logs

# 查看测试环境容器日志
docker-compose -f docker-compose-test.yml logs

# 查看指定容器日志
docker-compose -f docker-compose-prod.yml logs redis minio rabbitmq

# 查看容器状态
docker-compose -f docker-compose-prod.yml ps
```

## MQ 选型说明

**选择 RabbitMQ 的原因**：

1. **强大的可视化控制台**：提供丰富的管理功能，便于监控和管理消息队列
2. **易于部署**：单节点部署简单，适合快速上手
3. **实时性好**：低延迟，适合微服务间的实时通信
4. **协议支持**：支持多种协议，灵活性高
5. **社区活跃**：文档丰富，问题容易解决
6. **可靠性高**：支持消息确认机制，确保消息不丢失

**访问控制台**：

- 正式环境: <http://192.168.111.130:15672>
- 测试环境: <http://192.168.111.135:15673>
- 账号: admin
- 密码: rabbitmqadmin
