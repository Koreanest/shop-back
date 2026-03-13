package com.hbk.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @Column(name = "sku_id")
    private Long id; // PK = FK

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id") // FK column
    private Sku sku;

    @Column(name = "stock_qty", nullable = false )
    private Integer stockQty =0;

    @Column(name = "safety_stock_qty", nullable = false)
    private Integer safetyStockQty=0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 편의 메서드 (선택)
    @PrePersist
    @PreUpdate
    private void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    protected Inventory() {}

    public Inventory(Sku sku, Integer stockQty, Integer safetyStockQty) {
        this.sku = sku;
        this.stockQty = stockQty;
        this.safetyStockQty = safetyStockQty;
        this.updatedAt = LocalDateTime.now();
    }
}
