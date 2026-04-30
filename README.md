# MineGuard 矿山车辆运输管理系统

> 基于 Spring Boot 3.5 + Spring Cloud Alibaba 的矿山车辆运输智能管理系统，支持实时调度、轨迹追踪、智能预警与成本核算。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud Alibaba](https://img.shields.io/badge/SCA-2025.0.0.0-blue)](https://github.com/alibaba/spring-cloud-alibaba)
[![Java](https://img.shields.io/badge/Java-24-orange)](https://www.oracle.com/java/)
[![uni-app x](https://img.shields.io/badge/Frontend-uni--app%20x-00BFFF)](https://uniapp.dcloud.net.cn/)

---

## 📖 项目简介

MineGuard 是一套面向矿山企业的车辆运输智能管理平台，采用微服务架构，整合了**车辆管理、人员管理、智能调度、路径监管、实时预警、统计分析和成本管理**七大核心模块。系统通过 GPS 定位、WebSocket 实时通信和 AI 智能分析，实现矿山运输全流程的数字化、可视化管理。

### 核心特性

- 🚛 **智能调度** — 动态调度规划，车辆损坏/司机请假时自动重排
- 📍 **实时追踪** — GPS 轨迹实时上报，地图可视化，支持轨迹回放
- ⚠️ **智能预警** — 超速、偏航、停留、盗卸等多维度预警，WebSocket 实时推送
- 📊 **数据分析** — 多维度统计报表（车辆/司机/运输量/成本），ECharts 可视化
- 🤖 **AI 助手** — 集成 LLM，支持自然语言查询运营数据、驾驶行为分析
- 🔐 **权限管理** — RBAC 五角色（管理员/调度员/司机/维修员/安全员）

---

## 🏗️ 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | uni-app x + Vue 3 + TypeScript + ECharts |
| **网关** | Spring Cloud Gateway |
| **后端** | Spring Boot 3.5.10 + Spring Cloud Alibaba 2025.0.0.0 |
| **服务治理** | Nacos（注册中心 + 配置中心） |
| **服务调用** | HttpExchange（Java 11+ 原生 HTTP 客户端） |
| **数据库** | MySQL 8.0（业务）+ MongoDB 7.0（轨迹） |
| **缓存** | Redis 7.2 + Redisson（Sentinel 模式） |
| **消息队列** | RabbitMQ 3.12 |
| **文件存储** | MinIO |
| **AI** | Spring AI + LLM（通义千问/文心一言） |
| **地图** | 高德地图 API |

---

## 📁 项目结构

```
MineGuard/
├── backend/                          # 后端微服务
│   ├── pom.xml                       # Maven 父 POM
│   ├── mineguard-common-*/           # 公共模块（core/auth/redis/db/mq/file/map/websocket/web）
│   ├── mineguard-service-gateway/    # 网关服务 :8080
│   ├── mineguard-service-user/       # 用户服务 :8081
│   ├── mineguard-service-vehicle/    # 车辆服务 :8082
│   ├── mineguard-service-dispatch/   # 调度服务 :8083
│   ├── mineguard-service-trip/       # 行程服务 :8084
│   ├── mineguard-service-warning/    # 预警服务 :8085
│   ├── mineguard-service-statistics/ # 统计服务 :8086
│   ├── mineguard-service-cost/       # 成本服务 :8087
│   ├── mineguard-service-ai/         # AI 服务 :8088
│   └── mineguard-service-python/     # Python 代理服务 :8009
├── frontend/                         # 前端（uni-app x）
│   ├── pages/                        # 页面
│   ├── api/                          # API 接口
│   ├── components/                   # 组件（ECharts）
│   ├── pages.json                    # 页面路由配置
│   └── manifest.json                 # 应用配置
├── python-service/                   # Python 数据清洗/AI 服务
├── docs/                             # 项目文档
│   ├── Forenv/                       # Docker 环境部署
│   ├── xml/                          # 架构图源文件（draw.io）
│   └── test.md                       # 测试文档
├── scripts/                          # 工具脚本
├── .env                              # 环境变量模板
└── README.md                         # 本文件
```

---

## 🚀 快速启动

### 前置要求

| 组件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 24 | 后端运行环境 |
| Maven | 3.9+ | 项目构建 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.2 | 缓存 |
| MongoDB | 7.0 | 轨迹存储 |
| RabbitMQ | 3.12 | 消息队列 |
| Nacos | 2.3 | 服务注册与配置中心 |
| MinIO | Latest | 文件存储 |
| Python | 3.10+ | Python 代理服务 |

---

### 第一步：启动基础设施（Docker）

项目使用 `forenv` 管理 Docker 容器环境，测试环境配置在 `docs/Forenv/compose/docker-compose-test.yml`。

```bash
# 1. 创建数据目录
cd docs/Forenv
bash create_mineguard_directories.sh

# 2. 启动中间件（Redis / MinIO / RabbitMQ / MongoDB）
cd compose
docker compose -f docker-compose-test.yml up -d

# 3. 验证
docker compose -f docker-compose-test.yml ps
```

| 服务 | 端口 | 账号 | 密码 |
|------|------|------|------|
| Redis | 6380 | - | testpass |
| MinIO | 9002 / 9003(Console) | admin | minioadmin |
| RabbitMQ | 5673 / 15673(Console) | admin | rabbitmqadmin |
| MongoDB | 27018 | admin | mongodbadmin |

> **生产环境部署**：参照 `docker-compose-test.yml` 创建 `docker-compose-prod.yml`，修改密码、重启策略为 `unless-stopped`，并配置资源限制。

### 第二步：启动 Nacos 并导入配置

所有微服务配置由 Nacos 统一管理。各服务的 `application-dev.yml` 中通过 `spring.config.import` 从 Nacos 拉取配置。

```bash
# 1. 启动 Nacos（Docker 或官方包）
# 访问 http://localhost:8848/nacos （nacos/nacos）

# 2. 创建命名空间
#    - dev（各服务私有配置）
#    - Shared_Dev（共享配置：数据库、Redis、认证等）

# 3. 导入配置
#    将 backend/mineguard-common-config/ 下的 yml 文件导入 Nacos
#    参考各服务的 application-dev.yml 中的 spring.config.import 列表

# 4. 设置环境变量（或修改 application-dev.yml 中的默认值）
export NACOS_SERVER_IP=127.0.0.1
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos
```

**配置参考**：
- 服务基础配置：`backend/mineguard-service-*/src/main/resources/application.yml`（仅定义服务名和端口）
- 开发环境配置：`backend/mineguard-service-*/src/main/resources/application-dev.yml`（引用 Nacos 配置）
- 共享配置需在 Nacos 中手动创建：`common-database.yml`、`common-redis.yml`、`common-auth.yml`、`common-web.yml`、`common-mq.yml`、`common-mongo.yml`、`common-websocket.yml`、`common-file.yml` 等

### 第三步：初始化数据库

```bash
# 导入数据库表结构
mysql -u root -p < docs/sql/define.sql
```

### 第四步：启动后端服务

```bash
cd backend

# 编译
mvn clean package -DskipTests

# 按顺序启动（各服务独立进程）
java -jar mineguard-service-gateway/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-user/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-vehicle/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-dispatch/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-trip/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-warning/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-statistics/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-cost/target/*.jar --spring.profiles.active=dev
java -jar mineguard-service-ai/target/*.jar --spring.profiles.active=dev
```

**Python 代理服务**：
```bash
cd python-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8009
```

### 第五步：启动前端

```bash
cd frontend

# 方式一：HBuilderX（推荐）
# 1. 打开 HBuilderX
# 2. 文件 → 打开目录 → 选择 frontend/
# 3. 运行 → 运行到浏览器 → Chrome
# 4. 或 运行 → 运行到手机/模拟器

# 方式二：CLI
npm install
npx cross-env UNI_PLATFORM=app dev
```

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [架构设计 - 后端](docs/架构设计-最终版-backend.md) | 微服务架构、服务拆分、技术选型 |
| [架构设计 - 前端](docs/架构设计-最终版-frontend.md) | 前端架构、页面路由、组件设计 |
| [后端拆解](docs/后端拆解-构建顺序.md) | 模块构建顺序与依赖关系 |
| [测试文档](docs/test.md) | 测试用例与执行结果 |
| [Forenv 配置](docs/Forenv/compose/README.md) | Docker 环境部署详细说明 |

---

## 🧪 测试

```bash
cd backend

# 运行所有单元测试
mvn test

# 运行指定模块测试
mvn test -pl mineguard-service-trip

# 运行集成测试
mvn verify -Pintegration-test
```

---

## 📄 License

MIT License

## 👤 作者

- **klzw12** — [GitHub](https://github.com/klzw12)

---

*完整论文与架构文档详见 `docs/` 目录。*
