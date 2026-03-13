package com.hbk.service;

import com.hbk.dto.OrderCreateRequestDTO;
import com.hbk.dto.OrderResponseDTO;
import com.hbk.entity.*;
import com.hbk.repository.CartRepository;
import com.hbk.repository.MemberRepository;
import com.hbk.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;

    public OrderResponseDTO createOrderFromCart(Long memberId, OrderCreateRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "회원이 존재하지 않습니다. id=" + memberId));

        Cart cart = cartRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "장바구니가 없습니다."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문할 상품이 없습니다.");
        }

        int totalPrice = 0;

        Order order = Order.builder()
                .member(member)
                .orderNo(generateOrderNo())
                .status(OrderStatus.PENDING)
                .totalPrice(0)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .zip(request.getZip())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .memo(request.getMemo())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Sku sku = cartItem.getSku();

            validateSkuActive(sku);
            Inventory inventory = getInventoryOrThrow(sku);
            validateStock(inventory, cartItem.getQuantity());

            int orderPrice = sku.getPrice();
            int quantity = cartItem.getQuantity();
            int lineAmount = orderPrice * quantity;

            OrderItem orderItem = OrderItem.builder()
                    .sku(sku)
                    .orderPrice(orderPrice)
                    .quantity(quantity)
                    .lineAmount(lineAmount)
                    .build();

            order.addItem(orderItem);

            inventory.setStockQty(inventory.getStockQty() - quantity);
            totalPrice += lineAmount;
        }

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        cart.clearItems();

        return OrderResponseDTO.from(savedOrder);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private Inventory getInventoryOrThrow(Sku sku) {
        if (sku.getInventory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "재고 정보가 없는 SKU입니다. skuId=" + sku.getId());
        }
        return sku.getInventory();
    }

    private void validateStock(Inventory inventory, int requestedQty) {
        if (requestedQty < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다.");
        }
        if (inventory.getStockQty() == null || requestedQty > inventory.getStockQty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재고를 초과했습니다.");
        }
    }

    private void validateSkuActive(Sku sku) {
        if (sku.getIsActive() != null && !sku.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비활성 SKU는 주문할 수 없습니다.");
        }
    }
}