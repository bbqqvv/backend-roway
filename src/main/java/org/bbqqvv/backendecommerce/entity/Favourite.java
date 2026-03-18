package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "favourites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favourite extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

