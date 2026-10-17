package com.valorio.auth.security;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valorio.auth.api.AuthController;
import com.valorio.auth.domain.Role;
import com.valorio.auth.domain.User;
import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.service.AuthService;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ProtectedEndpointsSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Refuse l'accès à /api/auth/me sans authentification (retourne 401)")
    void accesMeSansTokenRetourne401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Refuse l'accès à /api/auth/me avec un jeton invalide ou expiré (retourne 401)")
    void accesMeAvecTokenInvalideRetourne401() throws Exception {
        given(jwtService.validateToken("faux-jeton-jwt")).willReturn(false);

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer faux-jeton-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Autorise l'accès à /api/auth/me avec un jeton Bearer valide et utilisateur actif")
    void accesMeAvecTokenValideRetourne200() throws Exception {
        String validToken = "valid.jwt.token";
        User user = new User(1L, "amine@valorio.tn", "hash", "Ben Salah", "Amine", Role.USER, true, true, OffsetDateTime.now(), OffsetDateTime.now());
        UserResponse responseDto = new UserResponse(1L, "amine@valorio.tn", "Ben Salah", "Amine", "Amine Ben Salah", Role.USER, true, true);

        given(jwtService.validateToken(validToken)).willReturn(true);
        given(jwtService.extractUserId(validToken)).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(authService.getCurrentUser(1L)).willReturn(responseDto);

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("amine@valorio.tn"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
