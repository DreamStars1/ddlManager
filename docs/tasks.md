# 实现计划

## 📊 当前得分评估（基于任务要求.md）

**说明**：评分规则为递进关系，取最高分，不累加。总分100分。

### 前端实现要求（20分）
1. **前端开发**（递进关系，取最高分）
   - ✅ 1.2 使用前端模版模式前端（Thymeleaf）：**8分**
   - ❌ 1.3 前后端分离模式（Vue）：未实现
   - **得分：8分**

2. **前端展示**（递进关系，取最高分）
   - ✅ 2.2 多层界面逻辑跳转：**7分**
   - ❌ 2.3 有图片展示：未实现（需要添加，可得8分）
   - ✅ 2.4 有CSS样式：**10分**
   - **得分：10分**（取最高分）

**前端小计：8 + 10 = 18分/20分**（如有图片展示可得8+8=16分或8+10=18分）

### 安全管理（20分）
1. **用户登录**（递进关系，取最高分）
   - ✅ 1.2 使用密码加密（BCrypt）：**7分**
   - ✅ 1.3 基于数据库的用户登录：**8分**
   - ❌ 1.4 基于Redis的用户登录：未实现
   - **得分：8分**

2. **权限控制**（递进关系，取最高分）
   - ✅ 2.2 基于角色的权限控制：**8分**
   - ✅ 2.3 基于方法的权限控制（@PreAuthorize）：**9分**
   - ✅ 2.4 权限持久化存储在数据库：**10分**
   - **得分：10分**

**安全管理小计：8 + 10 = 18分**

### 基本功能要求（30分）
1. **接口统计**（递进关系，取最高分）
   - ✅ 1.4 使用AOP+Redis+前端展示：**10分**
   - **得分：10分**

2. **数据持久化**（递进关系，取最高分）
   - ✅ 2.1 Spring Data JPA：**8分**
   - ✅ 2.2 知晓如何切换持久层框架（MyBatis适配器）：**10分**
   - **得分：10分**

3. **Kafka消息**：**10分**
   - ✅ 使用Kafka对消息进行生产和消费：**10分**

4. **Redis缓存使用**：**10分**
   - ⚠️ 部分实现：接口统计使用了Redis String类型
   - ❌ 未使用set/zset/list等数据类型解决特定场景问题
   - **得分：约0-3分**（需要改进，使用set/zset/list可得10分）

**基本功能小计：10 + 10 + 10 + 3 = 33分，但满分30分，实际得分约25-28分**

### 部署要求（15分）
- ✅ 1. 本地部署：**8分**
- ❌ 2. Dockerfile部署：未实现（需要创建Dockerfile）
- ✅ 3. docker-compose：**15分**（递进关系，取最高分）
- ❌ 4. K8s部署（附加分+5）：未实现
- **得分：15分**

### 文档要求（15分）
- ⚠️ 需要确认是否有2000字SpringBoot学习总结文档
- **得分：待确认（0-15分）**

### 📈 当前预估总分
- **已实现部分**：18（前端）+ 18（安全）+ 25（基本功能）+ 15（部署）= **76分/100分**
- **待完成改进**：
  - 图片展示：前端展示部分可从10分提升到8分（但总分不变，仍为18分）
  - Redis缓存改进：使用set/zset/list等数据类型（+7分，基本功能可从25分提升到30分）
  - Dockerfile：已有docker-compose得15分，Dockerfile不影响得分
- **潜在最高分**：约**83分/100分**（不含文档和K8s附加分）
- **加上文档**：如果文档完整，最高可达**98分/100分**（含K8s附加分可达103分）

---

## 1. 项目初始化与基础设施

- [x] 1.1 创建Spring Boot项目结构
  - 创建Maven项目，配置pom.xml依赖（Spring Boot、JPA、MySQL、Redis、Kafka、Thymeleaf、Security）
  - 创建包结构：infrastructure、shared、domain
  - 配置application.yml（数据库、Redis、Kafka连接信息）
  - _Requirements: 10.3, 12.1_

- [x] 1.2 实现异常处理体系
  - 创建BusinessException业务异常类
  - 创建SystemErrorException系统异常类
  - 创建AjaxResult统一响应类
  - 实现GlobalExceptionHandler全局异常处理器
  - _Requirements: 10.1_

