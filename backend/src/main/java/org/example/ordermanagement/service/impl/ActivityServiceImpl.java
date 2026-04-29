package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.dto.ActivitySaveDTO;
import org.example.ordermanagement.entity.Activity;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.mapper.ActivityMapper;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.example.ordermanagement.service.ActivityService;
import org.example.ordermanagement.vo.ActivityVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private final MerchantMapper merchantMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void createActivity(ActivitySaveDTO dto) {
        Activity activity = new Activity();
        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setRules(dto.getRules());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setMerchantId(dto.getMerchantId());
        activity.setStatus("ACTIVE");
        save(activity);
    }

    @Override
    public void updateActivity(Long id, ActivitySaveDTO dto) {
        Activity activity = getById(id);
        if (activity == null) throw new BusinessException("活动不存在");

        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setRules(dto.getRules());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setMerchantId(dto.getMerchantId());
        updateById(activity);
    }

    @Override
    public void deleteActivity(Long id) {
        removeById(id);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<ActivityVO> pageAdminActivities(org.example.ordermanagement.dto.AdminActivityQueryDTO dto) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (org.springframework.util.StringUtils.hasText(dto.getName())) {
            wrapper.like(Activity::getName, dto.getName());
        }
        wrapper.orderByDesc(Activity::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Activity> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        baseMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<ActivityVO> voList = page.getRecords().stream().map(a -> {
            ActivityVO vo = new ActivityVO();
            vo.setId(a.getId());
            vo.setName(a.getName());
            vo.setDescription(a.getDescription());
            vo.setRules(a.getRules());
            vo.setStartTime(a.getStartTime());
            vo.setEndTime(a.getEndTime());
            vo.setMerchantId(a.getMerchantId());
            vo.setStatus(a.getStatus());
            if (a.getMerchantId() != null) {
                Merchant merchant = merchantMapper.selectById(a.getMerchantId());
                if (merchant != null) vo.setMerchantName(merchant.getShopName());
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ActivityVO> getActiveActivities(Long merchantId) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 直接调用 Mapper，数据库一步到位返回组装好的 VO 列表
        return baseMapper.selectActiveActivitiesWithMerchant(now, merchantId);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, Long merchantId) {
        List<ActivityVO> activities = getActiveActivities(merchantId);
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (ActivityVO activity : activities) {
            try {
                List<Map<String, Object>> rules = objectMapper.readValue(
                    activity.getRules(), new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> rule : rules) {
                    BigDecimal threshold = new BigDecimal(rule.get("threshold").toString());
                    BigDecimal reduce = new BigDecimal(rule.get("reduce").toString());

                    if (orderAmount.compareTo(threshold) >= 0) {
                        if (reduce.compareTo(maxDiscount) > 0) {
                            maxDiscount = reduce;
                        }
                    }
                }
            } catch (Exception e) {
                // 解析规则失败，跳过
            }
        }

        return maxDiscount;
    }
}
