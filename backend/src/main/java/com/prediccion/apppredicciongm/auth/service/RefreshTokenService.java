package com.prediccion.apppredicciongm.auth.service;

import com.prediccion.apppredicciongm.auth.models.RefreshToken;
import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.auth.repository.RefreshTokenRepository;
import com.prediccion.apppredicciongm.models.Usuario;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        // Eliminar tokens anteriores del usuario para mantener solo uno activo
        refreshTokenRepository.deleteByUsuario(usuario);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .token(UUID.randomUUID().toString())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("El Refresh Token ha expirado. Por favor inicie sesión nuevamente.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        refreshTokenRepository.deleteByUsuario(usuario);
    }
}
