package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuración del scheduler para tareas programadas de alertas de inventario.
 * 
 * Habilita la programación de tareas automáticas (@Scheduled) y configura
 * un pool de threads dedicado para la ejecución de jobs.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-06
 */
@Configuration
@EnableScheduling
public class JobSchedulerConfig {

    /**
     * Pool de threads: 5 threads concurrentes para jobs de alertas.
     */
    private static final int POOL_SIZE = 5;

    /**
     * Configura el TaskScheduler para ejecutar tareas programadas.
     * 
     * Features:
     * - Pool size: 5 threads
     * - Daemon threads: true (se detienen al cerrar la app)
     * - Thread name prefix: "AlertaJob-"
     * - Wait for tasks on shutdown: true
     * - Await termination: 60 segundos
     * 
     * @return TaskScheduler configurado
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Configuración del pool
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("AlertaJob-");
        scheduler.setDaemon(true);
        
        // Configuración de shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        
        // Inicializar
        scheduler.initialize();
        
        return scheduler;
    }
}
