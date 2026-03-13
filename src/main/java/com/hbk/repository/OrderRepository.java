package com.hbk.repository;

import com.hbk.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMember_IdOrderByCreatedAtDesc(Long memberId);

    Optional<Order> findByOrderNo(String orderNo);
}