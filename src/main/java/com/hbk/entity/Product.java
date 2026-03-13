package com.hbk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_brand_id", columnList = "brand_id"),
                @Index(name = "idx_products_category_id", columnList = "category_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "series", length = 80)
    private String series;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "price", nullable = false)
    private Integer price = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "slug", nullable = false, length = 150, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private NavMenu category;

    @Column(name = "image_url", nullable = false, length = 300)
    private String imageUrl;

    @Column(name = "image_path", nullable = false, length = 300)
    private String imagePath;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductSpec spec;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sku> sizes = new ArrayList<>();

    public void setSpec(ProductSpec spec) {
        this.spec = spec;
        if (spec != null) {
            spec.setProduct(this);
        }
    }

    public void addSize(Sku sku) {
        this.sizes.add(sku);
        sku.setProduct(this);
    }

    public void clearSizes() {
        for (Sku sku : this.sizes) {
            sku.setProduct(null);
        }
        this.sizes.clear();
    }
}