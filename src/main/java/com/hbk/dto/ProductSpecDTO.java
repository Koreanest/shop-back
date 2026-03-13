package com.hbk.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecDTO {
    private Integer headSizeSqIn;
    private Integer unstrungWeightG;
    private Integer balanceMm;
    private BigDecimal lengthIn;
    private Integer patternMain;
    private Integer patternCross;
    private Integer stiffnessRa;
}