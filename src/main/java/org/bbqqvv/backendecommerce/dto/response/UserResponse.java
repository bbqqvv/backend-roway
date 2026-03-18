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
    String avatarUrl;
    String bio;
    Set<RoleResponse> authorities;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    public UserResponse(String authorities) {
    }
}
