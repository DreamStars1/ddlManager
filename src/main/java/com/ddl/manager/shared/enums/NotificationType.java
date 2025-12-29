package com.ddl.manager.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 * @author 郑海培
 * @since 2025-12-13
 */
@Getter
@AllArgsConstructor
public enum NotificationType {

    /** 邮件通知 */
    EMAIL("邮件"),

    /** 站内信 */
    SYSTEM("站内信");

    /** 类型标签 */
    private final String label;
}
