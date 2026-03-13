package com.hbk.dto;

import com.hbk.entity.Order;
import com.hbk.entity.OrderItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long orderId;
    private String orderNo;
    private String status;
    private Integer totalPrice;
    private String receiverName;
    private String receiverPhone;
    private String zip;
    private String address1;
    private String address2;
    private String memo;
    private List<OrderItemResponseDTO> items;

    public static OrderResponseDTO from(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .zip(order.getZip())
                .address1(order.getAddress1())
                .address2(order.getAddress2())
                .memo(order.getMemo())
                .items(order.getItems().stream()
                        .map(OrderItemResponseDTO::from)
                        .toList())
                .build();
    }
}