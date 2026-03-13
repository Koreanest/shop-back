package com.hbk.dto.mapper;

import com.hbk.dto.ProductResponseDTO;
import com.hbk.dto.ProductResponseDTO.ProductSizeItemDTO;
import com.hbk.dto.ProductResponseDTO.ProductSpecDTO;
import com.hbk.entity.Product;
import com.hbk.entity.ProductSpec;
import com.hbk.entity.Sku;

import java.util.List;

public class ProductMapper {

    public static ProductResponseDTO toResponse(Product p) {

        ProductSpecDTO specDto = null;

        if (p.getSpec() != null) {
            ProductSpec spec = p.getSpec();

            specDto = ProductSpecDTO.builder()
                    .headSizeSqIn(spec.getHeadSizeSqIn())
                    .unstrungWeightG(spec.getUnstrungWeightG())
                    .balanceMm(spec.getBalanceMm())
                    .lengthIn(spec.getLengthIn())
                    .patternMain(spec.getPatternMain())
                    .patternCross(spec.getPatternCross())
                    .stiffnessRa(spec.getStiffnessRa())
                    .build();
        }

        List<ProductSizeItemDTO> sizes =
                p.getSizes() == null ? List.of() :
                        p.getSizes().stream()
                                .map(ProductMapper::toSizeDto)
                                .toList();

        return ProductResponseDTO.builder()
                .id(p.getId())
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .title(p.getTitle())
                .series(p.getSeries())
                .description(p.getDescription())
                .price(p.getPrice())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .slug(p.getSlug())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .imageUrl(p.getImageUrl())
                .spec(specDto)
                .sizes(sizes)
                .build();
    }

    private static ProductSizeItemDTO toSizeDto(Sku s) {
        return ProductSizeItemDTO.builder()
                .id(s.getId())
                .size(s.getGripSize())
                .stock(s.getInventory() != null ? s.getInventory().getStockQty() : 0)
                .build();
    }
}