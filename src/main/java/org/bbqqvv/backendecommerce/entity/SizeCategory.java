package org.bbqqvv.backendecommerce.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "size_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"category"})
@EqualsAndHashCode(callSuper = false, exclude = {"category"})
public class SizeCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
