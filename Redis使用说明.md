# DDL管理系统Redis使用说明

## 概述
本系统基于Redis实现分布式会话管理、用户状态统计和缓存功能，确保系统在高并发场景下的稳定性和可扩展性。

## 核心功能模块

### 1. 会话管理（Session Management）

#### 配置信息
- **存储策略**：Spring Session + Redis
- **命名空间**：`ddl:session`
- **过期时间**：30分钟（1800秒）
- **序列化方式**：JDK序列化（支持Spring Security对象）

```java
// 配置类：RedisConfig.java
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 1800,
    redisNamespace = "ddl:session"
)
```

#### 关键特性
- **分布式会话**：支持多实例部署下的会话共享
- **自动续期**：用户每次请求自动延长会话有效期
- **安全序列化**：使用JDK序列化处理Spring Security对象

### 2. 用户状态统计

#### 在线用户统计
```java
// 服务类：RedisSessionService.java
public long getOnlineUserCount() {
    String pattern = USER_SESSION_PREFIX + "*";
    Set<String> keys = redisTemplate.keys(pattern);
    return keys.size();
}
```

#### 今日登录统计
```java
// 记录用户今日登录
public void recordUserLoginToday(String username) {
    String todayKey = USER_LOGIN_TODAY_PREFIX + LocalDate.now();
    redisTemplate.opsForSet().add(todayKey, username);
}

// 获取今日登录数
public long getTodayLoginCount() {
    String todayKey = USER_LOGIN_TODAY_PREFIX + LocalDate.now();
    return redisTemplate.opsForSet().size(todayKey);
}
```

### 3. 权限缓存

#### 用户权限存储
```java
// 服务类：CustomUserDetailsService.java
private void storeUserPermissionsToRedis(SecurityUser securityUser) {
    Map<String, Object> permissionData = new HashMap<>();
    permissionData.put("userId", securityUser.getUserId());
    permissionData.put("username", securityUser.getUsername());
    permissionData.put("authorities", securityUser.getAuthorities());
    permissionData.put("enabled", securityUser.isEnabled());
    
    redisSessionService.storeUserPermissions(securityUser.getUsername(), permissionData);
}
```

### 4. Token黑名单管理

#### JWT Token失效机制
```java
// 服务类：UserServiceImpl.java
public void logout(HttpServletRequest request) throws Exception {
    String token = extractTokenFromRequest(request);
    long jwtExpiration = jwtTokenUtil.getExpiration();
    
    // 将token加入黑名单，过期时间与JWT一致
    redisTemplate.opsForValue().set("TOKEN_BLACKLIST:" + token, "1", 
        jwtExpiration, TimeUnit.MILLISECONDS);
}
```

## Redis键命名规范

| 键前缀 | 用途 | 示例 |
|--------|------|------|
| `ddl:session:*` | Spring Session会话 | `ddl:session:sessions:123456` |
| `ddl:user:sessions:*` | 用户会话数据 | `ddl:user:sessions:admin` |
| `ddl:user:permissions:*` | 用户权限缓存 | `ddl:user:permissions:admin` |
| `ddl:user:login:today:*` | 今日登录用户 | `ddl:user:login:today:2024-01-20` |
| `TOKEN_BLACKLIST:*` | JWT Token黑名单 | `TOKEN_BLACKLIST:eyJhbGci...` |
| `spring:session:*` | Spring Session管理 | `spring:session:sessions:123456` |

## 核心服务类说明

### RedisSessionService
主要功能方法：
- `storeUserSession()` - 存储用户会话
- `getUserSession()` - 获取用户会话
- `storeUserPermissions()` - 缓存用户权限
- `extendUserSession()` - 延长会话有效期
- `getOnlineUserCount()` - 统计在线用户
- `recordUserLoginToday()` - 记录今日登录

### 统计服务集成
```java
// 文档18：StatisticsServiceImpl.java
public void incrementCallCount(String apiName, String apiPath) {
    // Redis统计API调用次数
    String countKey = REDIS_KEY_PREFIX + "count:" + apiPath;
    redisTemplate.opsForValue().increment(countKey, 1);
}
```

## 会话生命周期管理

### 1. 登录时
- 创建Spring Session
- 存储用户权限到Redis
- 记录今日登录统计

### 2. 请求处理时
- SessionExtendInterceptor自动延长会话有效期
- 验证用户权限状态

### 3. 登出时
- 清理用户会话数据
- 将JWT Token加入黑名单
- 更新在线用户统计

### 4. 过期时
- SessionEventListener监听会话过期事件
- 自动清理相关业务数据

## 监控和管理接口

### 会话统计接口
```java
// AdminController.java
@GetMapping("/session/statistics")
public Result<SessionStatsDTO> getSessionStats() {
    SessionStatsDTO stats = userService.getSessionStatistics();
    return Result.success("获取会话统计数据成功", stats);
}
```

### 会话清理接口
```java
// SessionController.java
@PostMapping("/clean")
public AjaxResult cleanAllSessions() {
    Set<String> keys = redisTemplate.keys("spring:session:*");
    redisTemplate.delete(keys);
    return AjaxResult.ok("Session数据清理完成");
}
```

## 性能优化建议

### 1. 内存优化
- 合理设置会话过期时间（当前30分钟）
- 定期清理无效会话数据

### 2. 监控指标
- 在线用户数变化趋势
- 会话创建/销毁频率
- Redis内存使用情况

### 3. 高可用配置
- 建议配置Redis集群模式
- 设置合适的持久化策略
- 监控Redis连接状态

## 故障排查

### 常见问题处理
1. **会话丢失**：检查Redis连接和序列化配置
2. **权限不一致**：调用`reloadUserPermissions()`重新加载
3. **统计不准确**：验证Redis键命名和过期时间设置

### 健康检查
```java
// SessionController.java
@PostMapping("/check")
public AjaxResult checkRedisConnection() {
    redisTemplate.opsForValue().set("test:connection", "OK", 10);
    String result = (String) redisTemplate.opsForValue().get("test:connection");
    return "OK".equals(result) ? AjaxResult.ok("Redis连接正常") : AjaxResult.error("Redis连接异常");
}
```
