package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.dto.CouponSaveDTO;
import org.example.ordermanagement.entity.Coupon;
import org.example.ordermanagement.entity.CouponUser;
import org.example.ordermanagement.mapper.CouponMapper;
import org.example.ordermanagement.mapper.CouponUserMapper;
import org.example.ordermanagement.service.CouponService;
import org.example.ordermanagement.vo.CouponVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponUserMapper couponUserMapper;

    @Override
    public void createCoupon(CouponSaveDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setMinAmount(dto.getMinAmount() != null ? dto.getMinAmount() : new java.math.BigDecimal("0"));
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setTotalCount(dto.getTotalCount());
        coupon.setRemainCount(dto.getTotalCount());
        coupon.setEachLimit(dto.getEachLimit() != null ? dto.getEachLimit() : 1);
        coupon.setStatus("ACTIVE");
        save(coupon);
    }

    @Override
    public void updateCoupon(Long id, CouponSaveDTO dto) {
        Coupon coupon = getById(id);
        if (coupon == null) throw new BusinessException("优惠券不存在");

        coupon.setName(dto.getName());
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setMinAmount(dto.getMinAmount());
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        if (dto.getTotalCount() != null) {
            int diff = dto.getTotalCount() - coupon.getTotalCount();
            coupon.setTotalCount(dto.getTotalCount());
            coupon.setRemainCount(coupon.getRemainCount() + diff);
        }
        coupon.setEachLimit(dto.getEachLimit());
        updateById(coupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        // 先删除已发放给用户的领取记录，避免外键冲突
        couponUserMapper.delete(
            new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getCouponId, id)
        );
        // 再删除优惠券主记录
        removeById(id);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<Coupon> pageAdminCoupons(org.example.ordermanagement.dto.AdminCouponQueryDTO dto) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(dto.getName())) {
            wrapper.like(Coupon::getName, dto.getName());
        }
        if (org.springframework.util.StringUtils.hasText(dto.getType())) {
            wrapper.eq(Coupon::getType, dto.getType());
        }
        wrapper.orderByDesc(Coupon::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Coupon> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<CouponVO> pageAvailableCoupons(Long userId, org.example.ordermanagement.dto.UserCouponQueryDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
            .eq(Coupon::getStatus, "ACTIVE")
            .le(Coupon::getStartTime, now)
            .ge(Coupon::getEndTime, now)
            .orderByDesc(Coupon::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Coupon> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        baseMapper.selectPage(page, wrapper);

        // 查询用户已领取的优惠券（包含已使用的，用于判断是否已达领取上限）
        List<CouponUser> userCoupons = couponUserMapper.selectList(
            new LambdaQueryWrapper<CouponUser>()
                .eq(CouponUser::getUserId, userId)
        );
        Map<Long, Long> couponCountMap = userCoupons.stream()
            .collect(Collectors.groupingBy(CouponUser::getCouponId, Collectors.counting()));

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<CouponVO> voList = page.getRecords().stream().map(coupon -> {
            CouponVO vo = new CouponVO();
            vo.setId(coupon.getId());
            vo.setName(coupon.getName());
            vo.setType(coupon.getType());
            vo.setValue(coupon.getValue());
            vo.setMinAmount(coupon.getMinAmount());
            vo.setStartTime(coupon.getStartTime());
            vo.setEndTime(coupon.getEndTime());
            vo.setRemainCount(coupon.getRemainCount());
            vo.setEachLimit(coupon.getEachLimit());
            vo.setStatus(coupon.getStatus());

            Long ownedCount = couponCountMap.getOrDefault(coupon.getId(), 0L);
            vo.setOwnedCount(ownedCount);
            vo.setReceived(ownedCount > 0);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null) throw new BusinessException("优惠券不存在");
        if (!"ACTIVE".equals(coupon.getStatus())) throw new BusinessException("优惠券已禁用");
        if (LocalDateTime.now().isBefore(coupon.getStartTime()) || LocalDateTime.now().isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券不在领取时间范围内");
        }
        if (coupon.getTotalCount() > 0 && coupon.getRemainCount() <= 0) {
            throw new BusinessException("优惠券已领完");
        }

        // 检查限领数量
        int ownedCount = couponUserMapper.countByUserId(userId, couponId);
        if (ownedCount >= coupon.getEachLimit()) {
            throw new BusinessException("已达到领取上限");
        }

        // 领取优惠券
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        couponUser.setStatus("UNUSED");
        couponUser.setReceiveTime(LocalDateTime.now());
        couponUserMapper.insert(couponUser);

        // 减少库存
        if (coupon.getTotalCount() > 0) {
            coupon.setRemainCount(coupon.getRemainCount() - 1);
            updateById(coupon);
        }
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<CouponVO> pageMyCoupons(Long userId, org.example.ordermanagement.dto.UserCouponQueryDTO dto) {
        LambdaQueryWrapper<CouponUser> wrapper = new LambdaQueryWrapper<CouponUser>()
            .eq(CouponUser::getUserId, userId);
        if (org.springframework.util.StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(CouponUser::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(CouponUser::getReceiveTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponUser> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        couponUserMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        // 批量查券模板，消除 N+1（原：每条领券记录单独 getById）
        Set<Long> couponIdSet = page.getRecords().stream()
                .map(CouponUser::getCouponId)
                .collect(Collectors.toSet());
        Map<Long, Coupon> couponMap = couponIdSet.isEmpty()
                ? Collections.emptyMap()
                : listByIds(couponIdSet).stream()
                        .collect(Collectors.toMap(Coupon::getId, Function.identity()));

        List<CouponVO> voList = page.getRecords().stream()
                .filter(cu -> couponMap.containsKey(cu.getCouponId()))
                .map(cu -> {
                    Coupon coupon = couponMap.get(cu.getCouponId());
                    CouponVO vo = new CouponVO();
                    vo.setId(coupon.getId());
                    vo.setName(coupon.getName());
                    vo.setType(coupon.getType());
                    vo.setValue(coupon.getValue());
                    vo.setMinAmount(coupon.getMinAmount());
                    vo.setStartTime(coupon.getStartTime());
                    vo.setEndTime(coupon.getEndTime());
                    vo.setStatus(cu.getStatus());
                    return vo;
                }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
