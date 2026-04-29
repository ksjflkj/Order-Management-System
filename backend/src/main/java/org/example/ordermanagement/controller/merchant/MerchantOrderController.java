package org.example.ordermanagement.controller.merchant;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.RefundRejectDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.vo.MerchantOrderDetailVO;
import org.example.ordermanagement.vo.MerchantOrderVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家端订单操作后台 - 提供给商家接单、催单处理、完成订单以及售后退款审核的功能。
 */
@RestController
@RequestMapping("/api/merchant/order")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;

    /**
     * 获取商家所在店铺的订单列表（工作台）
     */
    @GetMapping("/list")
    public Result<List<MerchantOrderVO>> list(Authentication authentication) {
        return Result.success(orderService.listMerchantOrders(authentication.getName()));
    }

    /**
     * 商家查看某笔订单的具体明细及配送地址
     */
    @GetMapping("/{id}")
    public Result<MerchantOrderDetailVO> detail(Authentication authentication, @PathVariable Long id) {
        return Result.success(orderService.getMerchantOrderDetail(authentication.getName(), id));
    }

    /**
     * 商家手动接单，订单进入制作中状态
     */
    @PostMapping("/{id}/accept")
    public Result<String> accept(Authentication authentication, @PathVariable Long id) {
        orderService.acceptOrder(authentication.getName(), id);
        return Result.successMessage("接单成功");
    }

    /**
     * 配送人员妥投后或取餐完成，流转状态归到完成。可累积销量。
     */
    @PostMapping("/{id}/complete")
    public Result<String> complete(Authentication authentication, @PathVariable Long id) {
        orderService.completeOrder(authentication.getName(), id);
        return Result.successMessage("订单完成");
    }

    /**
     * 商家同意用户发起的售后退款（涉及金额和库存的回滚）
     */
    @PostMapping("/{id}/agree-refund")
    public Result<String> agreeRefund(Authentication authentication, @PathVariable Long id) {
        orderService.agreeRefund(authentication.getName(), id);
        return Result.successMessage("已同意退款，库存和优惠券已自动归还");
    }

    /**
     * 商家拒绝用户的退款申请并填写拒绝原因
     */
    @PostMapping("/{id}/reject-refund")
    public Result<String> rejectRefund(Authentication authentication, @PathVariable Long id,
                                       @RequestBody(required = false) RefundRejectDTO dto) {
        String rejectReason = (dto != null) ? dto.getRejectReason() : null;
        orderService.rejectRefund(authentication.getName(), id, rejectReason);
        return Result.successMessage("已拒绝退款申请");
    }
}