package org.example.ordermanagement.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.OrderCreateDTO;
import org.example.ordermanagement.dto.RefundApplyDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.vo.OrderDetailVO;
import org.example.ordermanagement.vo.OrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单控制器 - 负责所有订单相关的核心生命周期管理。
 * 覆盖下单、支付、查看详情、取消、确认收货和售后退款等流程。
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单 - 点击“去结算”提交订单
     * 内部逻辑：校验购物车、扣减库存、计算优惠金额、清空相应购物车
     * @param authentication 当前用户
     * @param dto 包含选中的商家ID、使用的优惠券、地址、备注等信息
     */
    @PostMapping("/create")
    public Result<String> create(Authentication authentication, @RequestBody OrderCreateDTO dto) {
        orderService.createOrder(authentication.getName(), dto);
        return Result.successMessage("订单创建成功");
    }

    /**
     * 获取"我的订单"列表（支持分页及各种状态筛选：全部、待支付、进行中等）
     */
    @GetMapping("/my")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<OrderVO>> page(Authentication authentication, org.example.ordermanagement.dto.UserOrderQueryDTO dto) {
        return Result.success(orderService.pageMyOrders(authentication.getName(), dto));
    }

    /**
     * 用户查看单一笔订单的具体详情（含菜品列表、收货地址、金额明细等）
     */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(Authentication authentication, @PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(authentication.getName(), id));
    }

    /**
     * 取消还处于未付款状态的订单
     */
    @PostMapping("/{id}/cancel")
    public Result<String> cancel(Authentication authentication, @PathVariable Long id) {
        orderService.cancelOrder(authentication.getName(), id);
        return Result.successMessage("订单取消成功");
    }

    /**
     * 模拟订单支付（将状态由 PENDING_PAYMENT 更新为 PAID）
     */
    @PostMapping("/{id}/pay")
    public Result<String> pay(Authentication authentication, @PathVariable Long id) {
        orderService.payOrder(authentication.getName(), id);
        return Result.successMessage("支付成功");
    }

    /**
     * 用户确认收货，标识订单配送完结
     */
    @PostMapping("/{id}/confirm")
    public Result<String> confirm(Authentication authentication, @PathVariable Long id) {
        orderService.confirmReceived(authentication.getName(), id);
        return Result.successMessage("确认收货成功");
    }

    /**
     * 发起售后退款申请（支持填写具体退还原因）
     */
    @PostMapping("/{id}/refund")
    public Result<String> refund(Authentication authentication, @PathVariable Long id,
                                 @RequestBody(required = false) RefundApplyDTO dto) {
        String reason = (dto != null) ? dto.getReason() : null;
        orderService.applyRefund(authentication.getName(), id, reason);
        return Result.successMessage("退款申请已提交，请等待商家审核");
    }

    /**
     * 申请平台介入 —— 商家拒绝退款后，用户可上升至平台仲裁（REFUND_REJECTED → REFUND_ARBITRATING）
     */
    @PostMapping("/{id}/escalate")
    public Result<String> escalate(Authentication authentication, @PathVariable Long id) {
        orderService.escalateToArbitration(authentication.getName(), id);
        return Result.successMessage("已申请平台介入，请耐心等待平台处理");
    }
}