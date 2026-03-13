package com.hbk.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductListItemDTO {
    private Long id;
    private String title;
    private Integer price;
    private String imageUrl;
    private String slug;
    private Long categoryId;

    // 리스트에서도 사이즈 재고를 보여주고 싶으면 포함
    private List<ProductSizeDTO> sizes;
}