- [x] 1.3 创建基础实体类和枚举
  - 创建BaseEntity基类（id、uuid、createTime、updateTime）
  - 创建TaskStatus、TaskPriority、NotificationType、NotificationStatus枚举
  - _Requirements: 4.1_

## 2. 用户认证模块

- [x] 2.1 实现用户和角色实体
  - 创建UserEntity用户实体
  - 创建RoleEntity角色实体
  - 配置多对多关系映射
  - 创建UserRepository和RoleRepository
  - _Requirements: 1.1, 2.1_

- [x] 2.2 配置Spring Security
  - 创建SecurityConfig配置类
  - 配置BCrypt密码加密
  - 配置登录/登出处理
  - 配置角色权限（ROLE_USER、ROLE_ADMIN）
  - 实现CustomUserDetailsService
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 2.2, 2.3, 2.4_

- [x] 2.3 实现用户注册功能
  - 创建UserService用户服务
  - 实现注册逻辑（密码加密、默认角色分配）
  - 创建AuthController认证控制器
  - 创建注册页面模板
  - _Requirements: 1.1, 2.1_

- [x] 2.4 实现登录页面
  - 创建登录页面模板（login.html）
  - 配置登录成功/失败处理
  - _Requirements: 1.2, 1.3_

- [ ]* 2.5 编写用户认证单元测试
  - 测试用户注册逻辑
  - 测试密码加密验证
  - 测试角色分配
  - _Requirements: 1.1, 2.1_

## 3. DDL任务核心模块

- [x] 3.1 实现任务实体和仓库
  - 创建TaskEntity任务实体（包含category字段和progressLog字段）
  - 创建TaskRepository（含自定义查询方法）
  - _Requirements: 3.1, 4.2_

- [x] 3.2 实现任务服务层
  - 创建TaskService接口和实现
  - 实现任务CRUD操作
  - 实现任务数据隔离（仅返回当前用户任务）
  - 实现任务排序和筛选
  - 实现进度备注添加
  - 实现任务完成状态处理（自动记录完成时间）
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 3.3 实现任务控制器
  - 创建TaskController
  - 实现任务列表页（支持排序筛选）
  - 实现任务详情页
  - 实现任务创建/编辑页
  - 实现任务删除接口
  - 实现进度备注添加接口
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 4.2_

- [x] 3.4 创建任务相关页面模板
  - 创建任务列表页（task-list.html）
  - 创建任务详情页（task-detail.html）
  - 创建任务表单页（task-form.html）
  - _Requirements: 7.2, 7.3, 7.4, 7.5_

- [ ]* 3.5 编写任务模块单元测试
  - 测试任务CRUD操作
  - 测试任务数据隔离
  - 测试进度百分比范围校验
  - _Requirements: 3.1, 3.2, 3.5, 4.3_

## 4. 检查点 - 核心功能验证

- [ ] 4. Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.

## 5. 主页仪表盘

- [x] 5.1 实现主页控制器和服务
  - 创建DashboardController
  - 实现即将截止任务查询（未来N天）
  - 实现任务统计数据
  - _Requirements: 7.1_

- [x] 5.2 创建主页模板
  - 创建主页模板（dashboard.html）
  - 展示即将截止看板
  - 展示优先级和进度可视化
  - 实现倒计时显示
  - _Requirements: 7.1, 7.2, 7.3, 7.5_

## 6. 管理员功能模块（简化版）

- [ ] 6.1 实现用户列表查询接口
  - 扩展UserService
  - 实现用户列表查询（仅查询功能）
  - 创建AdminController
  - _Requirements: 8.1_

- [ ]* 6.2 编写管理员模块单元测试
  - 测试用户列表查询
  - _Requirements: 8.1_

## 7. Redis缓存模块

- [x] 7.1 配置Redis
  - 创建RedisConfig配置类
  - 配置RedisTemplate序列化
  - _Requirements: 11.1_

- [ ] 7.2 实现缓存服务
  - 创建CacheService缓存服务
  - 在TaskService中集成缓存（任务列表缓存）
  - 实现缓存失效策略（数据修改时清除缓存）
  - ⚠️ **待实现**：TaskService中未使用Redis缓存
  - ⚠️ **待实现**：使用Redis set/zset/list等数据类型解决特定场景问题
  - _Requirements: 11.1, 11.2_

## 8. 接口统计模块（AOP + Redis + 前端展示）

