package com.valorio.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.domain.Role;
import com.valorio.auth.dto.AuthResponse;
import com.valorio.auth.dto.LoginRequest;
import com.valorio.auth.dto.MessageResponse;
import com.valorio.auth.dto.RegisterRequest;
import com.valorio.auth.dto.UserResponse;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.security.JwtService;
import com.valorio.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Endpoint /register retourne 201 avec le token et l'utilisateur")
    void registerSucces() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Ben Salah", "Amine", "amine@valorio.tn", "Secret123456!", "Secret123456!", true
        );
        UserResponse user = new UserResponse(1L, "amine@valorio.tn", "Ben Salah", "Amine", "Amine Ben Salah", Role.USER, false, true);
        AuthResponse authRes = AuthResponse.of("access_token_xyz", 900, user);

        given(authService.register(any(RegisterRequest.class), any())).willReturn(authRes);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access_token_xyz"))
                .andExpect(jsonPath("$.user.email").value("amine@valorio.tn"));
    }

    @Test
    @DisplayName("Endpoint /register refuse les mots de passe trop courts (< 12 caractères)")
    void registerRefuseMotDePasseCourt() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Ben Salah", "Amine", "amine@valorio.tn", "Court1!", "Court1!", true
        );

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation échouée"));
    }

    @Test
    @DisplayName("Endpoint /login valide les identifiants")
    void loginSucces() throws Exception {
        LoginRequest req = new LoginRequest("amine@valorio.tn", "Secret123456!", false);
        UserResponse user = new UserResponse(1L, "amine@valorio.tn", "Ben Salah", "Amine", "Amine Ben Salah", Role.USER, true, true);
        AuthResponse authRes = AuthResponse.of("login_token_abc", 900, user);

        given(authService.login(any(LoginRequest.class), any())).willReturn(authRes);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("login_token_abc"));
    }

    @Test
    @DisplayName("Endpoint /forgot-password accepte la demande et renvoie un message générique")
    void forgotPasswordSucces() throws Exception {
        given(authService.forgotPassword(any())).willReturn(MessageResponse.of("Lien envoyé"));

        mvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"amine@valorio.tn\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
