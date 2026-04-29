package org.example.ordermanagement.config;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 —— 消息由开发者主动抛出，可安全返回给前端。
     * HTTP 400 Bad Request：客户端触发的业务规则冲突。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<String>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(e.getMessage()));
    }

    /**
     * 参数校验异常（@Valid / @Validated）
     * HTTP 400 Bad Request：请求体字段不合法。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(message));
    }

    /**
     * 约束校验异常（@NotNull 等单参数校验）
     * HTTP 400 Bad Request：请求参数不合法。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<String>> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail("参数校验失败"));
    }

    /**
     * 未预料到的运行时异常 —— 仅记录日志，不暴露细节。
     * HTTP 500 Internal Server Error：服务端错误，前端/网关可据此重试。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e) {
        log.error("未处理的运行时异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙，请稍后重试"));
    }

    /**
     * 兜底异常 —— 仅记录日志，不暴露细节。
     * HTTP 500 Internal Server Error：服务端错误。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙，请稍后重试"));
    }
}