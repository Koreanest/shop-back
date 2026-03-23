package com.hbk.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequestDTO {

    private String title;
    private String description;
    private Integer price;

    private Long brandId;
    private Long categoryId;

    private String series;
    private String imageUrl;

    private ProductSpecDTO spec;

    private List<ProductSizeDTO> sizes;
}
