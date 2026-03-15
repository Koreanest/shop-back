package com.hbk.service;

import com.hbk.dto.OrderCreateRequestDTO;
import com.hbk.dto.OrderListItemResponseDTO;
import com.hbk.dto.OrderResponseDTO;
import com.hbk.entity.Cart;
import com.hbk.entity.CartItem;
import com.hbk.entity.Inventory;
import com.hbk.entity.Member;
import com.hbk.entity.Order;
import com.hbk.entity.OrderItem;
import com.hbk.entity.OrderStatus;
import com.hbk.entity.Sku;
import com.hbk.repository.CartRepository;
import com.hbk.repository.InventoryRepository;
import com.hbk.repository.MemberRepository;
import com.hbk.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * 장바구니 기준 주문 생성
     */
    public OrderResponseDTO createOrderFromCart(Long memberId, OrderCreateRequestDTO request) {
        Member member = getValidatedMember(memberId);
        Cart cart = getValidatedCart(memberId);

        validateCartNotEmpty(cart);

        Order order = createDraftOrder(member, request);

        for (CartItem cartItem : cart.getItems()) {
            PreparedOrderLine prepared = prepareOrderLine(cartItem);

            order.addItem(prepared.orderItem());
            deductStock(prepared.inventory(), prepared.quantity());
        }

        int totalPrice = calculateOrderTotal(order.getItems());
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.saveAndFlush(order);
        savedOrder.setOrderNo(generateFinalOrderNo(savedOrder.getId()));

        Order finalOrder = orderRepository.save(savedOrder);

        clearCart(cart);

        return OrderResponseDTO.from(finalOrder);
    }

    /**
     * 내 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderListItemResponseDTO> getMyOrders(Long memberId) {
        Member member = getValidatedMember(memberId);

        return orderRepository.findByMember_IdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(OrderListItemResponseDTO::from)
                .toList();
    }

    /**
     * 내 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getMyOrderDetail(Long memberId, Long orderId) {
        validateMemberExists(memberId);

        Order order = orderRepository.findByIdAndMember_Id(orderId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문이 존재하지 않거나 조회 권한이 없습니다. orderId=" + orderId
                ));

        return OrderResponseDTO.from(order);
    }

    /**
     * 내 주문 취소
     */
    public OrderResponseDTO cancelMyOrder(Long memberId, Long orderId) {
        validateMemberExists(memberId);

        Order order = orderRepository.findWithLockByIdAndMember_Id(orderId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문이 존재하지 않거나 취소 권한이 없습니다. orderId=" + orderId
                ));

        validateOrderItemsExist(order);
        changeOrderStatus(order, OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            restoreStockForOrderItem(item);
        }

        return OrderResponseDTO.from(order);
    }

    /**
     * 주문 결제 처리
     */
    public OrderResponseDTO markPaid(Long orderId) {
        Order order = getOrderForStatusChangeOrThrow(orderId);
        changeOrderStatus(order, OrderStatus.PAID);
        return OrderResponseDTO.from(order);
    }

    /**
     * 주문 준비중 처리
     */
    public OrderResponseDTO startPreparing(Long orderId) {
        Order order = getOrderForStatusChangeOrThrow(orderId);
        changeOrderStatus(order, OrderStatus.PREPARING);
        return OrderResponseDTO.from(order);
    }

    /**
     * 주문 배송 처리
     */
    public OrderResponseDTO shipOrder(Long orderId) {
        Order order = getOrderForStatusChangeOrThrow(orderId);
        changeOrderStatus(order, OrderStatus.SHIPPED);
        return OrderResponseDTO.from(order);
    }

    /**
     * 주문 완료 처리
     */
    public OrderResponseDTO completeOrder(Long orderId) {
        Order order = getOrderForStatusChangeOrThrow(orderId);
        changeOrderStatus(order, OrderStatus.COMPLETED);
        return OrderResponseDTO.from(order);
    }

    /**
     * 상태 변경용 주문 조회 (lock)
     */
    private Order getOrderForStatusChangeOrThrow(Long orderId) {
        return orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "주문이 존재하지 않습니다. orderId=" + orderId
                ));
    }

    /**
     * 회원 검증 + 조회
     */
    private Member getValidatedMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원이 존재하지 않습니다. id=" + memberId
                ));
    }

    /**
     * 회원 존재 여부만 검증
     */
    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "회원이 존재하지 않습니다. id=" + memberId
            );
        }
    }

    /**
     * 장바구니 조회
     */
    private Cart getValidatedCart(Long memberId) {
        return cartRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "장바구니가 없습니다."
                ));
    }

    /**
     * 장바구니 비어있는지 검증
     */
    private void validateCartNotEmpty(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문할 상품이 없습니다.");
        }
    }

    /**
     * 주문 항목 존재 검증
     */
    private void validateOrderItemsExist(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 항목이 없어 처리할 수 없습니다.");
        }
    }

    /**
     * 임시 주문 생성
     */
    private Order createDraftOrder(Member member, OrderCreateRequestDTO request) {
        return Order.builder()
                .member(member)
                .orderNo(createTemporaryOrderNo())
                .status(OrderStatus.PENDING)
                .totalPrice(0)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .zip(request.getZip())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .memo(request.getMemo())
                .build();
    }

    /**
     * CartItem -> 주문 라인 준비
     */
    private PreparedOrderLine prepareOrderLine(CartItem cartItem) {
        validateCartItem(cartItem);

        Sku sku = cartItem.getSku();
        validateSkuOrderable(sku);

        int quantity = normalizeQuantity(cartItem.getQuantity());
        int unitPrice = validateAndGetUnitPrice(sku);

        Inventory inventory = lockInventory(sku.getId());
        validateAvailableStock(inventory, quantity);

        OrderItem orderItem = createOrderItem(cartItem, unitPrice, quantity);

        return new PreparedOrderLine(orderItem, inventory, quantity);
    }

    /**
     * 장바구니 상품 검증
     */
    private void validateCartItem(CartItem cartItem) {
        if (cartItem == null || cartItem.getSku() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "장바구니 상품 정보가 올바르지 않습니다.");
        }
    }

    /**
     * SKU 주문 가능 여부 검증
     */
    private void validateSkuOrderable(Sku sku) {
        validateSkuActive(sku);
    }

    /**
     * 비활성 SKU 주문 불가
     */
    private void validateSkuActive(Sku sku) {
        if (sku.getIsActive() != null && !sku.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비활성 SKU는 주문할 수 없습니다.");
        }
    }

    /**
     * 주문 수량 정규화
     */
    private int normalizeQuantity(Integer quantity) {
        int requestedQty = quantity == null ? 0 : quantity;
        validateRequestedQuantity(requestedQty);
        return requestedQty;
    }

    /**
     * 주문 수량 검증
     */
    private void validateRequestedQuantity(int requestedQty) {
        if (requestedQty < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다.");
        }
    }

    /**
     * SKU 단가 검증
     */
    private int validateAndGetUnitPrice(Sku sku) {
        Integer unitPrice = sku.getPrice();

        if (unitPrice == null || unitPrice < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SKU 가격 정보가 올바르지 않습니다. skuId=" + sku.getId()
            );
        }

        return unitPrice;
    }

    /**
     * 재고 lock 조회
     */
    private Inventory lockInventory(Long skuId) {
        return inventoryRepository.findBySkuIdForUpdate(skuId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "재고 정보가 없는 SKU입니다. skuId=" + skuId
                ));
    }

    /**
     * 주문 가능 재고 검증
     */
    private void validateAvailableStock(Inventory inventory, int requestedQty) {
        if (inventory.getStockQty() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재고 수량 정보가 없습니다.");
        }

        int stockQty = inventory.getStockQty();
        int safetyStockQty = inventory.getSafetyStockQty() == null ? 0 : inventory.getSafetyStockQty();

        if (requestedQty > stockQty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재고를 초과했습니다.");
        }

        if (stockQty - requestedQty < safetyStockQty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "안전재고 이하로는 주문할 수 없습니다.");
        }
    }

    /**
     * 재고 차감
     */
    private void deductStock(Inventory inventory, int quantity) {
        inventory.setStockQty(inventory.getStockQty() - quantity);
    }

    /**
     * 주문 취소 시 재고 복구
     */
    private void restoreStockForOrderItem(OrderItem item) {
        if (item == null || item.getSku() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "주문 항목 정보가 올바르지 않습니다.");
        }

        Inventory inventory = lockInventory(item.getSku().getId());
        int restoreQty = normalizeRestoreQuantity(item.getQuantity());

        Integer currentStock = inventory.getStockQty();
        if (currentStock == null) {
            currentStock = 0;
        }

        inventory.setStockQty(currentStock + restoreQty);
    }

    /**
     * 복구 수량 검증
     */
    private int normalizeRestoreQuantity(Integer quantity) {
        int restoreQty = quantity == null ? 0 : quantity;

        if (restoreQty < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "복구할 주문 수량이 올바르지 않습니다.");
        }

        return restoreQty;
    }

    /**
     * 주문 항목 생성
     */
    private OrderItem createOrderItem(CartItem cartItem, int unitPrice, int quantity) {
        Sku sku = cartItem.getSku();
        int lineTotal = calculateLineTotal(unitPrice, quantity);

        String productTitle = sku.getProduct() != null ? sku.getProduct().getTitle() : "UNKNOWN_PRODUCT";
        String brandName = (sku.getProduct() != null && sku.getProduct().getBrand() != null)
                ? sku.getProduct().getBrand().getName()
                : "UNKNOWN_BRAND";

        return OrderItem.builder()
                .sku(sku)
                .productNameSnapshot(productTitle)
                .brandNameSnapshot(brandName)
                .gripSnapshot(sku.getGripSize())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();
    }

    /**
     * 주문 라인 금액 계산
     */
    private int calculateLineTotal(int unitPrice, int quantity) {
        return unitPrice * quantity;
    }

    /**
     * 주문 총액 계산
     */
    private int calculateOrderTotal(List<OrderItem> items) {
        return items.stream()
                .mapToInt(item -> item.getLineTotal() == null ? 0 : item.getLineTotal())
                .sum();
    }

    /**
     * 주문 후 장바구니 비우기
     */
    private void clearCart(Cart cart) {
        cart.clearItems();
    }

    /**
     * 주문 상태 변경
     */
    private void changeOrderStatus(Order order, OrderStatus nextStatus) {
        validateStatusTransition(order.getStatus(), nextStatus);
        order.setStatus(nextStatus);
    }

    /**
     * 주문 상태 전이 검증
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 주문 상태가 없습니다.");
        }

        if (next == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "변경할 주문 상태가 없습니다.");
        }

        if (current == next) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "동일한 상태로 변경할 수 없습니다.");
        }

        switch (current) {
            case PENDING -> {
                if (next != OrderStatus.PAID && next != OrderStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "PENDING 상태에서는 PAID 또는 CANCELLED로만 변경할 수 있습니다."
                    );
                }
            }
            case PAID -> {
                if (next != OrderStatus.PREPARING && next != OrderStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "PAID 상태에서는 PREPARING 또는 CANCELLED로만 변경할 수 있습니다."
                    );
                }
            }
            case PREPARING -> {
                if (next != OrderStatus.SHIPPED && next != OrderStatus.CANCELLED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "PREPARING 상태에서는 SHIPPED 또는 CANCELLED로만 변경할 수 있습니다."
                    );
                }
            }
            case SHIPPED -> {
                if (next != OrderStatus.COMPLETED) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "SHIPPED 상태에서는 COMPLETED로만 변경할 수 있습니다."
                    );
                }
            }
            case COMPLETED -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "완료된 주문은 상태를 변경할 수 없습니다."
            );
            case CANCELLED -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "취소된 주문은 상태를 변경할 수 없습니다."
            );
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "지원하지 않는 주문 상태입니다."
            );
        }
    }

    /**
     * 임시 주문번호
     */
    private String createTemporaryOrderNo() {
        return "TMP-" + UUID.randomUUID();
    }

    /**
     * 최종 주문번호
     * 예: ORD-20260316-0000000123
     */
    private String generateFinalOrderNo(Long orderId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "ORD-" + datePart + "-" + String.format("%010d", orderId);
    }

    private record PreparedOrderLine(OrderItem orderItem, Inventory inventory, int quantity) {
    }
}