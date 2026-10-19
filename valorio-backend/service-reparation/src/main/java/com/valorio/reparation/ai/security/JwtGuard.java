package com.valorio.reparation.ai.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gardien d'accès du diagnostic IA. Vérifie localement la signature HMAC-SHA256
 * et l'expiration du jeton JWT émis par le service-auth (même secret partagé),
 * puis n'autorise que les acteurs REPARATEUR (ou ADMIN). Le jeton est transmis
 * dans l'en-tête {@code Authorization: Bearer <jwt>}.
 */
@Component
public class JwtGuard {

    private static final List<String> ACTEURS_AUTORISES = List.of("REPARATEUR", "ADMIN");
    private static final String PREFIXE_BEARER = "Bearer ";

    private final byte[] secretKeyBytes;
    private final ObjectMapper objectMapper;

    public JwtGuard(
            @Value("${valorio.jwt.secret:valorio-super-secret-jwt-signing-key-minimum-32-chars!}") String secret,
            ObjectMapper objectMapper) {
        this.secretKeyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    public UtilisateurConnecte exigerReparateur(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(PREFIXE_BEARER)) {
            throw new AccesNonAutoriseException("Jeton d'authentification manquant.");
        }
        Map<String, Object> claims = verifier(authorizationHeader.substring(PREFIXE_BEARER.length()).trim());
        if (claims == null) {
            throw new AccesNonAutoriseException("Jeton d'authentification invalide ou expiré.");
        }
        String acteur = acteur(claims);
        if (!ACTEURS_AUTORISES.contains(acteur)) {
            throw new AccesInterditException("Diagnostic IA réservé aux réparateurs authentifiés.");
        }
        return new UtilisateurConnecte(identifiant(claims), acteur);
    }

    private Map<String, Object> verifier(String token) {
        if (token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        if (!MessageDigest.isEqual(
                signer(parts[0] + "." + parts[1]).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                parts[2].getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payload, PAYLOAD_TYPE);
            Number expiration = claims.get("exp") instanceof Number nombre ? nombre : null;
            if (expiration == null || expiration.longValue() < Instant.now().getEpochSecond()) {
                return null;
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    private String acteur(Map<String, Object> claims) {
        Object acteur = claims.get("acteur");
        if (acteur != null) {
            return acteur.toString();
        }
        Object role = claims.get("role");
        return role == null ? "" : role.toString();
    }

    private Long identifiant(Map<String, Object> claims) {
        Object sub = claims.get("sub");
        if (sub == null) {
            return null;
        }
        try {
            return Long.valueOf(sub.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String signer(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKeyBytes, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la signature HMAC-SHA256", e);
        }
    }

    private static final com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>> PAYLOAD_TYPE =
            new com.fasterxml.jackson.core.type.TypeReference<>() {
            };
}
