package com.ddl.manager.infrastructure.exception;

import lombok.Data;
import lombok.Getter;

/**
 *
 * <p>Description: 错误码</p>
 *
 * @author zhenghaipei
 * @since 2025/12/14
 */
@Getter
public enum ErrorCode {
    BUSINESS(50000,"business Error"),
    SYSTEM(80000,"System Error");
    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
