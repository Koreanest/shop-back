package com.hbk.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "product_notice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductNoticeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}