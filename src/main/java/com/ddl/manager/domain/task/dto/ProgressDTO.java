package com.ddl.manager.domain.task.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 进度记录DTO
 * @author zhenghaipei
 * @since 2025-12-15
 */
@Data
public class ProgressDTO {

    /** 进度百分比 0-100 */
    @NotNull(message = "进度不能为空")
    @Min(value = 0, message = "进度不能小于0")
    @Max(value = 100, message = "进度不能大于100")
    private Integer progress;

    /** 进度备注 */
    private String progressLog;
}





