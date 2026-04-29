package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.AdminOrderQueryDTO;
import org.example.ordermanagement.dto.OrderCreateDTO;
import org.example.ordermanagement.vo.*;

import java.util.List;

public interface OrderService {

    void createOrder(String username, OrderCreateDTO dto);

    com.baomidou.mybatisplus.core.metadata.IPage<OrderVO> pageMyOrders(String username, org.example.ordermanagement.dto.UserOrderQueryDTO dto);

    OrderDetailVO getOrderDetail(String username, Long orderId);

    void cancelOrder(String username, Long orderId);

    void payOrder(String username, Long orderId);

    // 用户：确认收货 (ACCEPTED → RECEIVED)
    void confirmReceived(String username, Long orderId);

    // 用户：申请退款 (ACCEPTED/RECEIVED → REFUND_PENDING)
    void applyRefund(String username, Long orderId, String reason);

    // 商家：同意退款 (REFUND_PENDING → REFUNDED)
    void agreeRefund(String username, Long orderId);

    // 商家：拒绝退款 (REFUND_PENDING → REFUND_REJECTED)
    void rejectRefund(String username, Long orderId, String rejectReason);

    // 用户：申请平台介入（REFUND_REJECTED → REFUND_ARBITRATING）
    void escalateToArbitration(String username, Long orderId);

    // 管理员：仲裁退款纠纷（REFUND_ARBITRATING → REFUNDED / ARBITRATION_REJECTED）
    void resolveArbitration(Long orderId, boolean approve, String note);

    List<MerchantOrderVO> listMerchantOrders(String username);

    MerchantOrderDetailVO getMerchantOrderDetail(String username, Long orderId);

    void acceptOrder(String username, Long orderId);

    void completeOrder(String username, Long orderId);

    com.baomidou.mybatisplus.core.metadata.IPage<AdminOrderVO> pageAdminOrders(AdminOrderQueryDTO dto);

    AdminOrderDetailVO getAdminOrderDetail(Long orderId);

    AdminDashboardVO getAdminDashboard();
}