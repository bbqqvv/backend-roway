package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "discount_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "discount_id"}) // Đảm bảo mỗi user chỉ có 1 bản ghi với mỗi discount
})@ToString
@EqualsAndHashCode(callSuper = false)
public class DiscountUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "discount_id", nullable = true)
    private Discount discount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    public DiscountUser(Discount discount, User user) {
        this.discount = discount;
        this.user = user;
    }
}
