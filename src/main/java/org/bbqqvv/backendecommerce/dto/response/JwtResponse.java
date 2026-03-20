package org.bbqqvv.backendecommerce.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String refreshToken;

    public JwtResponse(String token) {
        this.token = token;
    }
}

