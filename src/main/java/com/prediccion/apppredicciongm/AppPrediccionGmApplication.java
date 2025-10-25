package com.prediccion.apppredicciongm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del sistema de predicción.
 * 
 * Configura la aplicación Spring Boot con:
 * - Soporte para Spring Data Web con DTOs (evita exponer entidades JPA)
 * - Programación de tareas asincrónicas
 * 
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableScheduling
public class AppPrediccionGmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppPrediccionGmApplication.class, args);
    }
}
