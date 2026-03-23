package com.hbk.repository;

import com.hbk.entity.Order;
import com.hbk.dto.ChartDataProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// 통계 및 대시보드 전용 Repository (기존 OrderRepository와 분리)
public interface AdminDashboardRepository extends JpaRepository<Order, Long> {

    // 1. 오늘 총 매출액
    @Query(value =
            "SELECT COALESCE(SUM(total_price), 0) " +
                    "FROM orders " +
                    "WHERE DATE(created_at) = CURDATE() " +
                    "AND status = 'PAID'",
            nativeQuery = true)
    Long getTodayRevenue();

    // 2. 오늘 총 주문 건수
    @Query(value = "SELECT COUNT(id) FROM orders WHERE DATE(created_at) = CURDATE()", nativeQuery = true)
    Long getTodayOrderCount();

    // 3. 최근 7일 주간 차트 데이터
    @Query(value =
            "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, " +
                    "COALESCE(SUM(total_price), 0) AS revenue, " +
                    "COUNT(id) AS orderCount " +
                    "FROM orders " +
                    "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                    "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') " +
                    "ORDER BY date ASC", nativeQuery = true)
    List<ChartDataProjection> getWeeklyChartData();

    // 4. 최근 6개월 월간 차트 데이터
    @Query(value =
            "SELECT DATE_FORMAT(created_at, '%Y-%m') AS date, " +
                    "COALESCE(SUM(total_price), 0) AS revenue, " +
                    "COUNT(id) AS orderCount " +
                    "FROM orders " +
                    "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 5 MONTH) " +
                    "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                    "ORDER BY date ASC", nativeQuery = true)
    List<ChartDataProjection> getMonthlyChartData();
}