package com.valorio.recommandation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recommandation.domain.RecommandationHistorique;
import com.valorio.recommandation.domain.SourcePrediction;
import com.valorio.recommandation.dto.ExplicationResponse;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ExplicationService {

    private static final String SYSTEME = """
            Tu es l'assistant Valorio, spécialiste de la valorisation des objets usagés \
            (réparation, don, recyclage, revente). Explique de façon claire, concise et \
            pédagogique, en français, la recommandation fournie. Maximum 4 phrases, sans \
            liste à puces ni emojis.""";

    private static final int MAX_CARACTERES = 1500;

    private final RestClient aiClient;
    private final ObjectMapper mapper;
    private final String provider;
    private final String apiKey;

    public ExplicationService(@Value("${valorio.ai.provider:mock}") String provider,
                              @Value("${valorio.ai.api-key:}") String apiKey,
                              @Value("${valorio.ai.base-url:https://api.openai.com/v1}") String baseUrl,
                              ObjectMapper mapper) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.mapper = mapper;
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(4000))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(4000));
        this.aiClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public ExplicationResponse expliquer(RecommandationHistorique reco) {
        if ("openai".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            try {
                String narration = appelerLlm(reco);
                if (narration != null && !narration.isBlank()) {
                    return new ExplicationResponse(reco.id(), narration, SourcePrediction.ML);
                }
            } catch (Exception e) {
                // repli sur la narration structurée
            }
        }
        return new ExplicationResponse(reco.id(), narrationStructuree(reco), SourcePrediction.HEURISTIQUE);
    }

    private String appelerLlm(RecommandationHistorique reco) throws JsonProcessingException {
        Map<String, Object> corps = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEME),
                        Map.of("role", "user", "content", synthese(reco))),
                "max_tokens", 300,
                "temperature", 0.2);

        String json = aiClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corps)
                .retrieve()
                .body(String.class);
        if (json == null) {
            return null;
        }
        JsonNode noeud = mapper.readTree(json);
        String contenu = noeud.path("choices").path(0).path("message").path("content").asText(null);
        return nettoyer(contenu);
    }

    private String synthese(RecommandationHistorique reco) {
        return "Type de recommandation : " + reco.typeReco()
                + " ; modèle : " + (reco.modeleVersion() == null ? "inconnu" : reco.modeleVersion())
                + " ; source : " + reco.source()
                + " ; résultat : " + tronquer(reco.resultat(), 800)
                + " ; charge utile : " + tronquer(reco.chargeUtile(), 400);
    }

    private String narrationStructuree(RecommandationHistorique reco) {
        String version = reco.modeleVersion() == null || reco.modeleVersion().isBlank()
                ? "moteur inconnu" : reco.modeleVersion();
        String resultat = reco.resultat() == null ? "" : reco.resultat();

        try {
            JsonNode noeud = mapper.readTree(resultat);
            if ("VALORISATION".equals(reco.typeReco()) && noeud.hasNonNull("route")) {
                String route = noeud.path("route").asText();
                int pourcent = (int) Math.round(noeud.path("probabilite").asDouble() * 100);
                String raisons = topRaisons(noeud, 2);
                return "L'IA recommande la route " + route + " avec une probabilité de " + pourcent + " %"
                        + (raisons.isEmpty() ? "" : " : " + raisons)
                        + ". Cette recommandation provient du modèle " + version + ".";
            }
            if ("ACTEURS".equals(reco.typeReco()) && noeud.path("classement").size() > 0) {
                JsonNode premier = noeud.path("classement").path(0);
                int pourcent = (int) Math.round(premier.path("score").asDouble() * 100);
                return "L'IA classe en tête le candidat " + premier.path("id").asText()
                        + " avec un score de pertinence de " + pourcent + " %."
                        + " Cette recommandation provient du modèle " + version + ".";
            }
            if ("MARCHE".equals(reco.typeReco()) && noeud.path("articles").size() > 0) {
                JsonNode premier = noeud.path("articles").path(0);
                int pourcent = (int) Math.round(premier.path("similarite").asDouble() * 100);
                return "L'IA propose l'article « " + premier.path("titre").asText()
                        + " » avec une similarité de contenu de " + pourcent + " % et une équité de prix "
                        + premier.path("equitePrix").asText() + "."
                        + " Cette recommandation provient du modèle " + version + ".";
            }
        } catch (JsonProcessingException e) {
            // narration brute ci-dessous
        }
        return "Recommandation de type " + reco.typeReco()
                + " générée par le modèle " + version + ". Résultat : " + tronquer(resultat, 300) + ".";
    }

    private String topRaisons(JsonNode noeud, int max) {
        JsonNode raisons = noeud.path("raisons");
        if (!raisons.isArray()) {
            return "";
        }
        StringBuilder texte = new StringBuilder();
        int combien = 0;
        for (JsonNode raison : raisons) {
            if (combien == max) {
                break;
            }
            String libelle = raison.asText("");
            if (libelle.isBlank()) {
                continue;
            }
            if (texte.length() > 0) {
                texte.append(" ; ");
            }
            texte.append(libelle);
            combien++;
        }
        return texte.toString();
    }

    private String nettoyer(String texte) {
        if (texte == null) {
            return null;
        }
        String sansControle = texte.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
        return tronquer(sansControle, MAX_CARACTERES);
    }

    private String tronquer(String texte, int max) {
        if (texte == null) {
            return "";
        }
        return texte.length() <= max ? texte : texte.substring(0, max) + "…";
    }
}
