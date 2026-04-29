package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "请输入用户名或手机号")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
