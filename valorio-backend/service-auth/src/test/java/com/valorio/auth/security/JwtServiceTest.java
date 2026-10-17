package com.valorio.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "super-secret-key-that-is-at-least-32-bytes-long-for-hmac256!",
                15,
                new ObjectMapper()
        );
        testUser = new User(
                42L,
                "test@valorio.tn",
                "hash",
                "Ben Salah",
                "Amine",
                Role.USER,
                true,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("Génère un jeton JWT valide et extrait les revendications")
    void genereTokenEtExtraitClaims() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@valorio.tn");
        assertThat(jwtService.extractRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Rejette un jeton falsifié ou altéré")
    void rejetteTokenAltere() {
        String token = jwtService.generateAccessToken(testUser);
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "extra." + parts[2];

        assertThat(jwtService.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("Rejette un jeton expiré")
    void rejetteTokenExpire() {
        JwtService expiredJwtService = new JwtService(
                "super-secret-key-that-is-at-least-32-bytes-long-for-hmac256!",
                -1, // Déjà expiré
                new ObjectMapper()
        );
        String expiredToken = expiredJwtService.generateAccessToken(testUser);

        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("Rejette un jeton malformé (format non-JWT ou chaîne aléatoire)")
    void rejetteTokenMalforme() {
        assertThat(jwtService.validateToken("ceci-n-est-pas-un-jwt")).isFalse();
        assertThat(jwtService.validateToken("header.sansSignature")).isFalse();
        assertThat(jwtService.validateToken(null)).isFalse();
        assertThat(jwtService.validateToken("")).isFalse();
    }
}
