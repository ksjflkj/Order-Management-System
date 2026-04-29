package org.example.ordermanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.vo.AdminDashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台管理员数据展板控制器 - 为超级管理的首页大屏提供关键性的宏观业务指标统计。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;

    @GetMapping
    public Result<AdminDashboardVO> dashboard() {
        return Result.success(orderService.getAdminDashboard());
    }
}
