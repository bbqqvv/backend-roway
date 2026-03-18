package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "discount_products")
@ToString
@EqualsAndHashCode(callSuper = false)
public class DiscountProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public DiscountProduct(Discount discount, Product product) {
        this.discount = discount;
        this.product = product;
    }
}
