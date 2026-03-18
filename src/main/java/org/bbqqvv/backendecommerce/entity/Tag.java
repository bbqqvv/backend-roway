package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"products"})
@EqualsAndHashCode(callSuper = false, exclude = {"products"})
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products;
    // ✅ Thêm constructor nhận name
    public Tag(String name) {
        this.name = name;
    }
}
