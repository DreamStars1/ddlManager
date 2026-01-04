# Docker 容器账号密码说明

## MySQL 数据库

### Root 用户（管理员）
- **用户名**: `root`
- **密码**: `123456`
- **端口**: `13306`（主机端口，容器内为 3306）

### 普通用户
- **用户名**: `ddl_user`
- **密码**: `ddl_password`
- **数据库**: `ddl_manager`

## 连接方式

### 从主机连接 MySQL

```powershell
# 使用 root 用户连接
mysql -h localhost -P 13306 -u root -p123456

# 或使用普通用户连接
mysql -h localhost -P 13306 -u ddl_user -pddl_password
```

### 从 Docker 容器内连接

```powershell
# 进入 MySQL 容器
docker exec -it ddl-mysql bash

# 连接 MySQL
mysql -u root -p123456
# 或
mysql -u ddl_user -pddl_password
```

### 使用 Docker 命令直接连接

```powershell
docker exec -it ddl-mysql mysql -u root -p123456

# 执行 SQL 命令
docker exec -it ddl-mysql mysql -u root -p123456 -e "SHOW DATABASES;"
```

## Redis

Redis 默认**无需密码**，可以直接连接：

```powershell
# 从主机连接
redis-cli -h localhost -p 6380

# 从容器内连接
docker exec -it ddl-redis redis-cli
```

## 应用配置

Spring Boot 应用配置（`application.yml`）使用：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:13306/ddl_manager?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 123456

  redis:
    host: localhost
    port: 6380
    # 无密码
```

## 安全建议

⚠️ **注意**: 当前配置使用简单密码，仅适用于开发环境。

### 生产环境建议：

1. **修改 MySQL root 密码**：
   ```sql
   ALTER USER 'root'@'%' IDENTIFIED BY '强密码';
   ```

2. **为 Redis 设置密码**：
   在 `docker-compose.yml` 中添加：
   ```yaml
   redis:
     command: redis-server --requirepass 你的密码
   ```

3. **使用环境变量文件**：
   创建 `.env` 文件存储敏感信息，不要将密码硬编码在配置文件中。

## 快速测试连接

### 测试 MySQL
```powershell
docker exec -it ddl-mysql mysql -u root -p123456 -e "SELECT 1;"
```

### 测试 Redis
```powershell
docker exec -it ddl-redis redis-cli ping
```

### 测试 Kafka
```powershell
docker exec -it ddl-kafka kafka-topics --bootstrap-server localhost:9092 --list
```





