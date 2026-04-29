package org.example.ordermanagement.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 客户端查看商家列表/分页时返回的数据VO。
 * 对比 Merchant，隐藏了 userId、businessLicense、status 等内部字段，
 * 防止向匿名用户暴露商家的敏感信息或不必要的后台状态。
 */
@Data
public class UserMerchantVO {
    private Long id;
    private String shopName;
    private String shopLogo;
    private String address;
    private String phone;
    private String description;
    private Integer isOpen;
    private BigDecimal rating;
}
