# 实现计划

## 1. 项目初始化与基础设施

- [ ] 1.1 创建Spring Boot项目结构
  - 创建Maven项目，配置pom.xml依赖（Spring Boot、JPA、MySQL、Redis、Kafka、Thymeleaf、Security）
  - 创建包结构：infrastructure、shared、domain
  - 配置application.yml（数据库、Redis、Kafka连接信息）
  - _Requirements: 10.3, 12.1_

- [ ] 1.2 实现异常处理体系
  - 创建BusinessException业务异常类
  - 创建SystemErrorException系统异常类
  - 创建AjaxResult统一响应类
  - 实现GlobalExceptionHandler全局异常处理器
  - _Requirements: 10.1_

- [ ] 1.3 创建基础实体类和枚举
  - 创建BaseEntity基类（id、uuid、createTime、updateTime）
  - 创建TaskStatus、TaskPriority、NotificationType、NotificationStatus枚举
  - _Requirements: 4.1_

## 2. 用户认证模块

- [ ] 2.1 实现用户和角色实体
  - 创建UserEntity用户实体
  - 创建RoleEntity角色实体
  - 配置多对多关系映射
  - 创建UserRepository和RoleRepository
  - _Requirements: 1.1, 2.1_

- [ ] 2.2 配置Spring Security
  - 创建SecurityConfig配置类
  - 配置BCrypt密码加密
  - 配置登录/登出处理
  - 配置角色权限（ROLE_USER、ROLE_ADMIN）
  - 实现CustomUserDetailsService
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 2.2, 2.3, 2.4_

- [ ] 2.3 实现用户注册功能
  - 创建UserService用户服务
  - 实现注册逻辑（密码加密、默认角色分配）
  - 创建AuthController认证控制器
  - 创建注册页面模板
  - _Requirements: 1.1, 2.1_

- [ ] 2.4 实现登录页面
  - 创建登录页面模板（login.html）
  - 配置登录成功/失败处理
  - _Requirements: 1.2, 1.3_

- [ ]* 2.5 编写用户认证单元测试
  - 测试用户注册逻辑
  - 测试密码加密验证
  - 测试角色分配
  - _Requirements: 1.1, 2.1_

## 3. DDL任务核心模块

- [ ] 3.1 实现任务实体和仓库
  - 创建TaskEntity任务实体（包含category字段和progressLog字段）
  - 创建TaskRepository（含自定义查询方法）
  - _Requirements: 3.1, 4.2_

- [ ] 3.2 实现任务服务层
  - 创建TaskService接口和实现
  - 实现任务CRUD操作
  - 实现任务数据隔离（仅返回当前用户任务）
  - 实现任务排序和筛选
  - 实现进度备注添加
  - 实现任务完成状态处理（自动记录完成时间）
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 3.3 实现任务控制器
  - 创建TaskController
  - 实现任务列表页（支持排序筛选）
  - 实现任务详情页
  - 实现任务创建/编辑页
  - 实现任务删除接口
  - 实现进度备注添加接口
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 4.2_

- [ ] 3.4 创建任务相关页面模板
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

- [ ] 5.1 实现主页控制器和服务
  - 创建DashboardController
  - 实现即将截止任务查询（未来N天）
  - 实现任务统计数据
  - _Requirements: 7.1_

- [ ] 5.2 创建主页模板
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

- [ ] 7.1 配置Redis
  - 创建RedisConfig配置类
  - 配置RedisTemplate序列化
  - _Requirements: 11.1_

- [ ] 7.2 实现缓存服务
  - 创建CacheService缓存服务
  - 在TaskService中集成缓存（任务列表缓存）
  - 实现缓存失效策略（数据修改时清除缓存）
  - _Requirements: 11.1, 11.2_

## 8. 接口统计模块（AOP + Redis + 前端展示）

- [ ] 8.1 创建接口统计注解和AOP切面
  - 创建@ApiStatistics自定义注解（标记需要统计的接口）
  - 创建ApiStatisticsAspect切面类
  - 使用@Around环绕通知拦截标记的接口
  - _Requirements: 9.1_

- [ ] 8.2 实现Redis存储统计数据
  - 使用Redis Hash存储接口调用次数（key: api:statistics, field: 接口路径）
  - 使用Redis原子操作HINCRBY实现计数递增
  - 支持按时间维度统计（可选：日/周/月）
  - _Requirements: 9.1_

- [ ] 8.3 实现统计服务层
  - 创建StatisticsService接口和实现
  - 实现获取所有接口统计数据
  - 实现获取Top N热门接口
  - 实现清空统计数据（管理员功能）
  - _Requirements: 9.2_

- [ ] 8.4 实现统计控制器和前端页面
  - 创建StatisticsController
  - 创建统计页面（admin-statistics.html）
  - 使用图表展示接口调用统计（柱状图/饼图）
  - 展示接口调用排行榜
  - _Requirements: 9.2, 9.3_

- [ ]* 8.5 编写接口统计单元测试
  - 测试AOP切面拦截
  - 测试Redis计数功能
  - _Requirements: 9.1_

## 9. Kafka消息与邮件通知模块（简化版）

- [ ] 9.1 配置Kafka
  - 创建KafkaConfig配置类
  - 配置生产者和消费者
  - 定义Topic（ddl-reminder）
  - _Requirements: 6.1_

- [ ] 9.2 实现提醒消息生产者
  - 创建TaskEventProducer
  - 实现DDL提醒消息发送
  - 创建定时任务扫描即将到期任务
  - _Requirements: 6.1, 6.3_

- [ ] 9.3 实现邮件通知消费者（简化版）
  - 创建NotificationConsumer（Kafka监听器）
  - 创建EmailService邮件发送服务
  - 消费失败直接打印日志，不实现重试机制
  - _Requirements: 6.2, 6.4_

- [ ]* 9.4 编写通知模块单元测试
  - 测试提醒生成条件
  - 测试已完成任务不发送提醒
  - _Requirements: 6.1, 6.3_

## 10. 日志模块

- [ ] 10.1 配置错误日志
  - 配置logback日志框架
  - 在GlobalExceptionHandler中记录错误日志
  - 关键操作直接用log.info打印（不使用AOP和AuditLogEntity）
  - _Requirements: 10.1, 10.2_

## 11. 检查点 - 完整功能验证

- [ ] 11. Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.

## 12. 容器化部署

- [ ] 12.1 创建Docker配置
  - 创建Dockerfile
  - 创建docker-compose.yml（MySQL、Redis、Kafka、应用）
  - 配置环境变量
  - _Requirements: 12.1, 12.2_

## 13. 最终检查点

- [ ] 13. Final Checkpoint - 确保所有测试通过
  - Ensure all tests pass, ask the user if questions arise.
