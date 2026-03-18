package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "recently_viewed_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentlyViewedProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
}

