package com.valorio.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordSecurityTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Test
    @DisplayName("BCrypt hache le mot de passe avec un sel sécurisé (longueur et préfixe $2a$12)")
    void hacheMotDePasseAvecBCrypt() {
        String rawPassword = "ComplexPassword2026!#";
        String hash = passwordEncoder.encode(rawPassword);

        assertThat(hash).isNotBlank();
        assertThat(hash).startsWith("$2a$12$");
        assertThat(hash).isNotEqualTo(rawPassword);
        assertThat(hash.length()).isEqualTo(60);
    }

    @Test
    @DisplayName("Vérifie avec succès le mot de passe en clair par rapport au haché")
    void verifieMotDePasseCorrect() {
        String rawPassword = "ComplexPassword2026!#";
        String hash = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(rawPassword, hash);
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("Rejette tout mot de passe incorrect par rapport au haché")
    void rejetteMotDePasseIncorrect() {
        String rawPassword = "ComplexPassword2026!#";
        String hash = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches("WrongPassword123!", hash);
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("Génère des sels uniques : deux hachages du même mot de passe sont différents")
    void deuxHachagesDuMemeMotDePasseOntDesSelsDifferents() {
        String rawPassword = "ComplexPassword2026!#";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();
    }
}
