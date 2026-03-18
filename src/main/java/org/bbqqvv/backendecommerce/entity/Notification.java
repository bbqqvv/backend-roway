package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@EqualsAndHashCode(callSuper = false, exclude = {"user"})
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;
    private boolean isRead = false;
}

