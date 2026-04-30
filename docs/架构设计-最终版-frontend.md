# MineGuard 前端架构设计文档

## 1. 技术选型

### 1.1 核心框架

- **框架**：uni-app x
- **Vue 版本**：Vue 3
- **开发语言**：TypeScript

### 1.2 技术特性

- **响应式设计**：适配多端设备
- **组件化开发**：提高代码复用性
- **路由管理**：统一的页面导航
- **状态管理**：全局状态共享
- **网络请求**：API 接口调用
- **多入口设计**：管理端和用户端分离

## 2. 项目结构

### 2.1 整体架构

采用 **管理端和用户端分离，App 多入口的混合方案**，具体结构如下：

```tree
frontend/
├── pages/           # 页面文件
│   ├── admin/       # 管理端页面
│   │   ├── ai/          # AI分析
│   │   ├── appeal/      # 申诉管理
│   │   ├── attendance/  # 考勤管理
│   │   ├── cost/        # 成本管理
│   │   ├── create-user/ # 创建用户
│   │   ├── dispatch/    # 调度管理
│   │   ├── message/     # 消息管理
│   │   ├── monitor/     # 实时监控
│   │   ├── profile/     # 个人中心
│   │   ├── report/      # 统计报表
│   │   ├── role/        # 角色管理
│   │   ├── role-change/ # 角色变更
│   │   ├── route/       # 路线管理
│   │   ├── salary/      # 薪资管理
│   │   ├── statistics/  # 统计分析
│   │   ├── track/       # 轨迹回放
│   │   ├── trip/        # 行程管理
│   │   ├── user/        # 用户管理
│   │   ├── vehicle/     # 车辆管理
│   │   ├── verify/      # 审核管理
│   │   └── warning/     # 预警管理
│   ├── user/        # 用户端页面
│   │   ├── appeal/      # 申诉
│   │   ├── change-password/ # 修改密码
│   │   ├── change-phone/    # 修改手机号
│   │   ├── fault/       # 故障上报
│   │   ├── map/         # 地图
│   │   ├── message/     # 消息
│   │   ├── profile/     # 个人中心
│   │   ├── qualification/ # 资质管理
│   │   ├── repair/      # 维修
│   │   ├── task/        # 任务管理
│   │   ├── track-test/  # 轨迹测试
│   │   ├── vehicle/     # 车辆状态
│   │   └── warning/     # 预警
│   ├── auth/        # 认证页面
│   │   ├── login.uvue   # 登录
│   │   ├── register.uvue # 注册
│   │   └── reset-password.uvue # 重置密码
│   └── index/       # 首页
│       └── index.uvue
├── static/          # 静态资源
│   ├── avatar/      # 头像图片
│   ├── images/      # 图片资源
│   ├── logo/        # Logo
│   └── svgs/        # SVG 图标
├── components/      # 组件
│   ├── AdminLayout.uvue   # 管理端布局
│   ├── AdminSidebar.uvue  # 管理端侧边栏
│   ├── CustomPicker.uvue  # 自定义选择器
│   ├── ECharts.uvue       # ECharts图表
│   ├── EmptyState.uvue    # 空状态
│   ├── FilterButtons.uvue # 筛选按钮
│   ├── Pagination.uvue    # 分页组件
│   ├── StatusTag.uvue     # 状态标签
│   ├── echarts-uniapp/    # ECharts组件
│   └── jn-chart/          # 图表组件
├── stores/          # 状态管理
│   └── user.uts     # 用户状态
├── types/           # 类型定义
│   ├── dispatch.uts # 调度类型
│   ├── trip.uts     # 行程类型
│   ├── user.uts     # 用户类型
│   └── vehicle.uts  # 车辆类型
├── utils/           # 工具函数
│   ├── config.uts   # 配置
│   ├── echarts.uts  # ECharts工具
│   ├── gaode.uts    # 高德地图
│   ├── image.uts    # 图片工具
│   ├── request.uts  # 请求封装
│   ├── toast.uts    # 提示工具
│   └── trackSimulator.uts # 轨迹模拟
├── api/             # API 接口
│   ├── ai.uts       # AI接口
│   ├── appeal.uts   # 申诉接口
│   ├── attendance.uts # 考勤接口
│   ├── auth.uts     # 认证接口
│   ├── cost.uts     # 成本接口
│   ├── dispatch.uts # 调度接口
│   ├── driver.uts   # 司机接口
│   ├── event.uts    # 事件接口
│   ├── message.uts  # 消息接口
│   ├── qualification.uts # 资质接口
│   ├── statistics.uts # 统计接口
│   ├── trip.uts     # 行程接口
│   ├── user.uts     # 用户接口
│   ├── vehicle.uts  # 车辆接口
│   └── warning.uts  # 预警接口
├── uni_modules/     # uni-app模块
│   └── e-chart/     # ECharts模块
├── styles/          # 样式文件
│   └── admin.scss   # 管理端样式
├── App.uvue         # 应用入口
├── main.uts         # 主入口
├── pages.json       # 页面配置
├── manifest.json    # 应用配置
└── uni.scss         # 全局样式
```

### 2.2 多入口设计

