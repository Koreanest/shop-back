package com.hbk.dto;

import com.hbk.entity.CartItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDTO {

    private Long cartItemId;

    private Long skuId;
    private String skuCode;
    private String size;

    private Long productId;
    private String productTitle;
    private String productSlug;
    private String imageUrl;

    private Integer unitPrice;
    private Integer quantity;
    private Integer lineAmount;

    private Integer stockQty;
    private Integer safetyStockQty;

    public static CartItemResponseDTO from(CartItem item) {
        return CartItemResponseDTO.builder()
                .cartItemId(item.getId())
                .skuId(item.getSku() != null ? item.getSku().getId() : null)
                .skuCode(item.getSku() != null ? item.getSku().getSkuCode() : null)
                .size(item.getSku() != null ? item.getSku().getGripSize() : null)
                .productId(item.getSku() != null && item.getSku().getProduct() != null ? item.getSku().getProduct().getId() : null)
                .productTitle(item.getSku() != null && item.getSku().getProduct() != null ? item.getSku().getProduct().getTitle() : null)
                .productSlug(item.getSku() != null && item.getSku().getProduct() != null ? item.getSku().getProduct().getSlug() : null)
                .imageUrl(item.getSku() != null && item.getSku().getProduct() != null ? item.getSku().getProduct().getImageUrl() : null)
                .unitPrice(item.getSku() != null ? item.getSku().getPrice() : 0)
                .quantity(item.getQuantity())
                .lineAmount(item.getSku() != null && item.getSku().getPrice() != null
                        ? item.getSku().getPrice() * item.getQuantity()
                        : 0)
                .stockQty(item.getSku() != null && item.getSku().getInventory() != null ? item.getSku().getInventory().getStockQty() : 0)
                .safetyStockQty(item.getSku() != null && item.getSku().getInventory() != null ? item.getSku().getInventory().getSafetyStockQty() : 0)
                .build();
    }
}