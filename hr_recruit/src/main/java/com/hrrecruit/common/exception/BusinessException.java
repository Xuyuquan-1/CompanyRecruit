package com.hrrecruit.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 当业务逻辑出错时抛出，由 GlobalExceptionHandler 统一捕获处理
 */
@Getter
public class BusinessException extends RuntimeException {

    /** HTTP状态码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
