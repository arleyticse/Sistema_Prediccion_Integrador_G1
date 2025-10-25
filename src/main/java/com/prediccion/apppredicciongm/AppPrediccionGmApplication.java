package com.prediccion.apppredicciongm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del sistema de predicción de gestión de materiales.
 * 
 * Configura la aplicación Spring Boot con:
 * - Soporte para Spring Data Web con DTOs (evita exponer entidades JPA)
 * - Programación de tareas asincrónicas
 * - Interfaz Swing para la consola administrativa
 * 
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableScheduling
public class AppPrediccionGmApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppPrediccionGmApplication.class)
                .headless(false)
                .run(args);
    }

    /**
     * Inicia la interfaz gráfica Swing cuando la aplicación está lista.
     * 
     * Aplica el Look and Feel "Nimbus" si está disponible y muestra
     * la ventana principal de administración.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void launchSwingUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}

            com.prediccion.apppredicciongm.view.App frame = new com.prediccion.apppredicciongm.view.App();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
