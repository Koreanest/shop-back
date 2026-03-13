package com.hbk.dto;

import com.hbk.entity.Cart;
import com.hbk.entity.CartItem;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {

    private Long cartId;
    private Long memberId;
    private List<CartItemResponseDTO> items;
    private Integer totalItemCount;
    private Integer totalAmount;

    public static CartResponseDTO of(Long memberId, Cart cart) {
        List<CartItem> cartItems = cart.getItems() != null ? cart.getItems() : Collections.emptyList();

        List<CartItemResponseDTO> itemDtos = cartItems.stream()
                .map(CartItemResponseDTO::from)
                .toList();

        int totalItemCount = cartItems.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        int totalAmount = cartItems.stream()
                .mapToInt(item -> {
                    Integer price = (item.getSku() != null && item.getSku().getPrice() != null)
                            ? item.getSku().getPrice()
                            : 0;
                    Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    return price * quantity;
                })
                .sum();

        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .memberId(memberId)
                .items(itemDtos)
                .totalItemCount(totalItemCount)
                .totalAmount(totalAmount)
                .build();
    }
}