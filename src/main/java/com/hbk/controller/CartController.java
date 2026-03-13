package com.hbk.controller;

import com.hbk.dto.CartAddRequestDTO;
import com.hbk.dto.CartItemQuantityUpdateRequestDTO;
import com.hbk.dto.CartResponseDTO;
import com.hbk.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CartController {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

    private final CartService cartService;

    private Long getLoginMemberId(HttpSession session) {
        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);
        if (memberId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return memberId;
    }

    /**
     * 장바구니 조회
     */
    @GetMapping
    public CartResponseDTO getCart(HttpSession session) {
        Long memberId = getLoginMemberId(session);
        return cartService.getCart(memberId);
    }

    /**
     * 장바구니 상품 추가
     */
    @PostMapping("/items")
    public CartResponseDTO addItem(
            HttpSession session,
            @Valid @RequestBody CartAddRequestDTO request
    ) {
        Long memberId = getLoginMemberId(session);
        return cartService.addItem(memberId, request);
    }

    /**
     * 장바구니 수량 수정
     */
    @PatchMapping("/items/{id}")
    public CartResponseDTO updateQuantity(
            HttpSession session,
            @PathVariable Long id,
            @Valid @RequestBody CartItemQuantityUpdateRequestDTO request
    ) {
        Long memberId = getLoginMemberId(session);
        return cartService.updateQuantity(memberId, id, request);
    }

    /**
     * 장바구니 항목 삭제
     */
    @DeleteMapping("/items/{id}")
    public CartResponseDTO deleteItem(
            HttpSession session,
            @PathVariable Long id
    ) {
        Long memberId = getLoginMemberId(session);
        return cartService.deleteItem(memberId, id);
    }
}