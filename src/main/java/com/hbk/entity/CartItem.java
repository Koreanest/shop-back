package com.hbk.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_item_cart_sku", columnNames = {"cart_id", "sku_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(nullable = false)
    private Integer quantity;

    public void changeQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public void increaseQuantity(Integer amount) {
        if (amount == null || amount < 1) {
            throw new IllegalArgumentException("증가 수량은 1 이상이어야 합니다.");
        }
        this.quantity += amount;
    }
}