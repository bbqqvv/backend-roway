package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 255)
    private String searchQuery;
}

