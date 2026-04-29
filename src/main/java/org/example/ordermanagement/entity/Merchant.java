package org.example.ordermanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String shopName;
    private String shopLogo;
    private String address;
    private String phone;
    private String description;
    private String businessLicense;
    private Integer status;
    private Integer isOpen;   // 营业开关：1=营业中 0=已打烊（仅 status=1 的店铺有意义）
    private BigDecimal rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}