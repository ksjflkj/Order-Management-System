package org.example.ordermanagement.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.MerchantAuditDTO;
import org.example.ordermanagement.dto.MerchantQueryDTO;
import org.example.ordermanagement.dto.MerchantStatusDTO;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.service.MerchantService;
import org.springframework.web.bind.annotation.*;

/**
 * 平台全量商家资格审计及处罚控制器 - 处理新店资质验证以及对违规违法的店铺进行封禁下线。
 */
@RestController
@RequestMapping("/api/admin/merchant")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final MerchantService merchantService;

    /**
     * 查询所有入驻的商家资料库（支持多条件筛选组合）
     */
    @GetMapping("/page")
    public Result<IPage<Merchant>> page(MerchantQueryDTO queryDTO) {
        return Result.success(merchantService.pageMerchants(queryDTO));
    }

    /**
     * 审核商家提交过来的开店资料，批准或退回
     */
    @PostMapping("/audit")
    public Result<String> audit(@Valid @RequestBody MerchantAuditDTO dto) {
        merchantService.auditMerchant(dto);
        return Result.successMessage("审核成功");
    }

    /**
     * 封紧/强制下架已入驻的不良店铺（通过调整status字段实现）
     */
    @PostMapping("/status")
    public Result<String> updateStatus(@Valid @RequestBody MerchantStatusDTO dto) {
        merchantService.updateMerchantStatus(dto);
        return Result.successMessage("状态更新成功");
    }
}