- [x] 8.1 创建接口统计注解和AOP切面
  - 创建@ApiStatistics自定义注解（标记需要统计的接口）
  - 创建ApiStatisticsAspect切面类
  - 使用@Around环绕通知拦截标记的接口
  - _Requirements: 9.1_

- [x] 8.2 实现Redis存储统计数据
  - 使用Redis String存储接口调用次数（使用opsForValue().increment）
  - 使用Redis原子操作实现计数递增
  - ⚠️ **注意**：当前使用String类型，未使用Hash（但功能已实现）
  - _Requirements: 9.1_

- [x] 8.3 实现统计服务层
  - 创建StatisticsService接口和实现
  - 实现获取所有接口统计数据
  - 实现获取Top N热门接口
  - 实现清空统计数据（管理员功能）
  - _Requirements: 9.2_

- [x] 8.4 实现统计控制器和前端页面
  - 创建StatisticsController
  - 创建统计页面（statistics.html）
  - 使用图表展示接口调用统计（Chart.js）
  - 展示接口调用排行榜
  - _Requirements: 9.2, 9.3_

- [ ]* 8.5 编写接口统计单元测试
  - 测试AOP切面拦截
  - 测试Redis计数功能
  - _Requirements: 9.1_

## 9. Kafka消息与邮件通知模块（简化版）

- [x] 9.1 配置Kafka
  - 创建KafkaConfig配置类（KafkaProducerConfig、KafkaConsumerConfig）
  - 配置生产者和消费者
  - 定义Topic（ddl-reminder）
  - _Requirements: 6.1_

- [x] 9.2 实现提醒消息生产者
  - 创建DdlReminderProducer
  - 实现DDL提醒消息发送
  - 创建定时任务扫描即将到期任务（DdlReminderScheduler）
  - _Requirements: 6.1, 6.3_

- [x] 9.3 实现邮件通知消费者（简化版）
  - 创建DdlReminderConsumer（Kafka监听器）
  - 创建EmailService邮件发送服务
  - 消费失败直接打印日志，不实现重试机制
  - _Requirements: 6.2, 6.4_

- [ ]* 9.4 编写通知模块单元测试
  - 测试提醒生成条件
  - 测试已完成任务不发送提醒
  - _Requirements: 6.1, 6.3_

## 10. 日志模块

- [x] 10.1 配置错误日志
  - 配置logback日志框架（logback-spring.xml）
  - 在GlobalExceptionHandler中记录错误日志
  - 关键操作直接用log.info打印（不使用AOP和AuditLogEntity）
  - _Requirements: 10.1, 10.2_

## 11. 检查点 - 完整功能验证

- [ ] 11. Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.

## 12. 容器化部署

- [ ] 12.1 创建Docker配置
  - ❌ **待创建**：Dockerfile（应用容器化）
  - ✅ 创建docker-compose.yml（MySQL、Redis、Kafka、ZooKeeper）
  - ✅ 配置环境变量（application-docker.yml）
  - _Requirements: 12.1, 12.2_

## 13. 最终检查点

- [ ] 13. Final Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.

---

## 📋 待完成任务清单

### 🔴 高优先级（影响得分）

1. **创建Dockerfile**（12分）
   - 用于应用容器化部署
   - 位置：项目根目录

2. **Redis缓存使用改进**（10分）
   - 在TaskService中集成Redis缓存（任务列表缓存）
   - 使用Redis set/zset/list等数据类型解决特定场景问题
   - 例如：使用ZSet实现任务按截止时间排序

3. **添加图片展示**（8分）
   - 在前端页面中添加图片展示
   - 例如：任务详情页、仪表盘等

4. **管理员功能**（可选，简化版）
   - 创建AdminController
   - 实现用户列表查询接口

### 🟡 中优先级（优化功能）

5. **单元测试补充**
   - 用户认证模块单元测试
   - 任务模块单元测试
   - 接口统计模块单元测试

6. **文档编写**
   - 2000字以内SpringBoot学习总结

### 🟢 低优先级（可选）

7. **前后端分离模式**（Vue）
   - 如需更高分，可考虑实现前后端分离

8. **基于Redis的用户登录**（Session存储）
   - 使用Redis存储用户Session

9. **K8s部署**（附加分+5分）
   - 如需额外加分，可考虑K8s部署
