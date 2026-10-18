package com.valorio.recommandation.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorio.recommandation.dto.ArticleMarche;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Pont vers le microservice Marketplace (8086) : récupère les annonces
 * disponibles pour enrichir le classement de marché. En cas d'indisponibilité,
 * retourne une liste vide (aucune donnée statique de repli).
 */
@Component
public class MarketplaceMarcheClient {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceMarcheClient.class);

    private static final Map<String, String> CATEGORIES = Map.ofEntries(
            Map.entry("ELECTRONICS", "Electronique"),
            Map.entry("BICYCLES_VEHICLES", "Velo & mobilite"),
            Map.entry("CLOTHING_ACCESSORIES", "Vetements"),
            Map.entry("JEWELRY_WATCHES", "Autre"),
            Map.entry("BAGS_LUGGAGE", "Autre"),
            Map.entry("DOCUMENTS_KEYS", "Autre"),
            Map.entry("SPORTS_LEISURE", "Autre"),
            Map.entry("OTHER", "Autre"));

    private static final Map<String, String> ETATS = Map.ofEntries(
            Map.entry("NEW_UNOPENED", "NEUF"),
            Map.entry("EXCELLENT", "EXCELLENT"),
            Map.entry("GOOD", "BON"),
            Map.entry("FAIR", "ENDOMMAGE"),
            Map.entry("FOR_PARTS", "HORS_SERVICE"));

    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final boolean actif;

    public MarketplaceMarcheClient(@Value("${valorio.marketplace.base-url:http://localhost:8086}") String baseUrl,
                                   @Value("${valorio.marketplace.timeout-ms:2000}") long timeoutMs,
                                   @Value("${valorio.marketplace.actif:true}") boolean actif,
                                   ObjectMapper mapper) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.mapper = mapper;
        this.actif = actif;
    }

    public List<ArticleMarche> findAllDisponibles() {
        if (!actif) {
            return List.of();
        }
        try {
            String json = restClient.get()
                    .uri("/api/marketplace/items?status=AVAILABLE")
                    .retrieve()
                    .body(String.class);
            if (json == null || json.isBlank()) {
                return List.of();
            }
            JsonNode items = mapper.readTree(json);
            List<ArticleMarche> articles = new ArrayList<>();
            for (JsonNode item : items) {
                String titre = text(item, "title");
                if (titre == null || titre.isBlank()) {
                    continue;
                }
                articles.add(new ArticleMarche(
                        item.has("id") && !item.get("id").isNull() ? item.get("id").asLong() : null,
                        titre,
                        CATEGORIES.getOrDefault(text(item, "category"), "Autre"),
                        ETATS.getOrDefault(text(item, "condition"), "BON"),
                        item.has("price") && !item.get("price").isNull() ? item.get("price").asDouble() : 0.0,
                        text(item, "description")));
            }
            log.info("Marche : {} annonces disponibles recuperees depuis le module Marketplace", articles.size());
            return articles;
        } catch (Exception e) {
            log.warn("Module Marketplace injoignable ({}), aucun article de marché disponible",
                    e.getMessage());
            return List.of();
        }
    }

    private String text(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
