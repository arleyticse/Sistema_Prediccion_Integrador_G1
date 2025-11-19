package com.prediccion.apppredicciongm.auth.repository;

import com.prediccion.apppredicciongm.auth.models.RefreshToken;
import com.prediccion.apppredicciongm.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUsuario(Usuario usuario);
}
