package com.hbk.dto;

import com.hbk.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long orderItemId;
    private Long skuId;
    private String skuCode;
    private String size;
    private Long productId;
    private String productTitle;
    private Integer orderPrice;
    private Integer quantity;
    private Integer lineAmount;

    public static OrderItemResponseDTO from(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .orderItemId(item.getId())
                .skuId(item.getSku().getId())
                .skuCode(item.getSku().getSkuCode())
                .size(item.getSku().getGripSize())
                .productId(item.getSku().getProduct() != null ? item.getSku().getProduct().getId() : null)
                .productTitle(item.getSku().getProduct() != null ? item.getSku().getProduct().getTitle() : null)
                .orderPrice(item.getOrderPrice())
                .quantity(item.getQuantity())
                .lineAmount(item.getLineAmount())
                .build();
    }
}