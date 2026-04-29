package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.ordermanagement.entity.CouponUser;

import java.util.List;

@Mapper
public interface CouponUserMapper extends BaseMapper<CouponUser> {
    Integer countByUserId(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
