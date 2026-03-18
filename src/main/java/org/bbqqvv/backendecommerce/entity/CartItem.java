package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private String color;
    @Column(nullable = false)
    private String sizeName;
    @Column(nullable = false)
    private BigDecimal subtotal;
    @Column(nullable = false)
    private BigDecimal price;

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
