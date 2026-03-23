package com.hbk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AdminDashboardDTO {

    // 1. 상단 요약 카드용 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {
        private long todayRevenue;      // 오늘 매출액
        private long todayOrders;       // 오늘 주문 건수
        private long newMembers;        // 오늘 신규 가입자 수
        private long pendingInquiries;  // 대기 중인 1:1 문의 수
    }

    // 2. 차트 데이터용 DTO (주간/월간 공용)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String date;        // 날짜 (예: "2026-03-20" 또는 "2026-03")
        private long revenue;       // 해당 날짜/월의 총 매출
        private long orderCount;    // 해당 날짜/월의 총 주문 건수
    }
}