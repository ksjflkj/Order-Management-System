package org.example.ordermanagement.controller.merchant;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.service.ReviewService;
import org.example.ordermanagement.vo.ReviewVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商家端评价看板 - 支持店主针对顾客留下的评价做出解释和回复。
 */
@RestController
@RequestMapping("/api/merchant/review")
@RequiredArgsConstructor
public class MerchantReviewController {

    private final ReviewService reviewService;

    /** 商家查看自己的评价（通过merchantId） */
    @GetMapping("/list/{merchantId}")
    public Result<List<ReviewVO>> listReviews(@PathVariable Long merchantId) {
        return Result.success(reviewService.listMerchantReviews(merchantId));
    }

    /** 商家回复评价 */
    @PostMapping("/reply/{reviewId}")
    public Result<String> reply(Authentication authentication,
                                @PathVariable Long reviewId,
                                @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        reviewService.merchantReply(authentication.getName(), reviewId, reply);
        return Result.successMessage("回复成功");
    }
}
