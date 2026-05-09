package com.print.module.dashboard;

import com.print.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(dashboardService.getStatistics());
    }

    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> orderTrend() {
        return Result.success(dashboardService.getOrderTrend());
    }

    @GetMapping("/customer-ranking")
    public Result<List<Map<String, Object>>> customerRanking() {
        return Result.success(dashboardService.getCustomerRanking());
    }
}
