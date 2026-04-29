package org.example.ordermanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.dto.ActivitySaveDTO;
import org.example.ordermanagement.service.ActivityService;
import org.example.ordermanagement.vo.ActivityVO;
import org.example.ordermanagement.common.result.Result;
import org.springframework.web.bind.annotation.*;


/**
 * 平台级营销活动管理后台 - 负责全局满减、新客等活动的增删改查配置。
 */
@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    /**
     * 创建新的促销活动
     */
    @PostMapping
    public Result<Void> create(@RequestBody ActivitySaveDTO dto) {
        activityService.createActivity(dto);
        return Result.success();
    }

    /**
     * 编辑指定活动（如延长有效时间、改额度）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ActivitySaveDTO dto) {
        activityService.updateActivity(id, dto);
        return Result.success();
    }

    /**
     * 删除指定的活动
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.success();
    }

    /**
     * 分页查询平台的促销活动列表
     */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<ActivityVO>> page(org.example.ordermanagement.dto.AdminActivityQueryDTO dto) {
        return Result.success(activityService.pageAdminActivities(dto));
    }
}