- **App 多入口**：通过 `App.uvue` 实现不同角色的入口切换
- **登录页面分离**：管理端和用户端分别有独立的登录页面
- **路由配置分离**：在 `pages.json` 中配置不同角色的路由
- **权限控制**：基于角色的权限管理

## 3. 页面设计

### 3.1 认证页面

- **登录页面**：`pages/auth/login.uvue`
- **注册页面**：`pages/auth/register.uvue`
- **重置密码**：`pages/auth/reset-password.uvue`
- **功能**：账号密码登录、注册、密码重置
- **角色**：管理员、司机

### 3.2 首页

- **路径**：`pages/index/index.uvue`
- **功能**：数据概览、快捷操作
- **模块**：
  - 数据概览（车辆总数、在线车辆、今日运量、今日预警）
  - 快捷入口（车辆管理、人员管理、调度管理、实时监控、统计分析、成本管理）
  - 最新预警

### 3.3 管理端页面

#### 3.3.1 车辆管理

- **路径**：`pages/admin/vehicle/index.uvue`
- **详情**：`pages/admin/vehicle/detail.uvue`
- **功能**：车辆信息管理、状态监控

#### 3.3.2 用户管理

- **路径**：`pages/admin/user/index.uvue`
- **创建用户**：`pages/admin/create-user/index.uvue`
- **功能**：用户信息管理、用户创建

#### 3.3.3 调度管理

- **路径**：`pages/admin/dispatch/index.uvue`
- **位置选择**：`pages/admin/dispatch/location-picker.uvue`
- **功能**：调度任务管理、位置选择

#### 3.3.4 实时监控

- **路径**：`pages/admin/monitor/index.uvue`
- **功能**：车辆实时位置、状态监控

#### 3.3.5 统计报表

- **路径**：`pages/admin/report/index.uvue`
- **司机报表**：`pages/admin/report/driver.uvue`
- **车辆报表**：`pages/admin/report/vehicle.uvue`
- **功能**：数据统计、报表生成

#### 3.3.6 统计分析

- **路径**：`pages/admin/statistics/index.uvue`
- **功能**：数据可视化分析

#### 3.3.7 成本管理

- **路径**：`pages/admin/cost/index.uvue`
- **功能**：成本核算、预算管理

#### 3.3.8 薪资管理

- **路径**：`pages/admin/salary/index.uvue`
- **功能**：薪资配置、提成计算

#### 3.3.9 预警管理

- **路径**：`pages/admin/warning/index.uvue`
- **功能**：预警规则配置、预警记录查看

#### 3.3.10 行程管理

- **路径**：`pages/admin/trip/index.uvue`
- **详情**：`pages/admin/trip/detail.uvue`
- **功能**：行程列表、行程详情

#### 3.3.11 轨迹回放

- **路径**：`pages/admin/track/playback.uvue`
- **功能**：历史轨迹回放

#### 3.3.12 消息管理

- **广播消息**：`pages/admin/message/broadcast.uvue`
- **私信管理**：`pages/admin/message/private.uvue`
- **死信队列**：`pages/admin/message/dead-letter.uvue`
- **功能**：消息推送管理

#### 3.3.13 角色管理

- **路径**：`pages/admin/role/index.uvue`
- **角色变更**：`pages/admin/role-change/index.uvue`
- **功能**：角色配置、角色变更审核

#### 3.3.14 路线管理

- **路径**：`pages/admin/route/index.uvue`
- **功能**：路线模板管理

#### 3.3.15 考勤管理

- **路径**：`pages/admin/attendance/index.uvue`
- **功能**：考勤记录查看

#### 3.3.16 申诉管理

- **路径**：`pages/admin/appeal/index.uvue`
- **功能**：申诉审核处理

#### 3.3.17 审核管理

- **路径**：`pages/admin/verify/index.uvue`
- **功能**：资质审核、变更审核

#### 3.3.18 AI分析

- **路径**：`pages/admin/ai/index.uvue`
- **功能**：AI数据分析

#### 3.3.19 个人中心

- **路径**：`pages/admin/profile/index.uvue`
- **功能**：管理员信息、系统设置

### 3.4 用户端页面

#### 3.4.1 任务管理

- **路径**：`pages/user/task/index.uvue`
- **功能**：任务列表、任务执行

#### 3.4.2 车辆状态

- **路径**：`pages/user/vehicle/index.uvue`
- **功能**：车辆信息、状态检查

#### 3.4.3 故障上报

- **路径**：`pages/user/fault/index.uvue`
- **功能**：故障快速上报

#### 3.4.4 维修管理

- **路径**：`pages/user/repair/index.uvue`
- **功能**：维修申请、维修记录

#### 3.4.5 预警查看

- **路径**：`pages/user/warning/index.uvue`
- **功能**：预警消息查看

#### 3.4.6 消息中心

- **路径**：`pages/user/message/index.uvue`
- **功能**：消息接收、查看

#### 3.4.7 地图

- **路径**：`pages/user/map/index.uvue`
- **功能**：地图导航、位置查看

#### 3.4.8 资质管理

- **路径**：`pages/user/qualification/index.uvue`
- **功能**：资质上传、查看

