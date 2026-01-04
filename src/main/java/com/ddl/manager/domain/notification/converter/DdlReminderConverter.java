package com.ddl.manager.domain.notification.converter;

import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.shared.enums.TaskPriority;
import com.ddl.manager.shared.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DDL 提醒消息转换器
 * 用于在 TaskEntity 和 DdlReminderMessage 之间进行转换
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Component
public class DdlReminderConverter {

    /**
     * 将 TaskEntity 和 UserEntity 转换为 DdlReminderMessage
     *
     * @param task 任务实体
     * @param user 用户实体
     * @return DDL 提醒消息
     */
    public DdlReminderMessage toReminderMessage(TaskEntity task, UserEntity user) {
        if (task == null) {
            throw new IllegalArgumentException("任务实体不能为空");
        }
        if (user == null) {
            throw new IllegalArgumentException("用户实体不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Long hoursUntilDeadline = calculateHoursUntilDeadline(task.getDeadline(), now);

        return DdlReminderMessage.builder()
                .taskId(task.getId())
                .taskUuid(task.getUuid())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .taskTitle(task.getTitle())
                .taskDescription(task.getDescription())
                .deadline(task.getDeadline())
                .priority(convertPriorityToString(task.getPriority()))
                .status(convertStatusToString(task.getStatus()))
                .reminderTime(now)
                .hoursUntilDeadline(hoursUntilDeadline)
                .build();
    }

    /**
     * 将 TaskEntity 转换为 DdlReminderMessage（不包含用户信息）
     * 适用于用户信息已通过其他方式获取的场景
     *
     * @param task 任务实体
     * @param userId 用户ID
     * @param userEmail 用户邮箱
     * @return DDL 提醒消息
     */
    public DdlReminderMessage toReminderMessage(TaskEntity task, Long userId, String userEmail) {
        if (task == null) {
            throw new IllegalArgumentException("任务实体不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Long hoursUntilDeadline = calculateHoursUntilDeadline(task.getDeadline(), now);

        return DdlReminderMessage.builder()
                .taskId(task.getId())
                .taskUuid(task.getUuid())
                .userId(userId)
                .userEmail(userEmail)
                .taskTitle(task.getTitle())
                .taskDescription(task.getDescription())
                .deadline(task.getDeadline())
                .priority(convertPriorityToString(task.getPriority()))
                .status(convertStatusToString(task.getStatus()))
                .reminderTime(now)
                .hoursUntilDeadline(hoursUntilDeadline)
                .build();
    }

    /**
     * 计算距离截止时间的小时数
     *
     * @param deadline 截止时间
     * @param currentTime 当前时间
     * @return 小时数，如果已过期返回负数
     */
    public Long calculateHoursUntilDeadline(LocalDateTime deadline, LocalDateTime currentTime) {
        if (deadline == null || currentTime == null) {
            return null;
        }

        Duration duration = Duration.between(currentTime, deadline);
        return duration.toHours();
    }

    /**
     * 计算距离截止时间的小时数（使用当前时间）
     *
     * @param deadline 截止时间
     * @return 小时数，如果已过期返回负数
     */
    public Long calculateHoursUntilDeadline(LocalDateTime deadline) {
        return calculateHoursUntilDeadline(deadline, LocalDateTime.now());
    }

    /**
     * 将 TaskPriority 枚举转换为字符串
     *
     * @param priority 任务优先级枚举
     * @return 优先级字符串
     */
    public String convertPriorityToString(TaskPriority priority) {
        if (priority == null) {
            return null;
        }
        return priority.name();
    }

    /**
     * 将 TaskStatus 枚举转换为字符串
     *
     * @param status 任务状态枚举
     * @return 状态字符串
     */
    public String convertStatusToString(TaskStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    /**
     * 将字符串转换为 TaskPriority 枚举
     *
     * @param priority 优先级字符串
     * @return TaskPriority 枚举
     */
    public TaskPriority convertStringToPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return null;
        }
        try {
            return TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将字符串转换为 TaskStatus 枚举
     *
     * @param status 状态字符串
     * @return TaskStatus 枚举
     */
    public TaskStatus convertStringToStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}




