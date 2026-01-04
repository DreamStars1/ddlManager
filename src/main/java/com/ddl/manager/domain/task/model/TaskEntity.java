package com.ddl.manager.domain.task.model;

import com.ddl.manager.shared.enums.TaskPriority;
import com.ddl.manager.shared.enums.TaskStatus;
import com.ddl.manager.shared.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * DDL任务实体
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Entity
@Table(name = "ddl_task", indexes = {
        @Index(name = "idx_task_uuid", columnList = "uuid"),
        @Index(name = "idx_task_user_id", columnList = "userId"),
        @Index(name = "idx_task_deadline", columnList = "deadline"),
        @Index(name = "idx_task_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity extends BaseEntity {

    /** 所属用户ID */
    @Column(nullable = false)
    private Long userId;

    /** 任务分类（固定值：学习、生活、工作等） */
    @Column(length = 50)
    private String category;

    /** 任务标题 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 任务描述 */
    @Column(length = 2000)
    private String description;

    /** 截止时间 */
    @Column(nullable = false)
    private LocalDateTime deadline;

    /** 任务状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /** 任务优先级 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /** 进度百分比 0-100 */
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    @Builder.Default
    private Integer progress = 0;

    /** 完成时间，状态变为COMPLETED时自动设置 */
    private LocalDateTime completedTime;

    /** 是否已发送提醒 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean reminderSent = false;

    /** 进度备注（文本形式） */
    @Column(columnDefinition = "TEXT")
    private String progressLog;

    /**
     * 标记任务完成
     */
    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.completedTime = LocalDateTime.now();
        this.progress = 100;
    }

    /**
     * 判断是否需要发送提醒
     * @param reminderHours 提醒阈值（小时）
     * @return true-需要提醒
     */
    public boolean needsReminder(int reminderHours) {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELED) {
            return false;
        }
        if (this.reminderSent) {
            return false;
        }
        LocalDateTime reminderTime = this.deadline.minusHours(reminderHours);
        return LocalDateTime.now().isAfter(reminderTime);
    }
}
