package org.bbqqvv.backendecommerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionsUpdateRequest {
    Set<String> permissions;
}
