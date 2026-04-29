package org.example.ordermanagement.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> successMessage(String message) {
        return new Result<>(200, message, null);
    }

    /**
     * 客户端错误（参数/业务规则不满足），对应 HTTP 400。
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 服务端内部错误，对应 HTTP 500。
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}