package org.bbqqvv.backendecommerce.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@EqualsAndHashCode(callSuper = false, exclude = {"user"})
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String recipientName;

    @Column
    private String country;

    @Column
    private String province;

    @Column
    private String district;

    @Column
    private String email;

    @Column
    private String note;

    @Column
    private String commune;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_default")
    private boolean defaultAddress;

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s, %s",
                addressLine != null ? addressLine : "",
                commune != null ? commune : "",
                district != null ? district : "",
                province != null ? province : "",
                country != null ? country : "");
    }

}

