package org.example.ordermanagement.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.ReviewSaveDTO;
import org.example.ordermanagement.service.ReviewService;
import org.example.ordermanagement.vo.ReviewVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单评价控制器 - 负责管理用户用餐后的反馈及对商家的公开评分。
 * 支持：首次评价、列表展示、追加评论、商家页所有评论拉取。
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 针对已确认收货的订单发布首评
     * 内部会一并计算商家的最新综合评分，并设置订单状态为已评价
     */
    @PostMapping("/save")
    public Result<String> save(Authentication authentication, @Valid @RequestBody ReviewSaveDTO dto) {
        reviewService.saveReview(authentication.getName(), dto);
        return Result.successMessage("评价成功");
    }

    /**
     * 主题面板：获取“我给出的”所有评价记录（带图文及追评信息）
     */
    @GetMapping("/my")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO>> my(Authentication authentication, org.example.ordermanagement.dto.UserReviewQueryDTO dto) {
        return Result.success(reviewService.pageMyReviews(authentication.getName(), dto));
    }

    /**
     * 商家店铺页：所有人均可查看的公开评价墙（仅拉取针对指定该店铺的内容）
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<ReviewVO>> getMerchantReviews(@PathVariable Long merchantId) {
        return Result.success(reviewService.listMerchantReviews(merchantId));
    }

    /** 用户追评 */
    @PostMapping("/follow-up/{reviewId}")
    public Result<String> followUp(Authentication authentication,
                                   @PathVariable Long reviewId,
                                   @RequestBody Map<String, String> body) {
        String content = body.get("content");
        reviewService.userFollowUp(authentication.getName(), reviewId, content);
        return Result.successMessage("追评成功");
    }
}