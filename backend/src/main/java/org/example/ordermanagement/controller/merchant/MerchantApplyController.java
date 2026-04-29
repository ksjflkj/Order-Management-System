package org.example.ordermanagement.controller.merchant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.MerchantApplyDTO;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.service.MerchantService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家入驻及店铺资格控制器 - 负责普通用户申请成为商家、以及查询自己的店铺资格。
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantApplyController {

    private final MerchantService merchantService;

    /**
     * 提交商家入驻申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@Valid @RequestBody MerchantApplyDTO dto,
                                Authentication authentication) {
        String username = authentication.getName();
        merchantService.applyMerchant(username, dto);
        return Result.successMessage("申请提交成功，请等待管理员审核");
    }

    /**
     * 获取当前用户审核通过的店铺列表
     * 普通用户（USER）也可调用，用于判断是否拥有店铺、进入商家后台
     */
    @GetMapping("/my")
    public Result<List<Merchant>> myMerchants(Authentication authentication) {
        return Result.success(merchantService.getMyMerchants(authentication.getName()));
    }

    /**
     * 获取当前用户全部申请记录（包括待审核、已拒绝）
     * 用于在"商家入驻"页面展示申请状态
     */
    @GetMapping("/my/all")
    public Result<List<Merchant>> allMyMerchants(Authentication authentication) {
        return Result.success(merchantService.getAllMyMerchants(authentication.getName()));
    }
}