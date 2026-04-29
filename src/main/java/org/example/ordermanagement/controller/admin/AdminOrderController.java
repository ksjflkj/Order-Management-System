package org.example.ordermanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.AdminOrderQueryDTO;
import org.example.ordermanagement.dto.ArbitrationDTO;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.vo.AdminOrderDetailVO;
import org.example.ordermanagement.vo.AdminOrderVO;
import org.springframework.web.bind.annotation.*;



/**
 * 平台全局订单监控面板 - 供最高权限的管理员检索平台运行期间产生的所有流水订单。
 */
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 跨商家分页检索平台订单总表（可依据店铺、用户、状态等维度检索）
     */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<AdminOrderVO>> page(AdminOrderQueryDTO dto) {
        return Result.success(orderService.pageAdminOrders(dto));
    }

    /**
     * 查看某笔订单底层的抽佣、金额归属情况及明细快照
     */
    @GetMapping("/{id}")
    public Result<AdminOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getAdminOrderDetail(id));
    }

    /**
     * 平台仲裁裁决 —— 对退款纠纷作出最终裁定。
     * approve=true  → 支持退款：归还库存/优惠券，状态流转至 REFUNDED
     * approve=false → 驳回申诉：状态流转至 ARBITRATION_REJECTED（终态）
     */
    @PostMapping("/{id}/arbitration")
    public Result<String> arbitration(@PathVariable Long id, @RequestBody ArbitrationDTO dto) {
        orderService.resolveArbitration(id, dto.isApprove(), dto.getNote());
        String msg = dto.isApprove() ? "已支持用户退款，款项将原路退回" : "已驳回退款申诉，订单纠纷终结";
        return Result.successMessage(msg);
    }
}

