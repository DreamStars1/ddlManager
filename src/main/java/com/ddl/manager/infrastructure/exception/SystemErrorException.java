package com.ddl.manager.infrastructure.exception;

import lombok.Getter;

/**
 * 系统异常
 * 用于处理不可预期的系统级错误
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Getter
public class SystemErrorException extends RuntimeException {

    /** 错误码 */
    private final int code;

    public SystemErrorException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM.getCode();
    }

    public SystemErrorException(String message, Throwable cause) {
        super(message, cause);
        this.code =  ErrorCode.SYSTEM.getCode();
    }

    public SystemErrorException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
