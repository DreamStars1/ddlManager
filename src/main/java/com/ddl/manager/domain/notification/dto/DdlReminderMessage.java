package com.ddl.manager.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DDL 提醒消息DTO
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdlReminderMessage implements Serializable {


    /** 任务ID */
    private Long taskId;

    /** 任务UUID */
    private String taskUuid;

    /** 用户ID */
    private Long userId;

    /** 用户邮箱 */
    private String userEmail;

    /** 任务标题 */
    private String taskTitle;

    /** 任务描述 */
    private String taskDescription;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 任务优先级 */
    private String priority;

    /** 任务状态 */
    private String status;

    /** 提醒时间 */
    private LocalDateTime reminderTime;

    /** 距离截止时间的小时数 */
    private Long hoursUntilDeadline;
}

