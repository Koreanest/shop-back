package com.hbk.controller;

import com.hbk.dto.OrderCreateRequestDTO;
import com.hbk.dto.OrderListItemResponseDTO;
import com.hbk.dto.OrderResponseDTO;
import com.hbk.entity.OrderStatus;
import com.hbk.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

    private final OrderService orderService;

    /**
     * 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderCreateRequestDTO request,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);
        return ResponseEntity.ok(orderService.createOrderFromCart(memberId, request));
    }

    /**
     * 내 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderListItemResponseDTO>> getMyOrders(HttpSession session) {
        Long memberId = getLoginMemberId(session);
        return ResponseEntity.ok(orderService.getMyOrders(memberId));
    }

    /**
     * 내 주문 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getMyOrderDetail(
            @PathVariable Long id,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);
        return ResponseEntity.ok(orderService.getMyOrderDetail(memberId, id));
    }

    /**
     * 내 주문 취소
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelMyOrder(
            @PathVariable Long id,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);
        return ResponseEntity.ok(orderService.cancelMyOrder(memberId, id));
    }

    /**
     * 주문 결제 처리
     * 현재는 관리자/시스템 처리 성격으로 두고 세션 검증 없이 연결
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponseDTO> payOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markPaid(id));
    }

    /**
     * 주문 준비중 처리
     */
    @PostMapping("/{id}/prepare")
    public ResponseEntity<OrderResponseDTO> prepareOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.startPreparing(id));
    }

    /**
     * 주문 배송 처리
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderResponseDTO> shipOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.shipOrder(id));
    }

    /**
     * 주문 완료 처리
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderResponseDTO> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    private Long getLoginMemberId(HttpSession session) {
        Object loginValue = session.getAttribute(LOGIN_MEMBER_ID);

        if (loginValue == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (loginValue instanceof Long memberId) {
            return memberId;
        }

        if (loginValue instanceof Integer memberId) {
            return memberId.longValue();
        }

        if (loginValue instanceof String memberId) {
            try {
                return Long.parseLong(memberId);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
    }
}