package org.bbqqvv.backendecommerce.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.JwtConfigProperties;
import org.bbqqvv.backendecommerce.entity.RefreshToken;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.UserErrorCode;
import org.bbqqvv.backendecommerce.repository.RefreshTokenRepository;
import org.bbqqvv.backendecommerce.service.auth.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfigProperties jwtConfigProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        log.info("Creating refresh token for user: {}", user.getUsername());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtConfigProperties.getRefreshExpiration()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user: {}", token.getUser().getUsername());
            refreshTokenRepository.delete(token);
            throw new AppException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (token.isRevoked()) {
            log.warn("Attempt to use revoked refresh token for user: {}", token.getUser().getUsername());
            throw new AppException(UserErrorCode.REFRESH_TOKEN_INVALID);
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        log.info("Rotating refresh token for user: {}", oldToken.getUser().getUsername());
        
        // Mark old token as revoked instead of deleting for security audit trail if needed
        // Or just delete to save space. Here we delete as per standard rotation.
        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        
        return createRefreshToken(user);
    }
}
