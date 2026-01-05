# Kafka 生产者和消费者使用说明

## 概述

已创建完整的 Kafka 生产者和消费者实现，用于 DDL 提醒消息的发送和消费。

## 已创建的文件

### 1. DTO 类
- **`DdlReminderMessage.java`** - DDL 提醒消息数据传输对象

### 2. 生产者
- **`DdlReminderProducer.java`** - DDL 提醒消息生产者服务

### 3. 消费者
- **`DdlReminderConsumer.java`** - DDL 提醒消息消费者（当前只打印信息，不发送邮件）

### 4. 测试控制器
- **`KafkaTestController.java`** - Kafka 测试 API 端点

## 功能特性

### 生产者功能

1. **异步发送消息**
   ```java
   ddlReminderProducer.sendReminder(message);
   ```
   - 异步发送，不阻塞
   - 带回调处理，记录发送结果

2. **同步发送消息**
   ```java
   boolean success = ddlReminderProducer.sendReminderSync(message);
   ```
   - 同步发送，等待结果
   - 返回是否发送成功

### 消费者功能

1. **自动消费消息**
   - 监听 `ddl-reminder` topic
   - 自动解析 JSON 消息
   - **当前只打印 DDL 信息，不发送邮件**

2. **格式化输出**
   - 美观的日志格式
   - 包含完整的任务信息
   - 显示用户信息

## 配置说明

### 启用 Kafka

在配置文件中添加 `kafka.enabled: true`：

```yaml
kafka:
  enabled: true  # 启用 Kafka
  consumer:
    servers: localhost:9092
    group-id: ddl-reminder-group
  producer:
    servers: localhost:9092
```

### Topic 配置

在 `application.yml` 中配置：

```yaml
ddl:
  reminder:
    topic: ddl-reminder  # Kafka topic 名称
```

## 使用示例

### 1. 在代码中使用生产者

```java
@Service
public class TaskReminderService {
    
    @Autowired
    private DdlReminderProducer ddlReminderProducer;
    
    public void sendReminder(TaskEntity task, UserEntity user) {
        DdlReminderMessage message = DdlReminderMessage.builder()
                .taskId(task.getId())
                .taskUuid(task.getUuid())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .taskTitle(task.getTitle())
                .taskDescription(task.getDescription())
                .deadline(task.getDeadline())
                .priority(task.getPriority().name())
                .status(task.getStatus().name())
                .reminderTime(LocalDateTime.now())
                .hoursUntilDeadline(calculateHoursUntilDeadline(task))
                .build();
        
        ddlReminderProducer.sendReminder(message);
    }
}
```

### 2. 通过 API 测试

#### 发送简单测试消息
```http
POST /api/kafka/test/send/simple
Authorization: Bearer <token>
```

#### 发送自定义测试消息
```http
POST /api/kafka/test/send
Content-Type: application/x-www-form-urlencoded

taskId=1&taskUuid=test-001&userId=1&userEmail=test@example.com&taskTitle=完成项目文档&deadline=2025-12-31T23:59:59
```

## 消息格式

### DdlReminderMessage 结构

```json
{
  "taskId": 1,
  "taskUuid": "uuid-001",
  "userId": 1,
  "userEmail": "user@example.com",
  "taskTitle": "完成项目文档",
  "taskDescription": "需要完成项目文档编写",
  "deadline": "2025-12-31T23:59:59",
  "priority": "HIGH",
  "status": "TODO",
  "reminderTime": "2025-12-30T23:59:59",
  "hoursUntilDeadline": 24
}
```

## 消费者日志输出示例

当消费者收到消息时，会输出如下格式的日志：

```
========== 收到 DDL 提醒消息 ==========
分区: 0, 偏移量: 123
┌─────────────────────────────────────────────────────────┐
│                    DDL 提醒信息                          │
├─────────────────────────────────────────────────────────┤
│ 任务ID:        1                                        │
│ 任务UUID:      uuid-001                                │
│ 任务标题:      完成项目文档                              │
│ 任务描述:      需要完成项目文档编写                      │
│ 截止时间:      2025-12-31 23:59:59                      │
│ 任务优先级:    HIGH                                     │
│ 任务状态:      TODO                                    │
│ 提醒时间:      2025-12-30 23:59:59                      │
│ 距离截止:      24 小时                                  │
├─────────────────────────────────────────────────────────┤
│ 用户信息:                                                │
│   用户ID:      1                                        │
│   用户邮箱:    user@example.com                         │
└─────────────────────────────────────────────────────────┘
⚠️  DDL 提醒: 任务 [完成项目文档] 将在 24 小时后到期！
========== DDL 提醒消息处理完成 ==========
```

