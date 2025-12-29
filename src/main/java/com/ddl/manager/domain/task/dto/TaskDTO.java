package com.ddl.manager.domain.task.dto;

import com.ddl.manager.shared.enums.TaskPriority;
import com.ddl.manager.shared.enums.TaskStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 任务数据传输对象
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Data
public class TaskDTO {

    /** 任务UUID（用于更新时） */
    private String uuid;

    /** 任务标题 */
    @NotBlank(message = "任务标题不能为空")
    private String title;

    /** 任务描述 */
    private String description;

    /** 任务分类 */
    private String category;

    /** 截止时间 */
    @NotNull(message = "截止时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;

    /** 任务状态 */
    private TaskStatus status;

    /** 任务优先级 */
    private TaskPriority priority;

    /** 进度百分比 0-100 */
    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    private Integer progress;

    /** 进度备注 */
    private String progressLog;
}

