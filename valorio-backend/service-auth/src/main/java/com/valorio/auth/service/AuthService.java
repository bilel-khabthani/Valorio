package com.valorio.auth.service;

import com.valorio.auth.domain.EmailVerificationToken;
import com.valorio.auth.domain.PasswordResetToken;
import com.valorio.auth.domain.RefreshToken;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import com.valorio.auth.dto.AuthResponse;
import com.valorio.auth.dto.ForgotPasswordRequest;
import com.valorio.auth.dto.LoginRequest;
import com.valorio.auth.dto.MessageResponse;
import com.valorio.auth.dto.RegisterRequest;
import com.valorio.auth.dto.ResetPasswordRequest;
import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.repo.TokenRepository;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    public static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final long refreshTokenDays;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthService(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            @Value("${valorio.jwt.refresh-token-expiration-days:7}") long refreshTokenDays,
            @Value("${valorio.security.cookie-secure:false}") boolean cookieSecure,
            @Value("${valorio.security.cookie-same-site:Lax}") String cookieSameSite
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.refreshTokenDays = refreshTokenDays;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
        if (!request.termsAccepted()) {
            throw new IllegalArgumentException("Vous devez accepter les conditions générales d'utilisation");
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Un compte existe déjà avec cette adresse email");
        }

        User newUser = new User(
                null,
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.nom().trim(),
                request.prenom().trim(),
                Role.USER, // Toujours USER par défaut pour la sécurité
                true,
                false, // Nécessite vérification optionnelle
                null,
                null
        );

        User savedUser = userRepository.save(newUser);

        // Création du token de vérification d'email
        String rawVerifToken = UUID.randomUUID().toString();
        tokenRepository.saveEmailVerificationToken(
                savedUser.id(),
                hashToken(rawVerifToken),
                OffsetDateTime.now().plusHours(24)
        );
        emailService.sendVerificationEmail(savedUser.email(), rawVerifToken);

        // Génération session
        return createSessionAndTokens(savedUser, 15, response);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.password(), userOpt.get().passwordHash())) {
            throw new BadCredentialsException("Adresse email ou mot de passe incorrect");
        }

        User user = userOpt.get();
        if (!user.actif()) {
            throw new IllegalStateException("Ce compte a été désactivé. Veuillez contacter l'administrateur.");
        }

        int sessionDays = request.rememberMe() ? 30 : (int) refreshTokenDays;
        return createSessionAndTokens(user, sessionDays, response);
    }

    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadCredentialsException("Jeton de rafraîchissement absent");
        }

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken record = tokenRepository.findRefreshToken(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Session invalide ou expirée"));

        if (!record.estValide()) {
            // Détection de réutilisation potentielle de token révoqué
            tokenRepository.revokeAllUserRefreshTokens(record.userId());
            throw new BadCredentialsException("Session expirée ou révoquée");
        }

        // Révocation du token précédent (rotation)
        tokenRepository.revokeRefreshToken(tokenHash);

        User user = userRepository.findById(record.userId())
                .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

        if (!user.actif()) {
            throw new IllegalStateException("Compte utilisateur inactif");
        }

        return createSessionAndTokens(user, (int) refreshTokenDays, response);
    }

    @Transactional
    public void logout(String rawRefreshToken, HttpServletResponse response) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            tokenRepository.revokeRefreshToken(hashToken(rawRefreshToken));
        }
        clearRefreshCookie(response);
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        return UserResponse.fromDomain(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        String devLink = null;
        // Réponse générique pour éviter l'énumération des utilisateurs
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String rawResetToken = UUID.randomUUID().toString();
            tokenRepository.savePasswordResetToken(
                    user.id(),
                    hashToken(rawResetToken),
                    OffsetDateTime.now().plusMinutes(15) // Valide 15 minutes
            );
            emailService.sendPasswordResetEmail(user.email(), rawResetToken);
            devLink = "/reset-password?token=" + rawResetToken;
        }

        return MessageResponse.of("Si cette adresse email existe, un lien de réinitialisation vous a été envoyé.", devLink);
    }


    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        String tokenHash = hashToken(request.token().trim());
        PasswordResetToken record = tokenRepository.findPasswordResetToken(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Jeton de réinitialisation invalide ou expiré"));

        if (!record.estValide()) {
            throw new IllegalArgumentException("Jeton de réinitialisation invalide ou expiré");
        }

        // Mise à jour mot de passe
        userRepository.updatePassword(record.userId(), passwordEncoder.encode(request.newPassword()));

        // Invalidation du jeton
        tokenRepository.markPasswordResetTokenUsed(tokenHash);

        // Révocation de toutes les sessions actives existantes par sécurité
        tokenRepository.revokeAllUserRefreshTokens(record.userId());

        return MessageResponse.of("Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
    }

    @Transactional
    public MessageResponse verifyEmail(String rawToken) {
        String tokenHash = hashToken(rawToken.trim());
        EmailVerificationToken record = tokenRepository.findEmailVerificationToken(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Jeton de vérification invalide ou expiré"));

        if (!record.estValide()) {
            throw new IllegalArgumentException("Jeton de vérification invalide ou déjà utilisé");
        }

        userRepository.verifyEmail(record.userId());
        tokenRepository.markEmailVerificationTokenUsed(tokenHash);

        return MessageResponse.of("Votre adresse email a été confirmée avec succès !");
    }

    @Transactional
    public MessageResponse resendVerification(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        String devLink = null;
        if (userOpt.isPresent() && !userOpt.get().emailVerifie()) {
            User user = userOpt.get();
            String rawVerifToken = UUID.randomUUID().toString();
            tokenRepository.saveEmailVerificationToken(
                    user.id(),
                    hashToken(rawVerifToken),
                    OffsetDateTime.now().plusHours(24)
            );
            emailService.sendVerificationEmail(user.email(), rawVerifToken);
            devLink = "/verify-email?token=" + rawVerifToken;
        }

        return MessageResponse.of("Si ce compte requiert une validation, un email a été réexpédié.", devLink);

    }

    private AuthResponse createSessionAndTokens(User user, int daysValid, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);

        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(daysValid);

        tokenRepository.saveRefreshToken(user.id(), tokenHash, expiresAt);

        setRefreshCookie(rawRefreshToken, (int) (daysValid * 86400L), response);

        long expiresIn = 15 * 60; // 15 minutes en secondes
        return AuthResponse.of(accessToken, expiresIn, UserResponse.fromDomain(user));
    }

    private void setRefreshCookie(String token, int maxAgeSeconds, HttpServletResponse response) {
        if (response == null) {
            return;
        }
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(REFRESH_COOKIE_NAME).append("=").append(token).append(";");
        cookieHeader.append(" Path=/api/auth;");
        cookieHeader.append(" Max-Age=").append(maxAgeSeconds).append(";");
        cookieHeader.append(" HttpOnly;");
        cookieHeader.append(" SameSite=").append(cookieSameSite).append(";");
        if (cookieSecure) {
            cookieHeader.append(" Secure;");
        }
        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(REFRESH_COOKIE_NAME).append("=;");
        cookieHeader.append(" Path=/api/auth;");
        cookieHeader.append(" Max-Age=0;");
        cookieHeader.append(" HttpOnly;");
        cookieHeader.append(" SameSite=").append(cookieSameSite).append(";");
        if (cookieSecure) {
            cookieHeader.append(" Secure;");
        }
        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    public static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 introuvable", e);
        }
    }
}
