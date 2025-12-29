package com.ddl.manager.infrastructure.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于处理可预期的业务逻辑错误
 * @author developer
 * @since 2025-12-13
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BUSINESS.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
