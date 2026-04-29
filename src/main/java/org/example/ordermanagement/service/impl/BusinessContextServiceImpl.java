package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.dto.UserCouponQueryDTO;
import org.example.ordermanagement.dto.UserOrderQueryDTO;
import org.example.ordermanagement.service.*;
import org.example.ordermanagement.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 业务数据上下文构建实现
 * 预查用户/商家的业务数据，拼接成文本注入 System Prompt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessContextServiceImpl implements BusinessContextService {

    private final OrderService orderService;
    private final CouponService couponService;
    private final UserService userService;
    private final MerchantStatisticsService merchantStatisticsService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Override
    public String buildUserContext(String username) {
        StringBuilder sb = new StringBuilder();

        // ─── 1. 最近订单（最多 5 条） ───
        try {
            UserOrderQueryDTO orderQuery = new UserOrderQueryDTO();
            orderQuery.setCurrent(1);
            orderQuery.setSize(5);
            IPage<OrderVO> orderPage = orderService.pageMyOrders(username, orderQuery);

            if (orderPage != null && !orderPage.getRecords().isEmpty()) {
                sb.append("\n【用户最近订单（最新5条）】\n");
                for (OrderVO o : orderPage.getRecords()) {
                    String time = o.getCreateTime() != null ? o.getCreateTime().format(FMT) : "未知";
                    sb.append(String.format(
                            "  - 订单号：%s | 商家：%s | 金额：¥%s | 状态：%s | 下单时间：%s\n",
                            o.getOrderNo(),
                            o.getShopName(),
                            o.getTotalAmount(),
                            translateStatus(o.getStatus()),
                            time
                    ));
                }
            } else {
                sb.append("\n【用户最近订单】无订单记录\n");
            }
        } catch (Exception e) {
            log.warn("构建订单上下文失败, username={}", username, e);
            sb.append("\n【用户最近订单】查询失败\n");
        }

        // ─── 2. 可用优惠券（最多 5 张） ───
        try {
            Long userId = userService.getIdByUsername(username);
            if (userId != null) {
                UserCouponQueryDTO couponQuery = new UserCouponQueryDTO();
                couponQuery.setCurrent(1);
                couponQuery.setSize(5);
                couponQuery.setStatus("UNUSED"); // 只注入未使用的券，已使用/已过期券不应出现在"可用优惠券"上下文中
                IPage<CouponVO> couponPage = couponService.pageMyCoupons(userId, couponQuery);

                if (couponPage != null && !couponPage.getRecords().isEmpty()) {
                    sb.append("\n【用户可用优惠券（最多5张）】\n");
                    for (CouponVO c : couponPage.getRecords()) {
                        String desc;
                        if ("REDUCE".equals(c.getType())) {
                            // 满减券：满 X 减 Y
                            desc = "满" + c.getMinAmount() + "减" + c.getValue();
                        } else if ("CASH".equals(c.getType())) {
                            // 代金券：直减 Y 元（无门槛或有门槛）
                            desc = c.getMinAmount() != null && c.getMinAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                                    ? "满" + c.getMinAmount() + "可用，立减¥" + c.getValue()
                                    : "立减¥" + c.getValue();
                        } else if ("DISCOUNT".equals(c.getType())) {
                            // 折扣券：value 存的是 0~1 的折扣系数（如 0.9），需转为"9折"
                            BigDecimal discountDisplay = c.getValue()
                                    .multiply(new BigDecimal("10"))
                                    .stripTrailingZeros();
                            String discountStr = discountDisplay.toPlainString() + "折";
                            // 有最低消费门槛时，拼"满X元可用，打Y折"
                            desc = c.getMinAmount() != null && c.getMinAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                                    ? "满" + c.getMinAmount().stripTrailingZeros().toPlainString() + "元可用，打" + discountStr
                                    : "无门槛，打" + discountStr;
                        } else {
                            desc = c.getType() + " ¥" + c.getValue();
                        }
                        String expire = c.getEndTime() != null ? c.getEndTime().format(FMT) : "未知";
                        sb.append(String.format(
                                "  - %s（%s）| 有效期至：%s\n",
                                c.getName(), desc, expire
                        ));
                    }
                } else {
                    sb.append("\n【用户可用优惠券】暂无可用优惠券\n");
                }
            }
        } catch (Exception e) {
            log.warn("构建优惠券上下文失败, username={}", username, e);
            sb.append("\n【用户可用优惠券】查询失败\n");
        }

        return sb.toString();
    }

    @Override
    public String buildMerchantContext(String username) {
        StringBuilder sb = new StringBuilder();

        // ─── 1. 商家近期订单 ───
        try {
            List<MerchantOrderVO> orders = orderService.listMerchantOrders(username);
            if (orders != null && !orders.isEmpty()) {
                // 统计数据
                long totalOrders = orders.size();
                long pendingOrders = orders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
                long processingOrders = orders.stream().filter(o -> "ACCEPTED".equals(o.getStatus())).count();
                long completedOrders = orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
                BigDecimal totalRevenue = orders.stream()
                        .filter(o -> "COMPLETED".equals(o.getStatus()))
                        .map(MerchantOrderVO::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                sb.append("\n【商家订单概况】\n");
                sb.append(String.format("  总订单数：%d | 待处理：%d | 进行中：%d | 已完成：%d\n",
                        totalOrders, pendingOrders, processingOrders, completedOrders));
                sb.append(String.format("  已完成订单总收入：¥%s\n", totalRevenue));

                // 最近 5 条订单明细
                sb.append("\n【最近订单明细（最新5条）】\n");
                int showCount = Math.min(5, orders.size());
                for (int i = 0; i < showCount; i++) {
                    MerchantOrderVO o = orders.get(i);
                    String time = o.getCreateTime() != null ? o.getCreateTime().format(FMT) : "未知";
                    sb.append(String.format(
                            "  - 订单号：%s | 用户：%s | 金额：¥%s | 状态：%s | 时间：%s | 备注：%s\n",
                            o.getOrderNo(),
                            o.getUsername(),
                            o.getTotalAmount(),
                            translateStatus(o.getStatus()),
                            time,
                            o.getRemark() != null ? o.getRemark() : "无"
                    ));
                }
            } else {
                sb.append("\n【商家订单概况】暂无订单\n");
            }
        } catch (Exception e) {
            log.warn("构建商家订单上下文失败, username={}", username, e);
            sb.append("\n【商家订单概况】查询失败\n");
        }

        // ─── 2. 商家统计数据 ───
        try {
            MerchantStatisticsVO stats = merchantStatisticsService.getStatistics(username);
            if (stats != null) {
                sb.append("\n【经营数据】\n");
                if (stats.getOrderConversionRate() != null) {
                    sb.append(String.format("  订单转化率：%s%%\n", stats.getOrderConversionRate()));
                }
                if (stats.getRefundRate() != null) {
                    sb.append(String.format("  退款率：%s%%\n", stats.getRefundRate()));
                }
                // 热销菜品
                if (stats.getHotDishes() != null && !stats.getHotDishes().isEmpty()) {
                    sb.append("  热销菜品：");
                    for (int i = 0; i < Math.min(3, stats.getHotDishes().size()); i++) {
                        MerchantStatisticsVO.HotDishVO dish = stats.getHotDishes().get(i);
                        sb.append(String.format("%s(%d份)", dish.getDishName(), dish.getSalesCount()));
                        if (i < Math.min(3, stats.getHotDishes().size()) - 1) sb.append("、");
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("构建商家统计上下文失败, username={}", username, e);
        }

        return sb.toString();
    }

    @Override
    public String buildAdminContext() {
        StringBuilder sb = new StringBuilder();

        // ─── 1. 平台全局 Dashboard ───
        try {
            AdminDashboardVO dashboard = orderService.getAdminDashboard();
            if (dashboard != null) {
                sb.append("\n【平台全局数据概览】\n");
                sb.append(String.format("  注册用户数：%d\n", dashboard.getUserCount()));
                sb.append(String.format("  入驻商家数：%d\n", dashboard.getMerchantCount()));
                sb.append(String.format("  菜品总数：%d\n", dashboard.getDishCount()));
                sb.append(String.format("  订单总数：%d（已支付：%d）\n",
                        dashboard.getOrderCount(), dashboard.getPaidOrderCount()));
                sb.append(String.format("  平台累计营收：¥%s\n", dashboard.getTotalSalesAmount()));
            }
        } catch (Exception e) {
            log.warn("构建管理员 Dashboard 上下文失败", e);
            sb.append("\n【平台全局数据】查询失败\n");
        }

        // ─── 2. 最近全平台订单（最新5条） ───
        try {
            org.example.ordermanagement.dto.AdminOrderQueryDTO queryDTO =
                    new org.example.ordermanagement.dto.AdminOrderQueryDTO();
            queryDTO.setCurrent(1L);
            queryDTO.setSize(5L);
            IPage<AdminOrderVO> adminOrders = orderService.pageAdminOrders(queryDTO);

            if (adminOrders != null && !adminOrders.getRecords().isEmpty()) {
                sb.append("\n【全平台最近订单（最新5条）】\n");
                for (AdminOrderVO o : adminOrders.getRecords()) {
                    String time = o.getCreateTime() != null ? o.getCreateTime().format(FMT) : "未知";
                    sb.append(String.format(
                            "  - 订单号：%s | 用户：%s | 商家：%s | ¥%s | %s | %s\n",
                            o.getOrderNo(),
                            o.getUsername(),
                            o.getShopName(),
                            o.getTotalAmount(),
                            translateStatus(o.getStatus()),
                            time
                    ));
                }
            }
        } catch (Exception e) {
            log.warn("构建管理员订单上下文失败", e);
        }

        return sb.toString();
    }

    private String translateStatus(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "PENDING_PAYMENT" -> "待支付";
            case "PAID" -> "已支付（待接单）";
            case "ACCEPTED" -> "商家已接单";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
