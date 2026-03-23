package com.hbk.service;

import com.hbk.dto.AdminDashboardDTO;
import com.hbk.dto.ChartDataProjection;
import com.hbk.repository.AdminDashboardRepository;
import com.hbk.repository.MemberRepository; // 🌟 MemberRepository로 교체 완료!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository dashboardRepository;
    private final MemberRepository memberRepository; // 🌟 Member 주입

    public AdminDashboardDTO.DailySummary getDailySummary() {
        long todayRevenue = dashboardRepository.getTodayRevenue();
        long todayOrders = dashboardRepository.getTodayOrderCount();

        // MemberRepository에서 오늘 가입한 신규 멤버 수 가져오기
        long newMembers = memberRepository.countTodayNewMembers();
        long pendingInquiries = 0L; // TODO: 1:1 문의 테이블 연동 시 수정

        return AdminDashboardDTO.DailySummary.builder()
                .todayRevenue(todayRevenue)
                .todayOrders(todayOrders)
                .newMembers(newMembers)
                .pendingInquiries(pendingInquiries)
                .build();
    }

    public List<AdminDashboardDTO.ChartData> getWeeklyChartData() {
        List<ChartDataProjection> projections = dashboardRepository.getWeeklyChartData();

        return projections.stream().map(p ->
                AdminDashboardDTO.ChartData.builder()
                        .date(p.getDate())
                        .revenue(p.getRevenue())
                        .orderCount(p.getOrderCount())
                        .build()
        ).collect(Collectors.toList());
    }

    public List<AdminDashboardDTO.ChartData> getMonthlyChartData() {
        List<ChartDataProjection> projections = dashboardRepository.getMonthlyChartData();

        return projections.stream().map(p ->
                AdminDashboardDTO.ChartData.builder()
                        .date(p.getDate())
                        .revenue(p.getRevenue())
                        .orderCount(p.getOrderCount())
                        .build()
        ).collect(Collectors.toList());
    }
}