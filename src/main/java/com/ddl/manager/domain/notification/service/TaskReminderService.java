package com.ddl.manager.domain.notification.service;

import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.UserRepository;
import com.ddl.manager.domain.notification.converter.DdlReminderConverter;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import com.ddl.manager.domain.task.model.TaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 任务提醒服务
 * 负责将任务转换为提醒消息并发送
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Service
public class TaskReminderService {

    @Autowired
    private DdlReminderConverter reminderConverter;

    @Autowired
    private DdlReminderProducer reminderProducer;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送任务提醒消息
     *
     * @param task 任务实体
     * @return 是否发送成功
     */
    public boolean sendTaskReminder(TaskEntity task) {
        if (task == null) {
            log.warn("任务实体为空，跳过提醒发送");
            return false;
        }

        try {
            // 获取用户信息
            Optional<UserEntity> userOpt = userRepository.findById(task.getUserId());
            if (!userOpt.isPresent()) {
                log.warn("用户不存在，跳过提醒发送 - 任务ID: {}, 用户ID: {}", task.getId(), task.getUserId());
                return false;
            }

            UserEntity user = userOpt.get();

            // 检查用户是否启用邮件提醒
            if (user.getEmailNotificationEnabled() == null || !user.getEmailNotificationEnabled()) {
                log.debug("用户未启用邮件提醒，跳过发送 - 用户ID: {}", user.getId());
                return false;
            }

            // 转换为提醒消息
            DdlReminderMessage message = reminderConverter.toReminderMessage(task, user);

            // 发送消息到 Kafka
            reminderProducer.sendReminder(message);

            log.info("任务提醒消息已发送 - 任务ID: {}, 用户: {}", task.getId(), user.getEmail());
            return true;

        } catch (Exception e) {
            log.error("发送任务提醒失败 - 任务ID: {}", task.getId(), e);
            return false;
        }
    }

    /**
     * 发送任务提醒消息（使用用户邮箱）
     *
     * @param task 任务实体
     * @param userEmail 用户邮箱
     * @return 是否发送成功
     */
    public boolean sendTaskReminder(TaskEntity task, String userEmail) {
        if (task == null) {
            log.warn("任务实体为空，跳过提醒发送");
            return false;
        }
        if (userEmail == null || userEmail.trim().isEmpty()) {
            log.warn("用户邮箱为空，跳过提醒发送 - 任务ID: {}", task.getId());
            return false;
        }

        try {
            // 转换为提醒消息
            DdlReminderMessage message = reminderConverter.toReminderMessage(
                    task, task.getUserId(), userEmail);

            // 发送消息到 Kafka
            reminderProducer.sendReminder(message);

            log.info("任务提醒消息已发送 - 任务ID: {}, 用户邮箱: {}", task.getId(), userEmail);
            return true;

        } catch (Exception e) {
            log.error("发送任务提醒失败 - 任务ID: {}", task.getId(), e);
            return false;
        }
    }

    /**
     * 批量发送任务提醒
     *
     * @param tasks 任务列表
     * @return 成功发送的数量
     */
    public int sendBatchReminders(java.util.List<TaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (TaskEntity task : tasks) {
            if (sendTaskReminder(task)) {
                successCount++;
            }
        }

        log.info("批量发送任务提醒完成 - 总数: {}, 成功: {}", tasks.size(), successCount);
        return successCount;
    }
}




