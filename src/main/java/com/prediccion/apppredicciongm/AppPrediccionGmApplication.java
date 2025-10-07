package com.prediccion.apppredicciongm;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class AppPrediccionGmApplication {

    public static void main(String[] args) {
        // Desactivar headless para permitir UI Swing
        new SpringApplicationBuilder(AppPrediccionGmApplication.class)
                .headless(false)
                .run(args);
    }

    // Abrir el JFrame cuando Spring termine de arrancar
    @EventListener(ApplicationReadyEvent.class)
    public void launchSwingUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Opcional: Look & Feel Nimbus
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
