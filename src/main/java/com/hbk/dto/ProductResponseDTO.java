package com.hbk.dto;

import com.hbk.entity.Product;
import com.hbk.entity.ProductSpec;
import com.hbk.entity.Sku;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;

    private Long brandId;
    private String brandName;

    private String title;
    private String series;
    private String description;
    private Integer price;
    private String status;
    private String slug;

    private Long categoryId;
    private String categoryName;

    private String imageUrl;

    private ProductSpecDTO spec;
    private List<ProductSizeItemDTO> sizes;

    public static ProductResponseDTO from(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .title(product.getTitle())
                .series(product.getSeries())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .slug(product.getSlug())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .imageUrl(product.getImageUrl())
                .spec(ProductSpecDTO.from(product.getSpec()))
                .sizes(product.getSizes() != null
                        ? product.getSizes().stream()
                        .map(ProductSizeItemDTO::from)
                        .toList()
                        : List.of())
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSpecDTO {
        private Integer headSizeSqIn;
        private Integer unstrungWeightG;
        private Integer balanceMm;
        private BigDecimal lengthIn;
        private Integer patternMain;
        private Integer patternCross;
        private Integer stiffnessRa;

        public static ProductSpecDTO from(ProductSpec spec) {
            if (spec == null) return null;

            return ProductSpecDTO.builder()
                    .headSizeSqIn(spec.getHeadSizeSqIn())
                    .unstrungWeightG(spec.getUnstrungWeightG())
                    .balanceMm(spec.getBalanceMm())
                    .lengthIn(spec.getLengthIn())
                    .patternMain(spec.getPatternMain())
                    .patternCross(spec.getPatternCross())
                    .stiffnessRa(spec.getStiffnessRa())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSizeItemDTO {
        private Long id;
        private String size;
        private Integer stock;

        public static ProductSizeItemDTO from(Sku sku) {
            return ProductSizeItemDTO.builder()
                    .id(sku.getId())
                    .size(sku.getGripSize())
                    .stock(sku.getInventory() != null ? sku.getInventory().getStockQty() : 0)
                    .build();
        }
    }
}