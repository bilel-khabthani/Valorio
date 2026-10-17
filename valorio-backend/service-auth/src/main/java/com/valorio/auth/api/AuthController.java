package com.valorio.auth.api;

import com.valorio.auth.dto.AuthResponse;
import com.valorio.auth.dto.ForgotPasswordRequest;
import com.valorio.auth.dto.LoginRequest;
import com.valorio.auth.dto.MessageResponse;
import com.valorio.auth.dto.RegisterRequest;
import com.valorio.auth.dto.ResetPasswordRequest;
import com.valorio.auth.dto.ResendVerificationRequest;
import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.dto.VerifyEmailRequest;
import com.valorio.auth.security.UserPrincipal;
import com.valorio.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'authentification et gestion de session")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur et émission des jetons")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchissement du jeton d'accès via le cookie HTTP-Only")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = AuthService.REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Supporte aussi un rafraîchissement via header de secours si les cookies cross-origin sont bloqués
        String token = refreshToken;
        if (token == null || token.isBlank()) {
            token = request.getHeader("X-Refresh-Token");
        }
        AuthResponse authResponse = authService.refreshToken(token, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion et révocation de la session")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = AuthService.REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = refreshToken;
        if (token == null || token.isBlank()) {
            token = request.getHeader("X-Refresh-Token");
        }
        authService.logout(token, response);
        return ResponseEntity.ok(MessageResponse.of("Déconnexion réussie"));
    }

    @GetMapping("/me")
    @Operation(summary = "Récupération du profil de l'utilisateur actuellement authentifié")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse userResponse = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demande de lien de réinitialisation de mot de passe")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialisation du mot de passe avec jeton à usage unique")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Validation de l'adresse email par jeton")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Réexpédition du lien de vérification d'adresse email")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        MessageResponse response = authService.resendVerification(request.email());
        return ResponseEntity.ok(response);
    }
}
