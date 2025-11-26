package com.prediccion.apppredicciongm.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de correos electrónicos mediante JavaMail (SMTP).
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Envía un código OTP al email especificado
     * @throws UnsupportedEncodingException 
     */
    public boolean sendPasswordResetEmail(String toEmail, String code, String userName) throws UnsupportedEncodingException {
        try {
            String htmlContent = buildPasswordResetEmailTemplate(code, userName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Global Market S.A.C");
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de Contraseña - Código de Verificación");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de recuperación enviado exitosamente a: {}", toEmail);
            return true;

        } catch (MessagingException | MailException e) {
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

    /**
     * Envía un código OTP para desbloquear la cuenta
     */
    public boolean sendAccountUnlockEmail(String toEmail, String code, String userName) {
        try {
            String htmlContent = buildAccountUnlockEmailTemplate(code, userName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Global Market S.A.C");
            helper.setTo(toEmail);
            helper.setSubject("Desbloqueo de Cuenta - Código de Verificación");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de desbloqueo enviado exitosamente a: {}", toEmail);
            return true;

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Error al enviar email de desbloqueo a {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Construye el template HTML para el email de desbloqueo de cuenta
     */
    private String buildAccountUnlockEmailTemplate(String code, String userName) {
        String greeting = userName != null ? "Hola " + userName + "," : "Hola,";
        
        return "<!DOCTYPE html>\n"
            + "<html lang=\"es\">\n"
            + "<head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "    <title>Desbloqueo de Cuenta</title>\n"
            + "    <style>\n"
            + "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa; margin: 0; padding: 0; }\n"
            + "        .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }\n"
            + "        .header { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); padding: 40px 30px; text-align: center; color: #ffffff; }\n"
            + "        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }\n"
            + "        .content { padding: 40px 30px; }\n"
            + "        .greeting { font-size: 18px; color: #333333; margin-bottom: 20px; }\n"
            + "        .message { font-size: 16px; color: #666666; line-height: 1.6; margin-bottom: 30px; }\n"
            + "        .alert-box { background-color: #fdf2f2; border: 1px solid #f5c6cb; border-left: 4px solid #e74c3c; border-radius: 8px; padding: 20px; margin: 20px 0; }\n"
            + "        .alert-box strong { color: #c0392b; }\n"
            + "        .code-container { background-color: #f8f9fa; border: 2px dashed #e74c3c; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0; }\n"
            + "        .code { font-size: 42px; font-weight: bold; color: #e74c3c; letter-spacing: 8px; font-family: 'Courier New', monospace; }\n"
            + "        .code-label { font-size: 14px; color: #666666; margin-top: 10px; }\n"
            + "        .security-tips { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; font-size: 14px; color: #856404; }\n"
            + "        .expiry { font-size: 14px; color: #999999; text-align: center; margin-top: 20px; }\n"
            + "        .footer { background-color: #f8f9fa; padding: 20px 30px; text-align: center; font-size: 13px; color: #999999; border-top: 1px solid #e9ecef; }\n"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "    <div class=\"container\">\n"
            + "        <div class=\"header\">\n"
            + "            <h1>🔓 Desbloqueo de Cuenta</h1>\n"
            + "        </div>\n"
            + "        <div class=\"content\">\n"
            + "            <div class=\"greeting\">" + greeting + "</div>\n"
            + "            <div class=\"alert-box\">\n"
            + "                <strong>⚠️ Tu cuenta ha sido bloqueada</strong><br><br>\n"
            + "                Esto ocurrió porque se detectaron múltiples intentos fallidos de inicio de sesión. \n"
            + "                Esta medida protege tu cuenta de accesos no autorizados.\n"
            + "            </div>\n"
            + "            <div class=\"message\">\n"
            + "                Para desbloquear tu cuenta, ingresa el siguiente código en la página de desbloqueo:\n"
            + "            </div>\n"
            + "            <div class=\"code-container\">\n"
            + "                <div class=\"code\">" + code + "</div>\n"
            + "                <div class=\"code-label\">Código de desbloqueo</div>\n"
            + "            </div>\n"
            + "            <div class=\"security-tips\">\n"
            + "                <strong>💡 ¿No fuiste tú?</strong><br>\n"
            + "                Si no reconoces estos intentos de acceso, te recomendamos cambiar tu contraseña \n"
            + "                inmediatamente después de desbloquear tu cuenta.\n"
            + "            </div>\n"
            + "            <div class=\"expiry\">\n"
            + "                ⏰ Este código expirará en <strong>10 minutos</strong>\n"
            + "            </div>\n"
            + "        </div>\n"
            + "        <div class=\"footer\">\n"
            + "            <p>Sistema de Predicción de Inventario - Global Market S.A.C</p>\n"
            + "            <p>Este es un correo automático, por favor no respondas a este mensaje.</p>\n"
            + "        </div>\n"
            + "    </div>\n"
            + "</body>\n"
            + "</html>";
    }
}