## 验证 Kafka 连接

### 1. 检查 Kafka 服务

```powershell
# 查看 Kafka 容器状态
docker-compose ps kafka

# 查看 Kafka 日志
docker-compose logs -f kafka
```

### 2. 查看 Topic

```powershell
# 进入 Kafka 容器
docker exec -it ddl-kafka bash

# 查看所有 topic
kafka-topics --bootstrap-server localhost:9092 --list

# 查看 ddl-reminder topic 详情
kafka-topics --bootstrap-server localhost:9092 --describe --topic ddl-reminder
```

### 3. 手动发送测试消息

```powershell
# 进入 Kafka 容器
docker exec -it ddl-kafka bash

# 发送测试消息
kafka-console-producer --bootstrap-server localhost:9092 --topic ddl-reminder

# 然后输入 JSON 消息：
{"taskId":1,"taskUuid":"test-001","userId":1,"userEmail":"test@example.com","taskTitle":"测试任务","deadline":"2025-12-31T23:59:59","priority":"HIGH","status":"TODO","reminderTime":"2025-12-30T23:59:59","hoursUntilDeadline":24}
```

### 4. 手动消费消息

```powershell
# 进入 Kafka 容器
docker exec -it ddl-kafka bash

# 消费消息
kafka-console-consumer --bootstrap-server localhost:9092 --topic ddl-reminder --from-beginning
```

## 注意事项

1. **Kafka 必须启用**
   - 确保配置文件中 `kafka.enabled: true`
   - 确保 Kafka 服务正在运行

2. **Topic 自动创建**
   - Kafka 配置了 `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"`
   - Topic 会在首次发送消息时自动创建

3. **消费者组**
   - 默认消费者组：`ddl-reminder-group`
   - 同一消费者组的多个实例会负载均衡消费消息

4. **消息序列化**
   - 使用 FastJSON2 进行 JSON 序列化/反序列化
   - 消息格式为 JSON 字符串

5. **当前消费者行为**
   - ✅ 接收消息
   - ✅ 解析消息
   - ✅ 打印 DDL 信息
   - ❌ **暂不发送邮件**（后续可扩展）

## 后续扩展

### 在消费者中添加邮件发送

修改 `DdlReminderConsumer.java`：

```java
@Autowired
private EmailService emailService;

@KafkaListener(...)
public void consumeReminder(...) {
    // ... 解析消息 ...
    
    // 发送邮件
    try {
        String subject = "DDL 提醒: " + reminderMessage.getTaskTitle();
        String html = buildReminderHtml(reminderMessage);
        emailService.sendHtmlEmail(
            reminderMessage.getUserEmail(), 
            subject, 
            html
        );
        log.info("DDL提醒邮件发送成功: {}", reminderMessage.getUserEmail());
    } catch (Exception e) {
        log.error("DDL提醒邮件发送失败", e);
    }
}
```

## 故障排查

### 问题1：消息发送失败

**检查：**
1. Kafka 服务是否运行：`docker-compose ps kafka`
2. Kafka 配置是否正确：`kafka.enabled: true`
3. Topic 是否存在：`kafka-topics --list`

### 问题2：消费者未收到消息

**检查：**
1. 消费者是否启动（查看应用日志）
2. 消费者组配置是否正确
3. Topic 名称是否匹配
4. 消息是否已发送（查看生产者日志）

### 问题3：配置未生效

**检查：**
1. 配置文件是否正确加载
2. `@ConditionalOnProperty` 条件是否满足
3. 应用是否重启

## 测试步骤

1. **启动 Kafka 服务**
   ```powershell
   docker-compose up -d kafka
   ```

2. **启动应用**
   ```powershell
   mvn spring-boot:run
   ```

3. **发送测试消息**
   ```http
   POST /api/kafka/test/send/simple
   ```

4. **查看消费者日志**
   - 应该能看到格式化的 DDL 提醒信息输出




