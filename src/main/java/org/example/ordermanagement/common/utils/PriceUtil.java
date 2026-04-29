package org.example.ordermanagement.common.utils;

import java.math.BigDecimal;

public class PriceUtil {

    /**
     * 根据菜品标签计算最终价格
     * @param originalPrice 原价
     * @param dishTags 菜品标签字符串
     * @return 最终价格
     */
    public static BigDecimal calculateFinalPrice(BigDecimal originalPrice, String dishTags) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal finalPrice = originalPrice;
        
        if (dishTags != null && !dishTags.isEmpty()) {
            if (dishTags.contains("大份")) {
                finalPrice = finalPrice.add(new BigDecimal("5"));
            } else if (dishTags.contains("小份")) {
                finalPrice = finalPrice.subtract(new BigDecimal("5"));
            }
        }
        
        // 保证价格不能小于0.01元
        if (finalPrice.compareTo(new BigDecimal("0.01")) < 0) {
            return new BigDecimal("0.01");
        }
        
        return finalPrice;
    }
}
