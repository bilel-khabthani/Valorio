package com.valorio.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.domain.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final byte[] secretKeyBytes;
    private final long expirationMinutes;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${valorio.jwt.secret}") String secret,
            @Value("${valorio.jwt.access-token-expiration-minutes:15}") long expirationMinutes,
            ObjectMapper objectMapper
    ) {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
        this.objectMapper = objectMapper;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.id().toString());
        payload.put("email", user.email());
        payload.put("role", user.role().name());
        payload.put("nom", user.nom());
        payload.put("prenom", user.prenom());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", exp.getEpochSecond());

        return createSignedToken(payload);
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> claims = parseClaims(token);
            if (claims == null) {
                return false;
            }
            Number exp = (Number) claims.get("exp");
            if (exp == null) {
                return false;
            }
            return Instant.now().getEpochSecond() < exp.longValue();
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> parseClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        String dataToSign = parts[0] + "." + parts[1];
        String expectedSignature = sign(dataToSign);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        Map<String, Object> claims = parseClaims(token);
        if (claims != null && claims.get("sub") != null) {
            return Long.valueOf(claims.get("sub").toString());
        }
        return null;
    }

    public String extractEmail(String token) {
        Map<String, Object> claims = parseClaims(token);
        if (claims != null && claims.get("email") != null) {
            return (String) claims.get("email");
        }
        return null;
    }

    public String extractRole(String token) {
        Map<String, Object> claims = parseClaims(token);
        if (claims != null && claims.get("role") != null) {
            return (String) claims.get("role");
        }
        return null;
    }

    private String createSignedToken(Map<String, Object> payload) {
        try {
            Map<String, String> header = Map.of("alg", "HS256", "typ", "JWT");
            String headerJson = objectMapper.writeValueAsString(header);
            String payloadJson = objectMapper.writeValueAsString(payload);

            String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String content = encodedHeader + "." + encodedPayload;
            String signature = sign(content);

            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le token JWT", e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signed = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signed);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la signature HMAC-SHA256", e);
        }
    }
}
