package org.example.ordermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.ordermanagement.entity.Activity;
import org.example.ordermanagement.vo.ActivityVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
//    List<Activity> selectActiveActivities(LocalDateTime now);
//    List<Activity> selectByMerchantId(Long merchantId);
    /**
     * 查询生效的活动列表（包含商户名称）
     *
     * @param currentTime 当前时间，用于判断活动是否生效
     * @param merchantId  商户ID（可为空）
     * @return 包含商户名称的活动视图对象列表
     */
    List<ActivityVO> selectActiveActivitiesWithMerchant(@Param("currentTime") LocalDateTime currentTime,
                                                        @Param("merchantId") Long merchantId);
}
