package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.MerchantHelper;
import org.example.ordermanagement.common.helper.UserHelper;
import org.example.ordermanagement.dto.ReviewSaveDTO;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.entity.Orders;
import org.example.ordermanagement.entity.Review;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.example.ordermanagement.mapper.OrdersMapper;
import org.example.ordermanagement.mapper.ReviewMapper;
import org.example.ordermanagement.mapper.UserMapper;
import org.example.ordermanagement.service.ReviewService;
import org.example.ordermanagement.vo.ReviewVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrdersMapper ordersMapper;
    private final UserHelper userHelper;
    private final MerchantHelper merchantHelper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReview(String username, ReviewSaveDTO dto) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权评价该订单");
        }

        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("只有已完成订单才可以评价");
        }

        Review existing = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>().eq(Review::getOrderId, dto.getOrderId())
        );
        if (existing != null) {
            throw new BusinessException("该订单已评价，不能重复评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setUserId(user.getId());
        review.setMerchantId(order.getMerchantId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus(1);
        reviewMapper.insert(review);

        updateMerchantRating(order.getMerchantId());
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO> pageMyReviews(String username, org.example.ordermanagement.dto.UserReviewQueryDTO dto) {
        User user = userHelper.getByUsername(username);

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, user.getId())
                .orderByDesc(Review::getCreateTime);

        if (org.springframework.util.StringUtils.hasText(dto.getShopName())) {
            List<Merchant> merchants = merchantMapper.selectList(new LambdaQueryWrapper<Merchant>().like(Merchant::getShopName, dto.getShopName()));
            List<Long> mIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());
            if (mIds.isEmpty()) {
                wrapper.in(Review::getMerchantId, -1L);
            } else {
                wrapper.in(Review::getMerchantId, mIds);
            }
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        reviewMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReviewVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<ReviewVO> voList = page.getRecords().stream().map(review -> {
            Merchant merchant = merchantMapper.selectById(review.getMerchantId());
            String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";

            return toVO(review, shopName, user.getNickname(), user.getAvatar());
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ReviewVO> listMerchantReviews(Long merchantId) {
        List<Review> reviewList = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getMerchantId, merchantId)
                        .eq(Review::getStatus, 1)
                        .orderByDesc(Review::getCreateTime)
        );

        return reviewList.stream().map(review -> {
            Merchant merchant = merchantMapper.selectById(review.getMerchantId());
            User user = userMapper.selectById(review.getUserId());
            String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";
            String nicknameValue = user != null ? user.getNickname() : "用户已注销";
            String avatarValue = user != null ? user.getAvatar() : null;

            return toVO(review, shopName, nicknameValue, avatarValue);
        }).collect(Collectors.toList());
    }

    @Override
    public void merchantReply(String username, Long reviewId, String reply) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        // 校验该评价的商家属于当前用户
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        boolean isOwner = merchants.stream().anyMatch(m -> m.getId().equals(review.getMerchantId()));
        if (!isOwner) {
            throw new BusinessException("无权回复该评价");
        }
        review.setMerchantReply(reply);
        review.setMerchantReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    public void userFollowUp(String username, Long reviewId, String content) {
        User user = userHelper.getByUsername(username);
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(user.getId())) {
            throw new BusinessException("无权追评该评价");
        }
        if (review.getFollowUpContent() != null) {
            throw new BusinessException("已经追评过，不可重复追评");
        }
        review.setFollowUpContent(content);
        review.setFollowUpTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    public void blockReview(Long reviewId, Integer status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<ReviewVO> pageAdminReviews(org.example.ordermanagement.dto.AdminReviewQueryDTO dto) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(dto.getShopName())) {
            List<Merchant> merchants = merchantMapper.selectList(new LambdaQueryWrapper<Merchant>().like(Merchant::getShopName, dto.getShopName()));
            List<Long> mIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());
            if (mIds.isEmpty()) {
                wrapper.in(Review::getMerchantId, -1L);
            } else {
                wrapper.in(Review::getMerchantId, mIds);
            }
        }
        if (org.springframework.util.StringUtils.hasText(dto.getOrderNo())) {
            List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>().like(Orders::getOrderNo, dto.getOrderNo()));
            List<Long> oIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
            if (oIds.isEmpty()) {
                wrapper.in(Review::getOrderId, -1L);
            } else {
                wrapper.in(Review::getOrderId, oIds);
            }
        }

        wrapper.orderByDesc(Review::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Review> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        reviewMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ReviewVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<ReviewVO> voList = page.getRecords().stream().map(review -> {
            Merchant merchant = merchantMapper.selectById(review.getMerchantId());
            User user = userMapper.selectById(review.getUserId());
            String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";
            String nicknameValue = user != null ? user.getNickname() : "用户已注销";
            String avatarValue = user != null ? user.getAvatar() : null;
            return toVO(review, shopName, nicknameValue, avatarValue);
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    private ReviewVO toVO(Review review, String shopName, String nickname, String avatar) {
        return new ReviewVO(
                review.getId(),
                review.getOrderId(),
                review.getMerchantId(),
                shopName,
                nickname,
                avatar,
                review.getRating(),
                review.getContent(),
                review.getMerchantReply(),
                review.getMerchantReplyTime(),
                review.getFollowUpContent(),
                review.getFollowUpTime(),
                review.getStatus(),
                review.getCreateTime()
        );
    }

    private void updateMerchantRating(Long merchantId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>().eq(Review::getMerchantId, merchantId)
                        .eq(Review::getStatus, 1)
        );

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            return;
        }

        if (reviews == null || reviews.isEmpty()) {
            merchant.setRating(BigDecimal.ZERO);
            merchantMapper.updateById(merchant);
            return;
        }

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        merchant.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        merchantMapper.updateById(merchant);
    }
}