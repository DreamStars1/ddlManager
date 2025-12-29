package com.ddl.manager.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务优先级枚举
 * @author 郑海培
 * @since 2025-12-13
 */
@Getter
@AllArgsConstructor
public enum TaskPriority {

    /** 低优先级 */
    LOW("低", 1, "#52c41a"),

    /** 中优先级 */
    MEDIUM("中", 2, "#1890ff"),

    /** 高优先级 */
    HIGH("高", 3, "#faad14"),

    /** 紧急 */
    URGENT("紧急", 4, "#f5222d");

    /** 优先级标签 */
    private final String label;

    /** 权重，用于排序 */
    private final int weight;

    /** 前端展示颜色 */
    private final String color;
}
