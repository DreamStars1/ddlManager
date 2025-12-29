package com.ddl.manager.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 * @author 郑海培
 * @since 2025-12-13
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {

    /** 待办 */
    TODO("待办", 1),

    /** 进行中 */
    IN_PROGRESS("进行中", 2),

    /** 已完成 */
    COMPLETED("已完成", 3),

    /** 已取消 */
    CANCELED("已取消", 4);

    /** 状态标签 */
    private final String label;

    /** 排序权重 */
    private final int order;

    /**
     * 是否为终态（不可再变更）
     * @return true-终态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELED;
    }

    /**
     * 是否需要发送提醒
     * @return true-需要提醒
     */
    public boolean needsReminder() {
        return this == TODO || this == IN_PROGRESS;
    }
}
