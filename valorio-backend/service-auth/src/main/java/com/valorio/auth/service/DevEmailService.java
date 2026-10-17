package com.valorio.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DevEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DevEmailService.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("[EMAIL DEV] Envoi du lien de réinitialisation de mot de passe à {} : http://localhost:4200/reset-password?token={}",
                toEmail, token);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        log.info("[EMAIL DEV] Envoi du lien de confirmation d'email à {} : http://localhost:4200/verify-email?token={}",
                toEmail, token);
    }
}
