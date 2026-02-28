#!/bin/bash

# MineGuard 容器环境目录创建脚本
# 用于创建 Docker 容器挂载目录结构
# 根目录: ~/DockerFiles/

# 设置根目录
ROOT_DIR="$HOME/DockerFiles"

echo "======================================="
echo "开始创建 MineGuard 容器环境目录结构"
echo "根目录: $ROOT_DIR"
echo "======================================="

# 创建主环境目录结构
mkdir -p "$ROOT_DIR/MineGuard"

# Redis 容器
mkdir -p "$ROOT_DIR/MineGuard/mineguard_redis/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_redis/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_redis/log"

# MySQL 主从容器（为后续迁移做准备）
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_master/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_master/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_master/log"

mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_slave/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_slave/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mysql_slave/log"

# MinIO 文件服务容器
mkdir -p "$ROOT_DIR/MineGuard/mineguard_minio/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_minio/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_minio/log"

# RabbitMQ 消息队列容器
mkdir -p "$ROOT_DIR/MineGuard/mineguard_rabbitmq/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_rabbitmq/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_rabbitmq/log"

# MongoDB 容器
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mongodb/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mongodb/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_mongodb/log"

# Nacos 配置中心容器
mkdir -p "$ROOT_DIR/MineGuard/mineguard_nacos/data"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_nacos/config"
mkdir -p "$ROOT_DIR/MineGuard/mineguard_nacos/log"

# 创建测试环境目录结构
mkdir -p "$ROOT_DIR/mineguard_test"

# 测试环境 Redis 容器
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_redis/config"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_redis/data"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_redis/log"

# 测试环境 MySQL 容器
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mysql/data"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mysql/config"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mysql/log"

# 测试环境 MinIO 容器
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_minio/data"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_minio/config"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_minio/log"

# 测试环境 MongoDB 容器
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mongodb/data"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mongodb/config"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_mongodb/log"

# 测试环境 RabbitMQ 容器
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_rabbitmq/data"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_rabbitmq/config"
mkdir -p "$ROOT_DIR/mineguard_test/mineguard_rabbitmq/log"

echo "======================================="
echo "目录结构创建完成！"
echo "======================================="
echo "已创建的目录结构："
echo
echo "主环境："
echo "$ROOT_DIR/MineGuard/"
echo "├── mineguard_redis/{config,data,log}"
echo "├── mineguard_mysql_master/{data,config,log}"
echo "├── mineguard_mysql_slave/{data,config,log}"
echo "├── mineguard_minio/{data,config,log}     [MinIO 文件服务]"
echo "├── mineguard_rabbitmq/{data,config,log}  [RabbitMQ 消息队列]"
echo "├── mineguard_mongodb/{data,config,log}"
echo "└── mineguard_nacos/{data,config,log}"
echo
echo "测试环境："
echo "$ROOT_DIR/mineguard_test/"
echo "├── mineguard_redis/{config,data,log}"
echo "├── mineguard_mysql/{data,config,log}"
echo "├── mineguard_minio/{data,config,log}     [MinIO 文件服务]"
echo "├── mineguard_mongodb/{data,config,log}"
echo "└── mineguard_rabbitmq/{data,config,log}  [RabbitMQ 消息队列]"
echo
echo "注意：此脚本创建的是目录结构，不包括实际的容器配置文件。"
echo "后续需要根据实际需求添加相应的配置文件。"
echo "======================================="
