package org.bbqqvv.backendecommerce.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum Role implements GrantedAuthority {
    ROLE_USER("USER"),
    ROLE_STAFF("STAFF"),
    ROLE_ADMIN("ADMIN");
    private final String value;
    Role(String value) {
        this.value = value;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
