package com.hbk.repository;

import com.hbk.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMember_IdOrderByCreatedAtDesc(Long memberId);

    Optional<Order> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = {
            "items",
            "items.sku",
            "items.sku.product"
    })
    Optional<Order> findByIdAndMember_Id(Long id, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "items",
            "items.sku",
            "items.sku.product"
    })
    Optional<Order> findWithLockById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "items",
            "items.sku",
            "items.sku.product"
    })
    Optional<Order> findWithLockByIdAndMember_Id(Long id, Long memberId);
}