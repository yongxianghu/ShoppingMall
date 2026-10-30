package com.shop.common;

import lombok.Getter;

/**
 * 业务异常：Service 里发现问题就 throw，由全局异常处理器转成 Result。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
