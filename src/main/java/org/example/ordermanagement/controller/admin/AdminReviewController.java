package org.example.ordermanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.service.ReviewService;
import org.example.ordermanagement.vo.ReviewVO;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

/**
 * 平台全网评价监管中心 - 治理恶意的刷单评价、脏话或违规客诉内容。
 */
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    /** 管理员分页查询评价 */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO>> page(org.example.ordermanagement.dto.AdminReviewQueryDTO dto) {
        return Result.success(reviewService.pageAdminReviews(dto));
    }

    /** 管理员屏蔽/取消屏蔽评价 */
    @PostMapping("/block/{reviewId}")
    public Result<String> block(@PathVariable Long reviewId, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        reviewService.blockReview(reviewId, status);
        return Result.successMessage(status == 1 ? "已取消屏蔽" : "已屏蔽");
    }
}
