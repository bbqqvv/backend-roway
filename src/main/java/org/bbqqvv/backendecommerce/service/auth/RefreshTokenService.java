package org.bbqqvv.backendecommerce.service.auth;

import org.bbqqvv.backendecommerce.entity.RefreshToken;
import org.bbqqvv.backendecommerce.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(User user);
    RefreshToken rotateToken(RefreshToken oldToken);
}
