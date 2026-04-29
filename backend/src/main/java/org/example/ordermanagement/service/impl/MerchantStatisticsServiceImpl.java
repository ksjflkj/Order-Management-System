package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.helper.MerchantHelper;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.entity.OrderItem;
import org.example.ordermanagement.entity.Orders;
import org.example.ordermanagement.mapper.OrderItemMapper;
import org.example.ordermanagement.mapper.OrdersMapper;
import org.example.ordermanagement.service.MerchantStatisticsService;
import org.example.ordermanagement.vo.MerchantStatisticsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantStatisticsServiceImpl implements MerchantStatisticsService {

    private final MerchantHelper merchantHelper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public MerchantStatisticsVO getStatistics(String username) {
        MerchantStatisticsVO vo = new MerchantStatisticsVO();
        vo.setDailySales(new ArrayList<>());
        vo.setHotDishes(new ArrayList<>());
        vo.setOrderConversionRate(BigDecimal.ZERO);
        vo.setRefundRate(BigDecimal.ZERO);

        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        if (merchantIds.isEmpty()) {
            return vo;
        }

        // Fetch all orders for this merchant
        List<Orders> allOrders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .in(Orders::getMerchantId, merchantIds)
        );

        int totalOrders = allOrders.size();
        if (totalOrders > 0) {
            long completedOrders = allOrders.stream()
                    .filter(o -> Arrays.asList("PAID", "ACCEPTED", "COMPLETED").contains(o.getStatus()))
                    .count();
            
            long cancelledOrders = allOrders.stream()
                    .filter(o -> "CANCELLED".equals(o.getStatus()))
                    .count();

            BigDecimal conversionRate = BigDecimal.valueOf(completedOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            
            BigDecimal refundRate = BigDecimal.valueOf(cancelledOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

            vo.setOrderConversionRate(conversionRate);
            vo.setRefundRate(refundRate);
        }

        // Calculate daily sales for the past 7 days
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        Map<String, MerchantStatisticsVO.DailySalesVO> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            MerchantStatisticsVO.DailySalesVO dailyVO = new MerchantStatisticsVO.DailySalesVO();
            dailyVO.setDate(date.format(formatter));
            dailyVO.setTotalAmount(BigDecimal.ZERO);
            dailyVO.setOrderCount(0);
            dailyMap.put(date.toString(), dailyVO); // key format used: YYYY-MM-DD
        }

        List<Orders> validOrders = allOrders.stream()
                .filter(o -> Arrays.asList("PAID", "ACCEPTED", "COMPLETED").contains(o.getStatus()))
                .collect(Collectors.toList());

        for (Orders order : validOrders) {
            if (order.getCreateTime() != null) {
                LocalDate orderDate = order.getCreateTime().toLocalDate();
                if (!orderDate.isBefore(today.minusDays(6))) {
                    MerchantStatisticsVO.DailySalesVO dailyVO = dailyMap.get(orderDate.toString());
                    if (dailyVO != null) {
                        dailyVO.setOrderCount(dailyVO.getOrderCount() + 1);
                        dailyVO.setTotalAmount(dailyVO.getTotalAmount().add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO));
                    }
                }
            }
        }

        vo.setDailySales(new ArrayList<>(dailyMap.values()));

        // Calculate hot dishes
        List<Long> validOrderIds = validOrders.stream().map(Orders::getId).collect(Collectors.toList());
        if (!validOrderIds.isEmpty()) {
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, validOrderIds)
            );

            Map<Long, MerchantStatisticsVO.HotDishVO> dishMap = new HashMap<>();
            for (OrderItem item : orderItems) {
                MerchantStatisticsVO.HotDishVO dishVO = dishMap.getOrDefault(item.getDishId(), new MerchantStatisticsVO.HotDishVO());
                dishVO.setDishId(item.getDishId());
                dishVO.setDishName(item.getDishName());
                dishVO.setSalesCount((dishVO.getSalesCount() == null ? 0 : dishVO.getSalesCount()) + item.getQuantity());
                dishMap.put(item.getDishId(), dishVO);
            }

            List<MerchantStatisticsVO.HotDishVO> hotDishes = dishMap.values().stream()
                    .sorted((a, b) -> Integer.compare(b.getSalesCount(), a.getSalesCount()))
                    .limit(5)
                    .collect(Collectors.toList());
            vo.setHotDishes(hotDishes);
        }

        return vo;
    }
}
