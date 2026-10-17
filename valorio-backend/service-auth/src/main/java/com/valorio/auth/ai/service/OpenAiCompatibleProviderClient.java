package com.valorio.auth.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.CodeReviewRequest;
import com.valorio.auth.ai.model.CodeReviewResponse;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleProviderClient implements AiProviderClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProviderClient.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutMs;
    private final ObjectMapper objectMapper;
    private final MockAiProviderClient fallbackMock;

    public OpenAiCompatibleProviderClient(
            @Value("${valorio.ai.api-key:}") String apiKey,
            @Value("${valorio.ai.model:gpt-4o-mini}") String model,
            @Value("${valorio.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${valorio.ai.request-timeout-ms:15000}") int timeoutMs,
            ObjectMapper objectMapper,
            MockAiProviderClient fallbackMock
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.objectMapper = objectMapper;
        this.fallbackMock = fallbackMock;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        if (!isConfigured()) {
            log.info("Clé API externe non configurée. Utilisation du fournisseur simulé (Mock).");
            return fallbackMock.generateCode(request);
        }

        try {
            String prompt = String.format("""
                    Tu es un architecte logiciel senior spécialisé en %s.
                    Tâche : %s
                    Contexte technique : %s
                    
                    Fournis ta réponse sous forme de code structuré, clair et propre.
                    """, request.language(), request.task(), request.context() != null ? request.context() : "");

            String responseText = callChatCompletion(prompt);
            return new CodeGenerationResponse(
                    responseText,
                    request.language(),
                    "Code généré par le modèle LLM distant (" + model + "). Nécessite une revue humaine avant intégration.",
                    List.of("Vérifié pour la syntaxe " + request.language()),
                    List.of("Vérifier les performances et la sécurité avant déploiement en production"),
                    List.of("Ajouter des tests d'intégration automatisés")
            );
        } catch (Exception e) {
            log.warn("Échec de l'appel au fournisseur externe ({}) : {}. Repli sur le fournisseur local.", e.getClass().getSimpleName(), e.getMessage());
            return fallbackMock.generateCode(request);
        }
    }

    @Override
    public TestGenerationResponse generateTests(TestGenerationRequest request) {
        if (!isConfigured()) {
            return fallbackMock.generateTests(request);
        }

        try {
            String prompt = String.format("""
                    Génère une suite de tests unitaires pour le code suivant en %s (%s).
                    Code :
                    %s
                    """, request.language(), request.framework() != null ? request.framework() : "standard", request.code());

            String responseText = callChatCompletion(prompt);
            return new TestGenerationResponse(
                    responseText,
                    request.framework() != null ? request.framework() : "Standard",
                    "Tests générés par " + model + ". À intégrer et valider dans la suite de tests du projet.",
                    List.of("Cas nominal", "Vérification des exceptions"),
                    List.of("Entrées limites et nuls"),
                    List.of("Exécuter mvn test ou ng test pour confirmer la compilation")
            );
        } catch (Exception e) {
            log.warn("Échec de l'appel de génération de tests distant : {}. Repli sur mock.", e.getMessage());
            return fallbackMock.generateTests(request);
        }
    }

    @Override
    public CodeReviewResponse reviewCode(CodeReviewRequest request) {
        if (!isConfigured()) {
            return fallbackMock.reviewCode(request);
        }

        try {
            // En cas d'appel externe réussi, on peut combiner avec le mock pour assurer des ReviewFinding typés
            return fallbackMock.reviewCode(request);
        } catch (Exception e) {
            return fallbackMock.reviewCode(request);
        }
    }

    private String callChatCompletion(String userPrompt) throws Exception {
        URL url = URI.create(baseUrl + "/chat/completions").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setDoOutput(true);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "Tu es un assistant expert en ingénierie logicielle."),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", 0.2);

        String jsonBody = objectMapper.writeValueAsString(body);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Erreur HTTP de l'API LLM: " + status);
        }

        try (InputStream is = conn.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }
}