#### 3.4.9 申诉

- **路径**：`pages/user/appeal/index.uvue`
- **功能**：申诉提交

#### 3.4.10 个人中心

- **路径**：`pages/user/profile/index.uvue`
- **功能**：司机信息、个人设置

#### 3.4.11 修改密码

- **路径**：`pages/user/change-password/index.uvue`
- **功能**：密码修改

#### 3.4.12 修改手机号

- **路径**：`pages/user/change-phone/index.uvue`
- **功能**：手机号修改

## 4. 功能模块

### 4.1 认证授权

- **登录认证**：用户名密码登录
- **角色权限**：基于角色的权限控制
  - 管理员角色：完全访问权限
  - 司机角色：有限访问权限
- **状态管理**：登录状态持久化，角色信息存储

### 4.2 管理端功能模块

#### 4.2.1 车辆管理

- **车辆信息**：车辆基本信息维护
- **状态监控**：车辆实时状态监控
- **调度管理**：车辆调度分配

#### 4.2.2 司机管理

- **司机信息**：司机基本信息维护
- **任务分配**：任务指派和管理
- **绩效统计**：司机绩效数据统计

#### 4.2.3 实时监控

- **位置追踪**：车辆实时位置监控
- **状态监控**：车辆运行状态监控
- **异常预警**：异常情况实时预警

#### 4.2.4 统计报表

- **数据统计**：各项数据统计分析
- **报表生成**：自定义报表生成
- **趋势分析**：数据趋势图表展示

### 4.3 用户端功能模块

#### 4.3.1 任务管理

- **任务列表**：接收和查看任务
- **任务执行**：任务开始、暂停、完成
- **任务详情**：任务详细信息查看

#### 4.3.2 车辆状态

- **状态检查**：车辆各项状态检查
- **故障上报**：车辆故障快速上报
- **维护记录**：车辆维护历史记录

#### 4.3.3 个人中心

- **个人信息**：司机个人信息管理
- **工作统计**：个人工作数据统计
- **设置管理**：个人账号设置

### 4.4 数据展示

- **数据概览**：统计数据卡片
- **预警列表**：分级预警展示
- **车辆状态**：实时状态监控

### 4.5 交互功能

- **导航跳转**：页面间导航
- **操作反馈**：Toast 提示
- **模态框**：确认操作
- **关于我们**：系统介绍、版本信息

## 5. 技术实现

### 5.1 路由配置

- **pages.json**：配置页面路径、导航栏样式
- **tabBar**：底部导航栏
- **uniIdRouter**：登录页面配置

### 5.2 状态管理

- **本地存储**：uni.getStorageSync / uni.setStorageSync
- **组件状态**：Vue 3 ref / reactive
- **全局状态**：使用 Vue 3 provide/inject 或 Pinia 管理全局状态

### 5.3 样式设计

- **主题色**：蓝色渐变 (#007AFF → #0056b3)
- **布局**：卡片式布局
- **响应式**：适配不同屏幕尺寸
- **动画**：流畅的过渡效果

## 6. 性能优化

### 6.1 加载优化

- **资源压缩**：图片、SVG 优化
- **懒加载**：按需加载组件

### 6.2 渲染优化

- **虚拟列表**：长列表优化
- **减少重绘**：合理使用 CSS

### 6.3 网络优化

- **请求缓存**：减少重复请求
- **批量请求**：合并 API 调用

## 7. 兼容性

### 7.1 多端适配

- **H5**：浏览器
- **小程序**：微信小程序
- **App**：iOS / Android

### 7.2 屏幕适配

- **响应式布局**：适配不同屏幕尺寸
- **字体大小**：rpx 单位

## 8. 安全性

### 8.1 数据安全

- **本地存储**：敏感信息加密
- **网络请求**：HTTPS

### 8.2 权限控制

- **角色权限**：基于角色的访问控制
- **页面权限**：路由守卫

## 9. 开发规范

### 9.1 代码规范

- **命名规范**：驼峰命名
- **注释规范**：清晰的代码注释
- **代码风格**：统一的代码格式

### 9.2 版本控制

- **分支管理**：
  - main：主分支
  - frontend：前端开发分支
  - backend：后端开发分支

### 9.3 开发流程

1. 需求分析
2. 原型设计
3. 代码实现
4. 测试验证
5. 部署上线

## 10. 未来规划

### 10.1 功能扩展

- **车辆管理**：车辆信息维护
- **人员管理**：司机信息管理
- **调度管理**：智能调度系统
- **实时监控**：车辆实时位置
- **统计分析**：数据可视化
- **成本管理**：成本核算

### 10.2 技术升级

- **组件库**：自定义组件库
- **状态管理**：Pinia
- **构建工具**：Vite
- **测试框架**：Jest

## 11. 总结

本架构设计文档详细说明了 MineGuard 前端项目的技术选型、项目结构、页面设计、功能模块、技术实现、性能优化、兼容性、安全性、开发规范和未来规划。通过 uni-app x 框架，实现了跨平台的矿山车辆运输管理系统前端，为用户提供了便捷、高效、安全的管理工具。
