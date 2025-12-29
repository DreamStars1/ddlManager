package com.ddl.manager.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知状态枚举
 * @author 郑海培
 * @since 2025-12-13
 */
@Getter
@AllArgsConstructor
public enum NotificationStatus {

    /** 待发送 */
    PENDING("待发送"),

    /** 已发送 */
    SENT("已发送"),

    /** 发送失败 */
    FAILED("发送失败");

    /** 状态标签 */
    private final String label;
}
