# Docker Compose 部署指南

本项目使用 Docker Compose 来管理所有依赖服务，包括 Kafka、ZooKeeper、MySQL 和 Redis。

## 前置要求

- 已安装 Docker Desktop（Windows）或 Docker + Docker Compose（Linux/Mac）
- 确保 Docker 服务正在运行

## 快速开始

### 1. 启动所有服务

```powershell
docker-compose up -d
```

### 2. 查看服务状态

```powershell
docker-compose ps
```

### 3. 查看服务日志

```powershell
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f kafka
docker-compose logs -f mysql
docker-compose logs -f redis
```

### 4. 停止所有服务

```powershell
docker-compose down
```

### 5. 停止并删除数据卷（清理数据）

```powershell
docker-compose down -v
```

## 服务说明

### Kafka
- **端口**: 9092
- **容器名**: ddl-kafka
- **Topic**: ddl-reminder（会自动创建）

### ZooKeeper
- **端口**: 2181
- **容器名**: ddl-zookeeper
- **说明**: Kafka 的依赖服务

### MySQL
- **端口**: 3306
- **容器名**: ddl-mysql
- **数据库名**: ddl_manager
- **root 密码**: 123456
- **用户名**: ddl_user
- **用户密码**: ddl_password

### Redis
- **端口**: 6379
- **容器名**: ddl-redis
- **说明**: 数据持久化到卷 redis-data

## 验证服务

### 验证 Kafka

```powershell
# 进入 Kafka 容器
docker exec -it ddl-kafka bash

# 查看所有 topic
kafka-topics --bootstrap-server localhost:9092 --list

# 创建测试 topic（如果需要）
kafka-topics --bootstrap-server localhost:9092 --create --topic test-topic --partitions 1 --replication-factor 1

# 查看 topic 详情
kafka-topics --bootstrap-server localhost:9092 --describe --topic ddl-reminder
```

### 验证 MySQL

```powershell
# 进入 MySQL 容器
docker exec -it ddl-mysql bash

# 连接 MySQL
mysql -u root -p123456

# 或者从外部连接
mysql -h localhost -P 3306 -u root -p123456
```

### 验证 Redis

```powershell
# 进入 Redis 容器
docker exec -it ddl-redis redis-cli

# 测试连接
ping
# 应该返回 PONG
```

## 常见问题

### 1. 端口冲突

如果端口已被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "9093:9092"  # 将本地端口改为 9093
```

同时需要修改 `application.yml` 中的配置。

### 2. Kafka 连接失败

确保：
- ZooKeeper 已正常启动（检查健康状态）
- Kafka 容器健康检查通过
- 端口 9092 未被占用

### 3. 数据持久化

所有数据都保存在 Docker 卷中：
- MySQL 数据: `mysql-data` 卷
- Redis 数据: `redis-data` 卷

即使删除容器，数据也会保留。要完全清理数据，使用：

```powershell
docker-compose down -v
```

### 4. 查看容器资源使用

```powershell
docker stats
```

## 开发环境配置

项目配置文件 `src/main/resources/application.yml` 已配置为连接本地服务：

- Kafka: `localhost:9092`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

这些配置与 Docker Compose 中的端口映射匹配，无需修改即可使用。

## 生产环境建议

在生产环境中，建议：

1. 修改默认密码（MySQL root 密码等）
2. 使用环境变量文件（`.env`）管理敏感信息
3. 配置适当的资源限制（CPU、内存）
4. 使用外部数据卷或挂载点
5. 配置日志轮转和监控





