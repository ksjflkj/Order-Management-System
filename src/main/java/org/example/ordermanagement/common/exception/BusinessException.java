package org.example.ordermanagement.common.exception;

/**
 * 业务异常 —— 消息可以安全地返回给前端用户。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
