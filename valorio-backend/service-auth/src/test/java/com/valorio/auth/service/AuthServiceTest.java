package com.valorio.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.domain.Acteur;
import com.valorio.auth.domain.EmailVerificationToken;
import com.valorio.auth.domain.PasswordResetToken;
import com.valorio.auth.domain.RefreshToken;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import com.valorio.auth.dto.AuthResponse;
import com.valorio.auth.dto.LoginRequest;
import com.valorio.auth.dto.MessageResponse;
import com.valorio.auth.dto.RegisterRequest;
import com.valorio.auth.dto.ResetPasswordRequest;
import com.valorio.auth.repo.TokenRepository;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("secret-key-that-has-at-least-32-bytes-length!", 15, new ObjectMapper());
        authService = new AuthService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                jwtService,
                emailService,
                7,
                false,
                "Lax"
        );
    }

    @Test
    @DisplayName("Inscription réussie d'un nouvel utilisateur avec rôle USER")
    void inscriptionReussie() {
        RegisterRequest req = new RegisterRequest(
                "Ben Salah", "Amine", "amine@valorio.tn", "Secret123456!", "Secret123456!", Acteur.UTILISATEUR, true
        );
        HttpServletResponse response = mock(HttpServletResponse.class);

        given(userRepository.existsByEmail("amine@valorio.tn")).willReturn(false);
        given(passwordEncoder.encode("Secret123456!")).willReturn("hashed_pwd");

        User savedUser = new User(1L, "amine@valorio.tn", "hashed_pwd", "Ben Salah", "Amine", Role.USER, true, false, OffsetDateTime.now(), OffsetDateTime.now());
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        AuthResponse res = authService.register(req, response);

        assertThat(res).isNotNull();
        assertThat(res.accessToken()).isNotBlank();
        assertThat(res.user().role()).isEqualTo(Role.USER);
        assertThat(res.user().acteur()).isEqualTo(Acteur.UTILISATEUR);
        assertThat(res.user().email()).isEqualTo("amine@valorio.tn");

        verify(emailService).sendVerificationEmail(eq("amine@valorio.tn"), anyString());
        verify(tokenRepository).saveRefreshToken(eq(1L), anyString(), any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Rejette l'inscription si l'adresse email existe déjà")
    void rejetteEmailExistant() {
        RegisterRequest req = new RegisterRequest(
                "Ben Salah", "Amine", "amine@valorio.tn", "Secret123456!", "Secret123456!", Acteur.UTILISATEUR, true
        );
        given(userRepository.existsByEmail("amine@valorio.tn")).willReturn(true);

        assertThatThrownBy(() -> authService.register(req, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    @DisplayName("Rejette l'inscription si les mots de passe ne correspondent pas")
    void rejetteMotsDePasseDifferents() {
        RegisterRequest req = new RegisterRequest(
                "Ben Salah", "Amine", "amine@valorio.tn", "Secret123456!", "AutreMotDePasse123!", Acteur.UTILISATEUR, true
        );

        assertThatThrownBy(() -> authService.register(req, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne correspondent pas");
    }

    @Test
    @DisplayName("Connexion réussie avec identifiants valides")
    void connexionReussie() {
        LoginRequest req = new LoginRequest("amine@valorio.tn", "Secret123456!", false);
        User user = new User(1L, "amine@valorio.tn", "hashed_pwd", "Ben Salah", "Amine", Role.USER, true, true, OffsetDateTime.now(), OffsetDateTime.now());

        given(userRepository.findByEmail("amine@valorio.tn")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Secret123456!", "hashed_pwd")).willReturn(true);

        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthResponse res = authService.login(req, response);

        assertThat(res.accessToken()).isNotBlank();
        assertThat(res.user().email()).isEqualTo("amine@valorio.tn");
    }

    @Test
    @DisplayName("Rejette la connexion avec de mauvais identifiants")
    void rejetteMauvaisIdentifiants() {
        LoginRequest req = new LoginRequest("amine@valorio.tn", "FauxPassword", false);
        User user = new User(1L, "amine@valorio.tn", "hashed_pwd", "Ben Salah", "Amine", Role.USER, true, true, OffsetDateTime.now(), OffsetDateTime.now());

        given(userRepository.findByEmail("amine@valorio.tn")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("FauxPassword", "hashed_pwd")).willReturn(false);

        assertThatThrownBy(() -> authService.login(req, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Rafraîchit le jeton et effectue une rotation du refresh token")
    void rafraichitJetonAvecRotation() {
        String rawToken = "sample-refresh-uuid";
        String tokenHash = AuthService.hashToken(rawToken);

        RefreshToken tokenRecord = new RefreshToken(10L, 1L, tokenHash, false, OffsetDateTime.now().plusDays(5), OffsetDateTime.now());
        User user = new User(1L, "amine@valorio.tn", "hashed_pwd", "Ben Salah", "Amine", Role.USER, true, true, OffsetDateTime.now(), OffsetDateTime.now());

        given(tokenRepository.findRefreshToken(tokenHash)).willReturn(Optional.of(tokenRecord));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthResponse res = authService.refreshToken(rawToken, response);

        assertThat(res.accessToken()).isNotBlank();
        verify(tokenRepository).revokeRefreshToken(tokenHash);
    }

    @Test
    @DisplayName("Réinitialisation réussie de mot de passe")
    void reinitialisationMotDePasse() {
        String rawToken = "reset-token-sample";
        String tokenHash = AuthService.hashToken(rawToken);

        PasswordResetToken tokenRecord = new PasswordResetToken(5L, 1L, tokenHash, false, OffsetDateTime.now().plusMinutes(10), OffsetDateTime.now());
        given(tokenRepository.findPasswordResetToken(tokenHash)).willReturn(Optional.of(tokenRecord));
        given(passwordEncoder.encode("NouveauMotDePasse123!")).willReturn("nouveau_hash");

        ResetPasswordRequest req = new ResetPasswordRequest(rawToken, "NouveauMotDePasse123!", "NouveauMotDePasse123!");
        MessageResponse res = authService.resetPassword(req);

        assertThat(res.success()).isTrue();
        verify(userRepository).updatePassword(1L, "nouveau_hash");
        verify(tokenRepository).markPasswordResetTokenUsed(tokenHash);
        verify(tokenRepository).revokeAllUserRefreshTokens(1L);
    }

    @Test
    @DisplayName("Confirmation réussie d'adresse email")
    void confirmationEmailReussie() {
        String rawToken = "verif-token-sample";
        String tokenHash = AuthService.hashToken(rawToken);

        EmailVerificationToken tokenRecord = new EmailVerificationToken(8L, 1L, tokenHash, false, OffsetDateTime.now().plusHours(12), OffsetDateTime.now());
        given(tokenRepository.findEmailVerificationToken(tokenHash)).willReturn(Optional.of(tokenRecord));

        MessageResponse res = authService.verifyEmail(rawToken);

        assertThat(res.success()).isTrue();
        verify(userRepository).verifyEmail(1L);
        verify(tokenRepository).markEmailVerificationTokenUsed(tokenHash);
    }
}
