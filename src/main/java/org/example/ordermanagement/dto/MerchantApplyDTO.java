package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplyDTO {

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    private String shopLogo;

    @NotBlank(message = "店铺地址不能为空")
    private String address;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String description;

    @NotBlank(message = "营业执照不能为空")
    private String businessLicense;
}