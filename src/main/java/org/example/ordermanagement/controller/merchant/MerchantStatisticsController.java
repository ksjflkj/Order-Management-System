package org.example.ordermanagement.controller.merchant;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.service.MerchantStatisticsService;
import org.example.ordermanagement.vo.MerchantStatisticsVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家控制台数据面板 - 提供经营分析大屏所需要的各类图表及数字统计能力。
 */
@RestController
@RequestMapping("/api/merchant/statistics")
@RequiredArgsConstructor
public class MerchantStatisticsController {

    private final MerchantStatisticsService merchantStatisticsService;

    /**
     * 汇总过去特定时间区间内的订单量、营收流水总计及商品销量排行的数据
     */
    @GetMapping
    public Result<MerchantStatisticsVO> getStatistics(Authentication authentication) {
        return Result.success(merchantStatisticsService.getStatistics(authentication.getName()));
    }
}
