package com.prediccion.apppredicciongm.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Servicio para envío de correos electrónicos mediante JavaMail y Thymeleaf.
 * 
 * Utiliza plantillas HTML ubicadas en resources/templates/email/
 * para generar correos con diseño profesional y mantenible.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;
    private final int otpExpirationMinutes;

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${spring.mail.username}") String fromEmail,
            @Value("${otp.expiration.minutes:10}") int otpExpirationMinutes) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
        this.otpExpirationMinutes = otpExpirationMinutes;
    }

    /**
     * Envía un código OTP para recuperación de contraseña.
     * Plantilla: templates/email/password-reset.html
     */
    public boolean sendPasswordResetEmail(String toEmail, String code, String userName) {
        log.info("Iniciando envío de email de recuperación a: {}", toEmail);
        
        Context context = new Context();
        context.setVariable("greeting", userName != null ? "Hola " + userName + "," : "Hola,");
        context.setVariable("code", code);
        context.setVariable("expiryMinutes", otpExpirationMinutes);
        
        return sendEmail(
            toEmail,
            "Recuperación de Contraseña - Código de Verificación",
            "email/password-reset",
            context
        );
    }

    /**
     * Envía un código OTP para desbloquear cuenta.
     * Plantilla: templates/email/account-unlock.html
     */
    public boolean sendAccountUnlockEmail(String toEmail, String code, String userName) {
        log.info("Iniciando envío de email de desbloqueo a: {}", toEmail);
        
        Context context = new Context();
        context.setVariable("greeting", userName != null ? "Hola " + userName + "," : "Hola,");
        context.setVariable("code", code);
        context.setVariable("expiryMinutes", otpExpirationMinutes);
        
        return sendEmail(
            toEmail,
            "Desbloqueo de Cuenta - Código de Verificación",
            "email/account-unlock",
            context
        );
    }

    /**
     * Método genérico para enviar emails usando plantillas Thymeleaf.
     */
    private boolean sendEmail(String toEmail, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Global Market S.A.C");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.debug("Configuración de email - From: {}, To: {}, Template: {}", fromEmail, toEmail, templateName);
            
            mailSender.send(message);
            log.info("Email enviado exitosamente a: {} (template: {})", toEmail, templateName);
            return true;

        } catch (MessagingException e) {
            log.error("MessagingException al enviar email a {}: {}", toEmail, e.getMessage(), e);
            return false;
        } catch (MailException e) {
            log.error("MailException al enviar email a {}: {}", toEmail, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al enviar email a {}: {} - {}", toEmail, e.getClass().getSimpleName(), e.getMessage(), e);
            return false;
        }
    }
}
