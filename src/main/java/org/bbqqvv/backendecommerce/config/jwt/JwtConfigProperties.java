package org.bbqqvv.backendecommerce.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ConfigurationProperties(prefix = "application.security.jwt")
@Component
public class JwtConfigProperties {

    private String secretKey;
    private long expiration;
    private long refreshExpiration;

}
