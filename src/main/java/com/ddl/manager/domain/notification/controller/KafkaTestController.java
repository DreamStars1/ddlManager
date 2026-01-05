package com.ddl.manager.domain.notification.controller;

import com.ddl.manager.domain.notification.converter.DdlReminderConverter;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import com.ddl.manager.domain.notification.service.DdlReminderProducer;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.shared.dto.AjaxResult;
import com.ddl.manager.shared.enums.TaskPriority;
import com.ddl.manager.shared.enums.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Kafka 测试控制器
 * 用于测试 Kafka 消息发送和消费
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@RestController
@RequestMapping("/api/kafka/test")
public class KafkaTestController {

    @Autowired(required = false)
    private DdlReminderProducer ddlReminderProducer;

    @Autowired
    private DdlReminderConverter reminderConverter;

    /**
     * 发送测试 DDL 提醒消息
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult sendTestMessage(
            @RequestParam(required = false, defaultValue = "1") Long taskId,
            @RequestParam(required = false, defaultValue = "test-uuid-001") String taskUuid,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false, defaultValue = "test@example.com") String userEmail,
            @RequestParam(required = false, defaultValue = "测试任务") String taskTitle,
            @RequestParam(required = false) String taskDescription,
            @RequestParam(required = false) String deadline) {
        
        try {
            if (ddlReminderProducer == null) {
                return AjaxResult.error("Kafka 生产者未配置，请检查 kafka.enabled 配置");
            }

            // 构建测试任务实体
            LocalDateTime deadlineTime = deadline != null ? 
                    LocalDateTime.parse(deadline) : 
                    LocalDateTime.now().plusHours(24);
            
            TaskEntity task = TaskEntity.builder()
                    .userId(userId)
                    .title(taskTitle)
                    .description(taskDescription != null ? taskDescription : "这是一个测试任务的描述")
                    .deadline(deadlineTime)
                    .priority(TaskPriority.HIGH)
                    .status(TaskStatus.TODO)
                    .build();
            
            // 设置BaseEntity的字段（id和uuid不在Builder中）
            if (taskId != null) {
                task.setId(taskId);
            }
            if (taskUuid != null) {
                task.setUuid(taskUuid);
            }

            // 使用转换器转换为提醒消息
            DdlReminderMessage message = reminderConverter.toReminderMessage(
                    task, userId, userEmail);

            // 发送消息
            ddlReminderProducer.sendReminder(message);
            
            log.info("测试 DDL 提醒消息已发送: {}", taskTitle);
            return AjaxResult.ok("DDL 提醒消息发送成功，请查看消费者日志");
            
        } catch (Exception e) {
            log.error("发送测试消息失败", e);
            return AjaxResult.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送简单测试消息（使用默认值）
     */
    @PostMapping("/send/simple")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult sendSimpleTestMessage() {
        return sendTestMessage(
                1L, "test-uuid-001", 1L, "test@example.com",
                "简单测试任务", null, null);
    }
}

