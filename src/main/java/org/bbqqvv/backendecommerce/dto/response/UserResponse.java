package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
package org.bbqqvv.backendecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String name;
    String email;
    String avatar;
    String phoneNumber;
    String bio;
    RoleResponse authorities; // Single RoleResponse

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    public UserResponse(String authorities) {
    }
}
