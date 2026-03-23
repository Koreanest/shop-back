package com.hbk.controller;

import com.hbk.dto.AdminDashboardDTO;
import com.hbk.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    // 1. 오늘 요약 데이터 조회
    @GetMapping("/daily-summary")
    public ResponseEntity<AdminDashboardDTO.DailySummary> getDailySummary() {
        return ResponseEntity.ok(dashboardService.getDailySummary());
    }

    // 2. 최근 7일 주간 차트 데이터 조회
    @GetMapping("/weekly-chart")
    public ResponseEntity<List<AdminDashboardDTO.ChartData>> getWeeklyChart() {
        return ResponseEntity.ok(dashboardService.getWeeklyChartData());
    }

    // 3. 최근 6개월/1년 월간 차트 데이터 조회
    @GetMapping("/monthly-chart")
    public ResponseEntity<List<AdminDashboardDTO.ChartData>> getMonthlyChart() {
        return ResponseEntity.ok(dashboardService.getMonthlyChartData());
    }
}