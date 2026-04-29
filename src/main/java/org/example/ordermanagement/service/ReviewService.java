package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.ReviewSaveDTO;
import org.example.ordermanagement.vo.ReviewVO;

import java.util.List;

public interface ReviewService {

    void saveReview(String username, ReviewSaveDTO dto);

    com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO> pageMyReviews(String username, org.example.ordermanagement.dto.UserReviewQueryDTO dto);

    List<ReviewVO> listMerchantReviews(Long merchantId);

    /** 商家回复评价 */
    void merchantReply(String username, Long reviewId, String reply);

    /** 用户追评 */
    void userFollowUp(String username, Long reviewId, String content);

    /** 管理员屏蔽/取消屏蔽评价 */
    void blockReview(Long reviewId, Integer status);

    /** 管理员分页查询评价 */
    com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO> pageAdminReviews(org.example.ordermanagement.dto.AdminReviewQueryDTO dto);
}