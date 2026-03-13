package com.hbk.dto;

import com.hbk.entity.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListItemDTO {

    private Long id;

    private Long brandId;
    private String brandName;

    private String title;
    private String series;

    private Integer price;

    private String status;

    private String slug;

    private Long categoryId;
    private String categoryName;

    private String imageUrl;

    // 목록에서는 보통 필요 없지만 기존 구조 유지
    private List<ProductSizeDTO> sizes;

    /**
     * Product → ProductListItemDTO 변환
     *
     * 목록 조회 API에서 사용
     */
    public static ProductListItemDTO from(Product product) {

        return ProductListItemDTO.builder()
                .id(product.getId())

                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)

                .title(product.getTitle())
                .series(product.getSeries())

                .price(product.getPrice())

                .status(product.getStatus() != null ? product.getStatus().name() : null)

                .slug(product.getSlug())

                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)

                .imageUrl(product.getImageUrl())

                // 목록에서는 size 정보는 보통 사용하지 않으므로 비워둔다
                .sizes(List.of())

                .build();
    }
}