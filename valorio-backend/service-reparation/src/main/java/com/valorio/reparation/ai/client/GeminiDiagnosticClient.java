package com.valorio.reparation.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.reparation.ai.model.DiagnosticRequest;
import com.valorio.reparation.ai.model.DiagnosticResponse;
import com.valorio.reparation.ai.model.DiagnosticResponse.DevisEstime;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client du modèle Gemini (Google AI Studio). Appelle l'API
 * {@code generateContent} avec la clé transmise dans l'en-tête
 * {@code x-goog-api-key} (jamais journalisée ni placée dans l'URL).
 *
 * La réponse est demandée en JSON strict ({@code responseMimeType}) puis mappée
 * vers {@link DiagnosticResponse}. En cas d'absence de clé ou d'échec, une
 * exception est levée : {@code DiagnosticService} bascule alors sur le moteur
 * heuristique.
 */
@Component
public class GeminiDiagnosticClient implements DiagnosticAiClient {

    public static final String SOURCE = "gemini";
    private static final Logger log = LoggerFactory.getLogger(GeminiDiagnosticClient.class);

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiDiagnosticClient(
            @Value("${valorio.ai.api-key:}") String apiKey,
            @Value("${valorio.ai.model:gemini-3.8-flash}") String model,
            @Value("${valorio.ai.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${valorio.ai.request-timeout-ms:20000}") int timeoutMs,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public String nom() {
        return SOURCE;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String modele() {
        return model;
    }

    @Override
    public DiagnosticResponse diagnostiquer(DiagnosticRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("Clé API Gemini absente (AI_API_KEY non configurée).");
        }
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt(request))))));
        corps.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.2));

        String brut = restClient.post()
                .uri("/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corps)
                .retrieve()
                .body(String.class);

        log.debug("Réponse Gemini reçue ({} caractères)", brut == null ? 0 : brut.length());
        return mapper(brut);
    }

    private String prompt(DiagnosticRequest request) {
        StringBuilder contexte = new StringBuilder();
        if (request.intitule() != null && !request.intitule().isBlank()) {
            contexte.append("Intitulé de l'objet : ").append(request.intitule()).append('\n');
        }
        if (request.etat() != null && !request.etat().isBlank()) {
            contexte.append("État déclaré : ").append(request.etat()).append('\n');
        }
        if (request.ageAnnees() != null) {
            contexte.append("Âge (années) : ").append(request.ageAnnees()).append('\n');
        }
        if (request.ville() != null && !request.ville().isBlank()) {
            contexte.append("Ville : ").append(request.ville()).append('\n');
        }
        return """
                Tu es un assistant technique de réparation pour la plateforme Valorio.
                Analyse la panne décrite et réponds UNIQUEMENT avec un objet JSON valide,
                sans texte autour, au format exact suivant :
                {
                  "categoriePanne": "ECRAN | BATTERIE | CARTE_MERE | ALIMENTATION | CLAVIER | DISQUE | REFROIDISSEMENT | MOTEUR_ELECTRIQUE | MENUISERIE | FREINAGE | TRANSMISSION | AUTRE",
                  "gravite": "FAIBLE | MOYENNE | ELEVEE | CRITIQUE",
                  "diagnostic": "explication courte (2 phrases maximum) en français",
                  "causesProbables": ["cause 1", "cause 2", "cause 3"],
                  "devisEstime": {
                    "montantMin": 0,
                    "montantMax": 0,
                    "delaiJours": 1,
                    "devise": "DT"
                  },
                  "confiance": 0.0
                }
                Le champ "confiance" est un nombre entre 0 et 1. Les montants sont en dinars tunisiens (DT).

                Contexte de l'objet :
                %s
                Description de la panne : %s
                """.formatted(contexte, request.descriptionPanne());
    }

    private DiagnosticResponse mapper(String brut) {
        try {
            JsonNode racine = objectMapper.readTree(brut);
            String texte = extraireTexte(racine);
            if (texte == null || texte.isBlank()) {
                throw new IllegalStateException("Réponse Gemini vide");
            }
            JsonNode data = objectMapper.readTree(stripCodeFences(texte));

            List<String> causes = new ArrayList<>();
            JsonNode causesNode = data.path("causesProbables");
            if (causesNode.isArray()) {
                causesNode.forEach(c -> causes.add(c.asText()));
            }

            JsonNode devisNode = data.path("devisEstime");
            DevisEstime devis = new DevisEstime(
                    decimal(devisNode.path("montantMin"), BigDecimal.valueOf(50)),
                    decimal(devisNode.path("montantMax"), BigDecimal.valueOf(150)),
                    devisNode.path("delaiJours").asInt(2),
                    devisNode.path("devise").asText("DT"));

            List<String> avertissements = new ArrayList<>();
            avertissements.add("Estimation générée par Gemini, à confirmer par un réparateur.");

            return new DiagnosticResponse(
                    data.path("categoriePanne").asText("AUTRE"),
                    data.path("gravite").asText("MOYENNE"),
                    data.path("diagnostic").asText(""),
                    causes,
                    devis,
                    List.of(),
                    data.path("confiance").asDouble(0.6),
                    SOURCE,
                    avertissements);
        } catch (Exception e) {
            throw new IllegalStateException("Réponse Gemini illisible : " + e.getMessage(), e);
        }
    }

    private String extraireTexte(JsonNode racine) {
        JsonNode candidates = racine.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        StringBuilder texte = new StringBuilder();
        if (parts.isArray()) {
            parts.forEach(part -> texte.append(part.path("text").asText("")));
        }
        return texte.toString();
    }

    private String stripCodeFences(String texte) {
        String nettoye = texte.trim();
        if (nettoye.startsWith("```")) {
            nettoye = nettoye.replaceFirst("^```[a-zA-Z]*\\s*", "");
            nettoye = nettoye.replaceFirst("\\s*```$", "");
        }
        return nettoye.trim();
    }

    private BigDecimal decimal(JsonNode noeud, BigDecimal defaut) {
        return noeud.isNumber() ? noeud.decimalValue() : defaut;
    }
}
