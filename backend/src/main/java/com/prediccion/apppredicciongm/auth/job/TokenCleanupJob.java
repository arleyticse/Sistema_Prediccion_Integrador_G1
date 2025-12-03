package com.prediccion.apppredicciongm.auth.job;

import com.prediccion.apppredicciongm.auth.repository.IPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Job programado para limpieza automática de tokens de recuperación expirados.
 * 
 * Se ejecuta cada hora para eliminar tokens que ya no son válidos,
 * manteniendo la tabla limpia y evitando acumulación de registros.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final IPasswordResetTokenRepository tokenRepository;

    /**
     * Limpia tokens expirados cada hora.
     * Cron: segundo minuto hora día-mes mes día-semana
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Iniciando limpieza de tokens de recuperación expirados...");
        
        try {
            int deleted = tokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
            
            if (deleted > 0) {
                log.info("Limpieza completada: {} tokens expirados eliminados", deleted);
            } else {
                log.debug("No hay tokens expirados para eliminar");
            }
        } catch (Exception e) {
            log.error("Error durante limpieza de tokens: {}", e.getMessage(), e);
        }
    }
}
