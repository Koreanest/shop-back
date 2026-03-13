package com.hbk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_specs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpec {

    @Id
    @Column(name = "product_id")
    private Long productId;   // PK = FK

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "head_size_sq_in")
    private Integer headSizeSqIn;

    @Column(name = "unstrung_weight_g")
    private Integer unstrungWeightG;

    @Column(name = "balance_mm")
    private Integer balanceMm;

    @Column(name = "length_in", precision = 4, scale = 1)
    private BigDecimal lengthIn;

    @Column(name = "pattern_main")
    private Integer patternMain;

    @Column(name = "pattern_cross")
    private Integer patternCross;

    @Column(name = "stiffness_ra")
    private Integer stiffnessRa;
}