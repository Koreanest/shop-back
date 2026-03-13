package com.hbk.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductSizeDTO {
    private String size;
    private Integer stock;
}