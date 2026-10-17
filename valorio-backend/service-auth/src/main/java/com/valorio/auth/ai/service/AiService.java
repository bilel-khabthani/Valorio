package com.valorio.auth.ai.service;

import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.CodeReviewRequest;
import com.valorio.auth.ai.model.CodeReviewResponse;
import com.valorio.auth.ai.model.QualityFinding;
import com.valorio.auth.ai.model.QualityReportRequest;
import com.valorio.auth.ai.model.QualityReportResponse;
import com.valorio.auth.ai.model.ReviewFinding;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final int MAX_CODE_LENGTH = 50000;

    private static final Pattern SECRET_BEARER = Pattern.compile("(?i)bearer\\s+[a-z0-9-_.]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_API_KEY = Pattern.compile("(?i)(api[_-]?key|secret|password|token)\\s*[:=]\\s*[\"'][^\"']+[\"']");
    private static final Pattern SECRET_PRIVATE_KEY = Pattern.compile("-----BEGIN [A-Z ]+ PRIVATE KEY-----");

    private final MockAiProviderClient mockProvider;
    private final OpenAiCompatibleProviderClient openAiProvider;
    private final String configuredProvider;

    public AiService(
            MockAiProviderClient mockProvider,
            OpenAiCompatibleProviderClient openAiProvider,
            @Value("${valorio.ai.provider:mock}") String configuredProvider
    ) {
        this.mockProvider = mockProvider;
        this.openAiProvider = openAiProvider;
        this.configuredProvider = configuredProvider;
    }

    private AiProviderClient getActiveProvider() {
        if ("openai".equalsIgnoreCase(configuredProvider) && openAiProvider.isConfigured()) {
            return openAiProvider;
        }
        return mockProvider;
    }

    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        String sanitizedTask = redactSecrets(request.task());
        String sanitizedContext = redactSecrets(request.context());

        CodeGenerationRequest sanitizedRequest = new CodeGenerationRequest(
                sanitizedTask,
                request.language(),
                sanitizedContext
        );

        return getActiveProvider().generateCode(sanitizedRequest);
    }

    public TestGenerationResponse generateTests(TestGenerationRequest request) {
        validatePayloadSize(request.code());
        String sanitizedCode = redactSecrets(request.code());
        String sanitizedContext = redactSecrets(request.context());

        TestGenerationRequest sanitizedRequest = new TestGenerationRequest(
                sanitizedCode,
                request.language(),
                request.framework(),
                request.testType(),
                sanitizedContext
        );

        return getActiveProvider().generateTests(sanitizedRequest);
    }

    public CodeReviewResponse reviewCode(CodeReviewRequest request) {
        validatePayloadSize(request.code());
        String sanitizedCode = redactSecrets(request.code());

        CodeReviewRequest sanitizedRequest = new CodeReviewRequest(
                sanitizedCode,
                request.language(),
                request.context()
        );

        return getActiveProvider().reviewCode(sanitizedRequest);
    }

    public QualityReportResponse generateQualityReport(QualityReportRequest request) {
        validatePayloadSize(request.code());
        String rawCode = request.code();
        String sanitizedCode = redactSecrets(rawCode);

        boolean includeStatic = request.includeStaticAnalysis() == null || request.includeStaticAnalysis();
        boolean includeAi = request.includeAiReview() == null || request.includeAiReview();

        List<QualityFinding> findings = new ArrayList<>();

        // 1. Analyse statique automatisée déterministe (source: STATIC_ANALYSIS)
        if (includeStatic) {
            findings.addAll(runStaticAnalysis(rawCode, request.language()));
        }

        // 2. Revue IA prédictive (source: AI_PREDICTED)
        if (includeAi) {
            CodeReviewResponse aiReview = getActiveProvider().reviewCode(
                    new CodeReviewRequest(sanitizedCode, request.language(), null)
            );
            for (ReviewFinding rf : aiReview.findings()) {
                findings.add(new QualityFinding(
                        rf.id(),
                        rf.category(),
                        rf.severity(),
                        rf.description(),
                        rf.evidence(),
                        rf.lineReference(),
                        rf.suggestedCorrection(),
                        "AI_PREDICTED",
                        rf.confidenceLevel()
                ));
            }
        }

        // Calcul des métriques
        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;

        for (QualityFinding f : findings) {
            switch (f.severity().toUpperCase()) {
                case "CRITICAL" -> critical++;
                case "HIGH" -> high++;
                case "MEDIUM" -> medium++;
                default -> low++;
            }
        }

        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("totalFindings", findings.size());
        metrics.put("criticalCount", critical);
        metrics.put("highCount", high);
        metrics.put("mediumCount", medium);
        metrics.put("lowCount", low);

        String status = critical > 0 ? "FAILED" : (high > 0 ? "WARNING" : "PASSED");
        String summary = String.format("Rapport d'assurance qualité : %d anomalie(s) détectée(s) (Statut: %s).",
                findings.size(), status);

        return new QualityReportResponse(
                "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                OffsetDateTime.now(),
                summary,
                status,
                metrics,
                findings
        );
    }

    public String redactSecrets(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String result = SECRET_BEARER.matcher(input).replaceAll("Bearer [REDACTED_TOKEN]");
        result = SECRET_API_KEY.matcher(result).replaceAll("$1=\"[REDACTED_CREDENTIAL]\"");
        result = SECRET_PRIVATE_KEY.matcher(result).replaceAll("[REDACTED_PRIVATE_KEY]");
        return result;
    }

    private void validatePayloadSize(String code) {
        if (code != null && code.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("La taille du code dépasse la limite autorisée (" + MAX_CODE_LENGTH + " caractères).");
        }
    }

    private List<QualityFinding> runStaticAnalysis(String code, String language) {
        List<QualityFinding> findings = new ArrayList<>();

        // Règle 1 : Détection d'injection SQL potentielle (concaténation dans requête SQL)
        if (code.matches("(?s).*SELECT\\s+.*\\+\\s*[a-zA-Z0-9_]+.*") ||
            code.matches("(?s).*WHERE\\s+.*\\+\\s*[a-zA-Z0-9_]+.*")) {
            findings.add(new QualityFinding(
                    "STATIC-SQL-01",
                    "SECURITY",
                    "CRITICAL",
                    "Concaténation dynamique de chaîne dans une requête SQL détectée (vulnérabilité d'injection SQL).",
                    "Concaténation avec opérateur '+' dans clause SQL",
                    "Requête SQL",
                    "Utiliser des requêtes paramétrées avec JdbcClient (:param) ou PreparedStatement.",
                    "STATIC_ANALYSIS",
                    1.0
            ));
        }

        // Règle 2 : Clé secrète / token en clair dans le code
        if (SECRET_API_KEY.matcher(code).find() || SECRET_PRIVATE_KEY.matcher(code).find()) {
            findings.add(new QualityFinding(
                    "STATIC-SEC-02",
                    "SECURITY",
                    "HIGH",
                    "Secret, mot de passe ou clé privée codé en dur dans le fichier source.",
                    "Motif de secret détecté dans le code",
                    "Déclaration de secret",
                    "Externaliser tous les secrets dans des variables d'environnement (${VAR_NAME}).",
                    "STATIC_ANALYSIS",
                    0.99
            ));
        }

        // Règle 3 : Méthode vide ou TODO critique
        if (code.contains("TODO") || code.contains("FIXME")) {
            findings.add(new QualityFinding(
                    "STATIC-MAINT-03",
                    "MAINTAINABILITY",
                    "LOW",
                    "Commentaire TODO ou FIXME présent dans le code source.",
                    "TODO / FIXME trouvé",
                    "Commentaires de code",
                    "Résoudre les points techniques en suspens avant mise en production.",
                    "STATIC_ANALYSIS",
                    1.0
            ));
        }

        return findings;
    }
}
