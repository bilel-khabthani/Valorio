package com.valorio.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.ai.api.AiController;
import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.QualityReportRequest;
import com.valorio.auth.ai.model.QualityReportResponse;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import com.valorio.auth.ai.service.AiService;
import com.valorio.auth.repo.UserRepository;
import com.valorio.auth.security.JwtService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Endpoint /api/ai/code-generation retourne le code généré")
    void codeGenerationSucces() throws Exception {
        CodeGenerationRequest req = new CodeGenerationRequest("Générer un service", "java", "Spring Boot");
        CodeGenerationResponse res = new CodeGenerationResponse(
                "public class GeneratedService {}", "java", "Explication", List.of("Hypothèse"), List.of(), List.of()
        );

        given(aiService.generateCode(any())).willReturn(res);

        mvc.perform(post("/api/ai/code-generation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("public class GeneratedService {}"))
                .andExpect(jsonPath("$.language").value("java"));
    }

    @Test
    @DisplayName("Endpoint /api/ai/tests/generate génère des tests")
    void testsGenerateSucces() throws Exception {
        TestGenerationRequest req = new TestGenerationRequest("class A {}", "java", "JUnit", "UNIT", null);
        TestGenerationResponse res = new TestGenerationResponse(
                "class ATest {}", "JUnit 5", "Explication tests", List.of("Cas 1"), List.of(), List.of()
        );

        given(aiService.generateTests(any())).willReturn(res);

        mvc.perform(post("/api/ai/tests/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.framework").value("JUnit 5"));
    }

    @Test
    @DisplayName("Endpoint /api/ai/quality-report retourne le rapport complet")
    void qualityReportSucces() throws Exception {
        QualityReportRequest req = new QualityReportRequest("public class Main {}", "java", true, true);
        QualityReportResponse res = new QualityReportResponse(
                "REP-123456", OffsetDateTime.now(), "Rapport OK", "PASSED", Map.of("totalFindings", 0), List.of()
        );

        given(aiService.generateQualityReport(any())).willReturn(res);

        mvc.perform(post("/api/ai/quality-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("REP-123456"))
                .andExpect(jsonPath("$.status").value("PASSED"));
    }
}
