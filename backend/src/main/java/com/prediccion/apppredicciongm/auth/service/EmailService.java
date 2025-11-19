package com.prediccion.apppredicciongm.auth.service;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de correos electrónicos usando Resend API.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Service
@Slf4j
public class EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    /**
     * Envía un código OTP al email especificado
     */
    public boolean sendPasswordResetEmail(String toEmail, String code, String userName) {
        try {
            String htmlContent = buildPasswordResetEmailTemplate(code, userName);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Recuperación de Contraseña - Código de Verificación")
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            
            log.info("Email de recuperación enviado exitosamente a: {} - ID: {}", toEmail, response.getId());
            return true;

        } catch (ResendException e) {
            log.error("Error al enviar email de recuperación a {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Construye el template HTML para el email de recuperación
     */
    private String buildPasswordResetEmailTemplate(String code, String userName) {
        String greeting = userName != null ? "Hola " + userName + "," : "Hola,";
        
        return "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "    <title>Recuperación de Contraseña</title>\n"
            + "    <style>\n"
            + "        body {\n"
            + "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n"
            + "            background-color: #f4f7fa;\n"
            + "            margin: 0;\n"
            + "            padding: 0;\n"
            + "        }\n"
            + "        .container {\n"
            + "            max-width: 600px;\n"
            + "            margin: 40px auto;\n"
            + "            background-color: #ffffff;\n"
            + "            border-radius: 10px;\n"
            + "            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n"
            + "            overflow: hidden;\n"
            + "        }\n"
            + "        .header {\n"
            + "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n"
            + "            padding: 40px 30px;\n"
            + "            text-align: center;\n"
            + "            color: #ffffff;\n"
            + "        }\n"
            + "        .header h1 {\n"
            + "            margin: 0;\n"
            + "            font-size: 28px;\n"
            + "            font-weight: 600;\n"
            + "        }\n"
            + "        .content {\n"
            + "            padding: 40px 30px;\n"
            + "        }\n"
            + "        .greeting {\n"
            + "            font-size: 18px;\n"
            + "            color: #333333;\n"
            + "            margin-bottom: 20px;\n"
            + "        }\n"
            + "        .message {\n"
            + "            font-size: 16px;\n"
            + "            color: #666666;\n"
            + "            line-height: 1.6;\n"
            + "            margin-bottom: 30px;\n"
            + "        }\n"
            + "        .code-container {\n"
            + "            background-color: #f8f9fa;\n"
            + "            border: 2px dashed #667eea;\n"
            + "            border-radius: 8px;\n"
            + "            padding: 30px;\n"
            + "            text-align: center;\n"
            + "            margin: 30px 0;\n"
            + "        }\n"
            + "        .code {\n"
            + "            font-size: 42px;\n"
            + "            font-weight: bold;\n"
            + "            color: #667eea;\n"
            + "            letter-spacing: 8px;\n"
            + "            font-family: 'Courier New', monospace;\n"
            + "        }\n"
            + "        .code-label {\n"
            + "            font-size: 14px;\n"
            + "            color: #666666;\n"
            + "            margin-top: 10px;\n"
            + "        }\n"
            + "        .warning {\n"
            + "            background-color: #fff3cd;\n"
            + "            border-left: 4px solid #ffc107;\n"
            + "            padding: 15px;\n"
            + "            margin: 20px 0;\n"
            + "            font-size: 14px;\n"
            + "            color: #856404;\n"
            + "        }\n"
            + "        .expiry {\n"
            + "            font-size: 14px;\n"
            + "            color: #999999;\n"
            + "            text-align: center;\n"
            + "            margin-top: 20px;\n"
            + "        }\n"
            + "        .footer {\n"
            + "            background-color: #f8f9fa;\n"
            + "            padding: 20px 30px;\n"
            + "            text-align: center;\n"
            + "            font-size: 13px;\n"
            + "            color: #999999;\n"
            + "            border-top: 1px solid #e9ecef;\n"
            + "        }\n"
            + "        .security-tips {\n"
            + "            background-color: #e7f3ff;\n"
            + "            border-left: 4px solid #2196F3;\n"
            + "            padding: 15px;\n"
            + "            margin: 20px 0;\n"
            + "            font-size: 14px;\n"
            + "            color: #0c5460;\n"
            + "        }\n"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "    <div class=\"container\">\n"
            + "        <div class=\"header\">\n"
            + "            <h1>🔐 Recuperación de Contraseña</h1>\n"
            + "        </div>\n"
            + "        \n"
            + "        <div class=\"content\">\n"
            + "            <div class=\"greeting\">\n"
            + "                " + greeting + "\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"message\">\n"
            + "                Hemos recibido una solicitud para restablecer la contraseña de tu cuenta \n"
            + "                en el Sistema de Predicción de Inventario.\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"code-container\">\n"
            + "                <div class=\"code\">" + code + "</div>\n"
            + "                <div class=\"code-label\">Tu código de verificación</div>\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"message\">\n"
            + "                Ingresa este código de 6 dígitos en la página de recuperación de contraseña \n"
            + "                para continuar con el proceso.\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"warning\">\n"
            + "                <strong>⚠️ Importante:</strong> Si no solicitaste este cambio de contraseña, \n"
            + "                ignora este correo. Tu cuenta permanecerá segura.\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"security-tips\">\n"
            + "                <strong>💡 Consejos de seguridad:</strong>\n"
            + "                <ul style=\"margin: 10px 0; padding-left: 20px;\">\n"
            + "                    <li>Nunca compartas este código con nadie</li>\n"
            + "                    <li>Nuestro equipo nunca te pedirá este código</li>\n"
            + "                    <li>Verifica que la URL sea la oficial del sistema</li>\n"
            + "                </ul>\n"
            + "            </div>\n"
            + "            \n"
            + "            <div class=\"expiry\">\n"
            + "                ⏰ Este código expirará en <strong>10 minutos</strong>\n"
            + "            </div>\n"
            + "        </div>\n"
            + "        \n"
            + "        <div class=\"footer\">\n"
            + "            <p>Sistema de Predicción de Inventario</p>\n"
            + "            <p>Este es un correo automático, por favor no respondas a este mensaje.</p>\n"
            + "        </div>\n"
            + "    </div>\n"
            + "</body>\n"
            + "</html>";
    }
}
