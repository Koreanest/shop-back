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

    /**
     * Product : ProductSpec = 1:1
     * 상품 스펙은 상품과 생명주기를 같이 가져간다.
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductSpec spec;

    /**
     * Product : Sku = 1:N
     * SKU는 상품에 종속되므로 cascade + orphanRemoval 유지
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sku> sizes = new ArrayList<>();

    /**
     * 양방향 연관관계 편의 메서드
     */
    public void setSpec(ProductSpec spec) {
        this.spec = spec;
        if (spec != null) {
            spec.setProduct(this);
        }
    }

    /**
     * SKU 추가
     */
    public void addSize(Sku sku) {
        this.sizes.add(sku);
        sku.setProduct(this);
    }

    /**
     * 특정 SKU 제거
     * sync update 방식에서 요청에 없는 기존 SKU 제거 시 사용
     */
    public void removeSize(Sku sku) {
        this.sizes.remove(sku);
        sku.setProduct(null);
    }

    /**
     * 전량 교체가 필요한 경우를 대비해 유지
     * 현재 update는 sync 방식으로 가지만 create나 특수 케이스에서 쓸 수 있다.
     */
    public void clearSizes() {
        for (Sku sku : this.sizes) {
            sku.setProduct(null);
        }
        this.sizes.clear();
    }
}