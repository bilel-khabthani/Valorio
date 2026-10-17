package com.valorio.auth.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Primary
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String frontendUrl;

    public SmtpEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:lemonselmi@gmail.com}") String fromEmail,
            @Value("${valorio.frontend.url:http://localhost:4200}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        log.info("[EMAIL GMAIL] Préparation de l'envoi de réinitialisation à {} (expéditeur: {})", toEmail, fromEmail);
        
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail, "ValorIO Plateforme");
                helper.setTo(toEmail);
                helper.setSubject("ValorIO — Réinitialisation de votre mot de passe");

                String html = """
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;">
                            <div style="text-align: center; margin-bottom: 24px;">
                                <h2 style="color: #4338ca; margin: 0;">ValorIO</h2>
                                <p style="color: #64748b; font-size: 14px; margin: 4px 0 0 0;">Économie Circulaire, Réparation, Don & Recyclage</p>
                            </div>
                            <h3 style="color: #1e293b; margin-top: 0;">Réinitialisation de votre mot de passe</h3>
                            <p style="color: #334155; line-height: 1.6;">Bonjour,</p>
                            <p style="color: #334155; line-height: 1.6;">Une demande de réinitialisation de mot de passe a été initiée pour votre compte ValorIO.</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #4f46e5; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; display: inline-block;">
                                    Réinitialiser mon mot de passe
                                </a>
                            </div>
                            <p style="color: #64748b; font-size: 13px; line-height: 1.5;">
                                Ce lien est strictement personnel et expire dans <strong>15 minutes</strong>.<br/>
                                Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email en toute sécurité.
                            </p>
                            <hr style="border: 0; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                            <p style="color: #94a3b8; font-size: 12px; text-align: center; margin: 0;">
                                Cet email a été envoyé automatiquement par la plateforme ValorIO via Gmail SMTP.
                            </p>
                        </div>
                        """.formatted(resetUrl);

                helper.setText(html, true);
                mailSender.send(message);
                log.info("[EMAIL GMAIL] Email de réinitialisation envoyé avec SUCCÈS à {}", toEmail);
            } catch (Exception e) {
                log.error("[EMAIL GMAIL] Erreur lors de l'envoi de l'email à {} : {}", toEmail, e.getMessage(), e);
            }
        });
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        log.info("[EMAIL GMAIL] Préparation de l'envoi de confirmation à {} (expéditeur: {})", toEmail, fromEmail);

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail, "ValorIO Plateforme");
                helper.setTo(toEmail);
                helper.setSubject("ValorIO — Confirmation de votre compte");

                String html = """
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;">
                            <div style="text-align: center; margin-bottom: 24px;">
                                <h2 style="color: #4338ca; margin: 0;">ValorIO</h2>
                                <p style="color: #64748b; font-size: 14px; margin: 4px 0 0 0;">Économie Circulaire, Réparation, Don & Recyclage</p>
                            </div>
                            <h3 style="color: #1e293b; margin-top: 0;">Bienvenue sur ValorIO !</h3>
                            <p style="color: #334155; line-height: 1.6;">Bonjour,</p>
                            <p style="color: #334155; line-height: 1.6;">Merci pour votre inscription. Veuillez valider votre adresse email pour activer pleinement votre compte :</p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #10b981; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; display: inline-block;">
                                    Confirmer mon adresse email
                                </a>
                            </div>
                            <p style="color: #64748b; font-size: 13px; line-height: 1.5;">
                                Ce lien de confirmation est valable pendant <strong>24 heures</strong>.
                            </p>
                            <hr style="border: 0; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                            <p style="color: #94a3b8; font-size: 12px; text-align: center; margin: 0;">
                                Cet email a été envoyé automatiquement par la plateforme ValorIO via Gmail SMTP.
                            </p>
                        </div>
                        """.formatted(verifyUrl);

                helper.setText(html, true);
                mailSender.send(message);
                log.info("[EMAIL GMAIL] Email de confirmation envoyé avec SUCCÈS à {}", toEmail);
            } catch (Exception e) {
                log.error("[EMAIL GMAIL] Erreur lors de l'envoi de la confirmation à {} : {}", toEmail, e.getMessage(), e);
            }
        });
    }

}
