package org.example.ordermanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantStatisticsVO {
    private List<DailySalesVO> dailySales;
    private List<HotDishVO> hotDishes;
    private BigDecimal orderConversionRate; // 订单转化率 (%)
    private BigDecimal refundRate; // 退款率 (%)

    @Data
    public static class DailySalesVO {
        private String date;
        private BigDecimal totalAmount;
        private Integer orderCount;
    }

    @Data
    public static class HotDishVO {
        private Long dishId;
        private String dishName;
        private Integer salesCount;
    }
}
