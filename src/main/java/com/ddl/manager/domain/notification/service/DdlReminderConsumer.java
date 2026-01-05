package com.ddl.manager.domain.notification.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ddl.manager.shared.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * DDL 提醒消息消费者
 * 消费 Kafka 消息并发送邮件提醒
 * 
 * 限流策略：每秒最多发送 2 封邮件（即 1 分钟最多 120 封邮件）
 * 
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class DdlReminderConsumer {

    /** 限流器：每秒只允许通过 2 个请求（即 1 分钟最多发 120 封邮件） */
    private final RateLimiter rateLimiter = RateLimiter.create(2.0);

    @Autowired
    private DdlEmailContentBuilder emailContentBuilder;

    @Autowired(required = false)
    private EmailService emailService;

    /**
     * 消费 DDL 提醒消息并发送邮件
     * 使用手动ACK模式，确保消息处理成功后才确认
     * 
     * @param message 消息内容（JSON字符串）
     * @param ack 手动确认对象
     */
    @KafkaListener(
            topics = "${ddl.reminder.topic:ddl-reminder}",
            groupId = "${kafka.consumer.group-id:ddl-reminder-group}",
            containerFactory = "factoryAck"
    )
    public void onMessage(@Payload String message, Acknowledgment ack) {
        // 1. 获取令牌。如果发太快，这里会阻塞等待
        rateLimiter.acquire();
        
        DdlReminderMessage reminderMessage = null;
        
        try {
            log.info("========== 收到 DDL 提醒消息 ==========");
            
            // 2. 解析消息
            reminderMessage = JSON.parseObject(message, DdlReminderMessage.class);
            
            if (reminderMessage == null) {
                log.error("解析消息失败：消息内容为空");
                ack.acknowledge(); // 即使失败也确认，避免重复消费
                return;
            }
            
            log.info("任务ID: {}, 任务标题: {}, 用户邮箱: {}", 
                    reminderMessage.getTaskId(), 
                    reminderMessage.getTaskTitle(),
                    reminderMessage.getUserEmail());
            
            // 3. 验证必要字段
            if (reminderMessage.getUserEmail() == null || reminderMessage.getUserEmail().trim().isEmpty()) {
                log.error("用户邮箱为空，跳过邮件发送 - 任务ID: {}", reminderMessage.getTaskId());
                ack.acknowledge();
                return;
            }
            
            // 4. 构建邮件内容
            String emailSubject = emailContentBuilder.buildSubject(reminderMessage);
            String emailHtml = emailContentBuilder.buildReminderHtml(reminderMessage);
            
            log.debug("邮件主题: {}", emailSubject);
            log.debug("邮件内容已构建（长度: {} 字符）", emailHtml != null ? emailHtml.length() : 0);
            
            // 5. 发送邮件
            if (emailService != null) {
                emailService.sendHtmlEmail(
                        reminderMessage.getUserEmail(),
                        emailSubject,
                        emailHtml
                );
                log.info("✅ DDL 提醒邮件发送成功 - 任务: {}, 收件人: {}", 
                        reminderMessage.getTaskTitle(), 
                        reminderMessage.getUserEmail());
            } else {
                log.warn("⚠️  邮件服务未配置，跳过邮件发送 - 任务: {}", reminderMessage.getTaskTitle());
            }
            
            // 6. 打印 DDL 信息（用于日志记录）
            printDdlInfo(reminderMessage);
            
            // 7. 确认消息已处理
            ack.acknowledge();
            
            log.info("========== DDL 提醒消息处理完成 ==========");
            
        } catch (JSONException e) {
            log.error("❌ 解析 DDL 提醒消息失败 - 消息内容: {}", message, e);
            // JSON 解析失败，确认消息避免重复消费
            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ 处理 DDL 提醒消息异常 - 任务: {}", 
                    reminderMessage != null ? reminderMessage.getTaskTitle() : "未知", e);
            // 发送失败，不确认消息，让 Kafka 重新投递（根据业务需求决定）
            // 如果希望失败后不重试，可以取消下面的注释
            // ack.acknowledge();
            // 当前策略：失败后不确认，让 Kafka 重新投递（最多重试几次后进入死信队列）
        }
    }

    /**
     * 打印 DDL 信息
     *
     * @param message DDL 提醒消息
     */
    private void printDdlInfo(DdlReminderMessage message) {
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│                    DDL 提醒信息                          │");
        log.info("├─────────────────────────────────────────────────────────┤");
        log.info("│ 任务ID:        {}", formatValue(String.valueOf(message.getTaskId())));
        log.info("│ 任务UUID:      {}", formatValue(message.getTaskUuid()));
        log.info("│ 任务标题:      {}", formatValue(message.getTaskTitle()));
        log.info("│ 任务描述:      {}", formatValue(message.getTaskDescription()));
        log.info("│ 截止时间:      {}", formatDateTime(message.getDeadline()));
        log.info("│ 任务优先级:    {}", formatValue(message.getPriority()));
        log.info("│ 任务状态:      {}", formatValue(message.getStatus()));
        log.info("│ 提醒时间:      {}", formatDateTime(message.getReminderTime()));
        log.info("│ 距离截止:      {} 小时", formatValue(
                message.getHoursUntilDeadline() != null ? 
                String.valueOf(message.getHoursUntilDeadline()) : "未知"));
        log.info("├─────────────────────────────────────────────────────────┤");
        log.info("│ 用户信息:                                                │");
        log.info("│   用户ID:      {}", formatValue(String.valueOf(message.getUserId())));
        log.info("│   用户邮箱:    {}", formatValue(message.getUserEmail()));
        log.info("└─────────────────────────────────────────────────────────┘");
        
        // 额外打印关键信息
        log.warn("⚠️  DDL 提醒: 任务 [{}] 将在 {} 小时后到期！", 
                message.getTaskTitle(),
                message.getHoursUntilDeadline() != null ? 
                message.getHoursUntilDeadline() : "未知");
    }

    /**
     * 格式化值（处理null）
     */
    private String formatValue(String value) {
        return value != null ? value : "未设置";
    }

    /**
     * 格式化日期时间（使用工具类）
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return DateTimeUtils.format(dateTime);
    }
}

