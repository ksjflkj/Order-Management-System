package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String phone;
    private Integer status;  // null=全部 0=正常 1=禁用
    private Long current = 1L;
    private Long size = 10L;
}
