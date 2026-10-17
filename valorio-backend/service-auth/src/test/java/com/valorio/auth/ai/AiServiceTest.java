package com.valorio.auth.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.QualityReportRequest;
import com.valorio.auth.ai.model.QualityReportResponse;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import com.valorio.auth.ai.service.AiService;
import com.valorio.auth.ai.service.MockAiProviderClient;
import com.valorio.auth.ai.service.OpenAiCompatibleProviderClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiServiceTest {

    private AiService aiService;

    @BeforeEach
    void setUp() {
        MockAiProviderClient mockClient = new MockAiProviderClient();
        OpenAiCompatibleProviderClient openAiClient = new OpenAiCompatibleProviderClient(
                "", "gpt-4o-mini", "https://api.openai.com/v1", 15000, new ObjectMapper(), mockClient
        );
        aiService = new AiService(mockClient, openAiClient, "mock");
    }

    @Test
    @DisplayName("Génère du code Java avec le fournisseur mock par défaut")
    void genereCodeJavaAvecMock() {
        CodeGenerationRequest req = new CodeGenerationRequest("Créer un service d'audit", "java", "Spring Boot 3.3");
        CodeGenerationResponse res = aiService.generateCode(req);

        assertThat(res).isNotNull();
        assertThat(res.code()).contains("ValorioAuditService");
        assertThat(res.language()).isEqualTo("java");
        assertThat(res.explanation()).isNotBlank();
        assertThat(res.assumptions()).isNotEmpty();
    }

    @Test
    @DisplayName("Génère des tests unitaires Angular / TypeScript")
    void genereTestsAngular() {
        TestGenerationRequest req = new TestGenerationRequest(
                "export class DemoService {}", "typescript", "Jasmine", "UNIT", "Angular 20"
        );
        TestGenerationResponse res = aiService.generateTests(req);

        assertThat(res).isNotNull();
        assertThat(res.generatedTests()).contains("TestBed");
        assertThat(res.framework()).contains("Jasmine");
        assertThat(res.testCasesCovered()).isNotEmpty();
    }

    @Test
    @DisplayName("Censure et masque les données sensibles (tokens, clés d'API)")
    void masqueDonneesSensibles() {
        String codeAvecSecret = "String token = \"Bearer eyJhbGciOi...\"; String apiKey = \"sk-1234567890abcdef\";";
        String censure = aiService.redactSecrets(codeAvecSecret);

        assertThat(censure).doesNotContain("eyJhbGciOi...");
        assertThat(censure).contains("[REDACTED");
    }

    @Test
    @DisplayName("Détecte les vulnérabilités par analyse statique et produit un rapport de qualité")
    void detecteVulnerabilitesEtProduitRapport() {
        String codeVuln = """
                public void search(String input) {
                    String query = "SELECT * FROM users WHERE name = '" + input + "'";
                    String password = "hardcoded_secret_123";
                }
                """;

        QualityReportRequest req = new QualityReportRequest(codeVuln, "java", true, true);
        QualityReportResponse res = aiService.generateQualityReport(req);

        assertThat(res).isNotNull();
        assertThat(res.reportId()).startsWith("REP-");
        assertThat(res.findings()).isNotEmpty();

        // Vérifie qu'il y a des constatations issues de l'analyse statique ET de l'IA
        boolean aStatic = res.findings().stream().anyMatch(f -> "STATIC_ANALYSIS".equals(f.source()));
        boolean aAi = res.findings().stream().anyMatch(f -> "AI_PREDICTED".equals(f.source()));

        assertThat(aStatic).isTrue();
        assertThat(aAi).isTrue();
    }

    @Test
    @DisplayName("Rejette les charges utiles dépassant la limite maximale de taille")
    void rejetteChargeUtileTropGrande() {
        String codeTropGrand = "a".repeat(50001);
        TestGenerationRequest req = new TestGenerationRequest(codeTropGrand, "java", "JUnit", "UNIT", null);

        assertThatThrownBy(() -> aiService.generateTests(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dépasse la limite");
    }
}
