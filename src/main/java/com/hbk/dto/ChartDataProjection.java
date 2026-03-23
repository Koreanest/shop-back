package com.hbk.dto;

public interface ChartDataProjection {
    String getDate();       // 날짜 (YYYY-MM-DD 또는 YYYY-MM)
    Long getRevenue();      // 매출 합계
    Long getOrderCount();   // 주문 건수 합계
}