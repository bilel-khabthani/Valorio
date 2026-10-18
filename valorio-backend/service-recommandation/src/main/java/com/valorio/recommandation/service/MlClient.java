package com.valorio.recommandation.service;

import com.valorio.recommandation.dto.ActeursRequest;
import com.valorio.recommandation.dto.MarcheRequest;
import com.valorio.recommandation.dto.ModeleMetadata;
import com.valorio.recommandation.dto.ObjetAnalyseInput;
import com.valorio.recommandation.dto.RecoActeursResponse;
import com.valorio.recommandation.dto.RecoMarcheResponse;
import com.valorio.recommandation.dto.RecoValorisationResponse;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MlClient {

    private static final Logger log = LoggerFactory.getLogger(MlClient.class);

    private final RestClient restClient;

    public MlClient(@Value("${valorio.ml.base-url}") String baseUrl,
                    @Value("${valorio.ml.timeout-ms:3000}") long timeoutMs) {
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
    }

    public RecoValorisationResponse predictValorisation(ObjetAnalyseInput objet) {
        return post("/predict/valorisation", objet, RecoValorisationResponse.class);
    }

    public RecoActeursResponse predictActeurs(ActeursRequest request) {
        return post("/predict/acteurs", versCorpsPython(request), RecoActeursResponse.class);
    }

    private Object versCorpsPython(ActeursRequest request) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("typeActeur", request.typeActeur().name());
        Map<String, Object> position = new LinkedHashMap<>();
        position.put("latitude", request.latitude() != null ? request.latitude() : 0.0);
        position.put("longitude", request.longitude() != null ? request.longitude() : 0.0);
        corps.put("position", position);
        Map<String, Object> contexte = new LinkedHashMap<>();
        ObjetAnalyseInput objet = request.objet();
        if (objet != null) {
            if (objet.categorie() != null) {
                contexte.put("categorie", objet.categorie());
            }
            if (objet.etat() != null) {
                contexte.put("etat", objet.etat());
            }
            if (objet.ageAnnees() != null) {
                contexte.put("ageAnnees", objet.ageAnnees());
            }
            if (objet.valeurEstimee() != null) {
                contexte.put("valeurEstimee", objet.valeurEstimee());
            }
            if (objet.ville() != null) {
                contexte.put("ville", objet.ville());
            }
        }
        contexte.put("rayonKm", request.rayonKm() != null ? request.rayonKm() : 100.0);
        corps.put("contexte", contexte);
        corps.put("candidats", request.candidats());
        return corps;
    }

    public RecoMarcheResponse predictMarche(MarcheRequest request) {
        return post("/predict/marche", request, RecoMarcheResponse.class);
    }

    public ModeleMetadata metadata() {
        try {
            ModeleMetadata reponse = restClient.get()
                    .uri("/model/metadata")
                    .retrieve()
                    .body(ModeleMetadata.class);
            if (reponse == null) {
                throw new MlIndisponibleException("Reponse vide du moteur ML sur /model/metadata");
            }
            return reponse;
        } catch (MlIndisponibleException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new MlIndisponibleException("Moteur ML injoignable sur /model/metadata", e);
        }
    }

    private <T> T post(String path, Object payload, Class<T> type) {
        try {
            T reponse = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(type);
            if (reponse == null) {
                throw new MlIndisponibleException("Reponse vide du moteur ML sur " + path);
            }
            return reponse;
        } catch (MlIndisponibleException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("Moteur ML a rejeté {} (HTTP {}) : {}", path, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new MlIndisponibleException("Moteur ML a rejeté la requête sur " + path, e);
        } catch (RuntimeException e) {
            log.warn("Moteur ML injoignable sur {} : {}", path, e.getMessage());
            throw new MlIndisponibleException("Moteur ML injoignable sur " + path, e);
        }
    }
}
