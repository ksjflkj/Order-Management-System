package org.example.ordermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ordermanagement.dto.ActivitySaveDTO;
import org.example.ordermanagement.entity.Activity;
import org.example.ordermanagement.vo.ActivityVO;

import java.math.BigDecimal;
import java.util.List;

public interface ActivityService extends IService<Activity> {
    // 管理员
    void createActivity(ActivitySaveDTO dto);
    void updateActivity(Long id, ActivitySaveDTO dto);
    void deleteActivity(Long id);
    com.baomidou.mybatisplus.core.metadata.IPage<ActivityVO> pageAdminActivities(org.example.ordermanagement.dto.AdminActivityQueryDTO dto);

    // 获取进行中的活动
    List<ActivityVO> getActiveActivities(Long merchantId);

    // 计算订单优惠
    BigDecimal calculateDiscount(BigDecimal orderAmount, Long merchantId);
}
