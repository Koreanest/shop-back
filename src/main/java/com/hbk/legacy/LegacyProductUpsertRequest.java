package com.hbk.legacy;

import com.hbk.dto.ProductSizeDTO;
import com.hbk.dto.ProductSpecDTO;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LegacyProductUpsertRequest {
    private String title;
    private String desc;
    private Integer price;
    private Long categoryId;

    private List<ProductSizeDTO> sizes;
    private List<ProductSpecDTO> specs;
}