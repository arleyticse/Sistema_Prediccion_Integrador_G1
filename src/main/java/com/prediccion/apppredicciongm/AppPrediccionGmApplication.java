package com.prediccion.apppredicciongm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)// Configura la serialización de Page para usar DTOs permiter paginación directa en controladores para no exponer entidades JPA
public class AppPrediccionGmApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AppPrediccionGmApplication.class)
                .headless(false)
                .run(args);
    }

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

            // Mostrar tu JFrame
            com.prediccion.apppredicciongm.view.App frame = new com.prediccion.apppredicciongm.view.App();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
