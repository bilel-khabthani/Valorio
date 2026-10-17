package com.valorio.auth.ai.service;

import com.valorio.auth.ai.model.CodeGenerationRequest;
import com.valorio.auth.ai.model.CodeGenerationResponse;
import com.valorio.auth.ai.model.CodeReviewRequest;
import com.valorio.auth.ai.model.CodeReviewResponse;
import com.valorio.auth.ai.model.ReviewFinding;
import com.valorio.auth.ai.model.TestGenerationRequest;
import com.valorio.auth.ai.model.TestGenerationResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAiProviderClient implements AiProviderClient {

    @Override
    public CodeGenerationResponse generateCode(CodeGenerationRequest request) {
        String lang = request.language() != null ? request.language().toLowerCase() : "java";
        String task = request.task() != null ? request.task() : "";

        String generatedCode;
        String explanation;
        List<String> assumptions = new ArrayList<>();
        List<String> potentialRisks = new ArrayList<>();
        List<String> suggestedImprovements = new ArrayList<>();

        switch (lang) {
            case "typescript":
            case "angular":
                generatedCode = """
                        import { Injectable, inject, signal } from '@angular/core';
                        import { HttpClient } from '@angular/common/http';
                        import { Observable, catchError, map, throwError } from 'rxjs';
                        
                        export interface ValorioItem {
                          id: number;
                          libelle: string;
                          statut: 'ACTIF' | 'ARCHIVE';
                          dateCreation: string;
                        }
                        
                        @Injectable({ providedIn: 'root' })
                        export class ItemManagementService {
                          private readonly http = inject(HttpClient);
                          private readonly apiUrl = '/api/items';
                        
                          readonly items = signal<ValorioItem[]>([]);
                          readonly chargement = signal<boolean>(false);
                        
                          chargerItems(): Observable<ValorioItem[]> {
                            this.chargement.set(true);
                            return this.http.get<ValorioItem[]>(this.apiUrl).pipe(
                              map((donnees) => {
                                this.items.set(donnees);
                                this.chargement.set(false);
                                return donnees;
                              }),
                              catchError((err) => {
                                this.chargement.set(false);
                                return throwError(() => new Error('Erreur de chargement des éléments'));
                              })
                            );
                          }
                        }
                        """;
                explanation = "Service Angular 20 moderne utilisant les Signals pour la réactivité, l'injection de dépendances simplifiée et la gestion des erreurs HTTP.";
                assumptions.add("Environnement Angular 20 avec provideHttpClient configuré");
                assumptions.add("L'API REST retourne une structure JSON conforme à l'interface ValorioItem");
                potentialRisks.add("Absence de pagination côté serveur si le volume d'éléments est très important");
                suggestedImprovements.add("Ajouter un mécanisme de cache local ou de retry automatique");
                suggestedImprovements.add("Implémenter un sélecteur réactif pour filtrer par statut");
                break;

            case "sql":
            case "postgresql":
                generatedCode = """
                        -- Migration Flyway pour la gestion optimisée des métriques
                        CREATE TABLE IF NOT EXISTS audit_performance (
                            id BIGSERIAL PRIMARY KEY,
                            module VARCHAR(64) NOT NULL,
                            action VARCHAR(128) NOT NULL,
                            duree_ms INTEGER NOT NULL,
                            succes BOOLEAN NOT NULL DEFAULT TRUE,
                            cree_le TIMESTAMPTZ NOT NULL DEFAULT now()
                        );
                        
                        CREATE INDEX IF NOT EXISTS idx_audit_module_cree_le 
                        ON audit_performance (module, cree_le DESC);
                        
                        CREATE INDEX IF NOT EXISTS idx_audit_succes 
                        ON audit_performance (succes) WHERE NOT succes;
                        """;
                explanation = "Schéma PostgreSQL avec clé primaire BIGSERIAL, index partiel pour les requêtes de diagnostic et horodatage TIMESTAMPTZ conforme aux normes Valorio.";
                assumptions.add("Base de données PostgreSQL 15+");
                assumptions.add("Rétention des données gérée par un processus d'archivage ultérieur");
                potentialRisks.add("Croissance de la table en cas de fort trafic sans partitionnement mensuel");
                suggestedImprovements.add("Mettre en place un partitionnement par date (PARTITION BY RANGE)");
                suggestedImprovements.add("Créer une vue matérialisée pour les métriques agrégées");
                break;

            case "python":
                generatedCode = """
                        import logging
                        from typing import List, Dict, Any
                        from dataclasses import dataclass
                        from datetime import datetime
                        
                        logging.basicConfig(level=logging.INFO)
                        logger = logging.getLogger(__name__)
                        
                        @dataclass
                        class DiagnosticReport:
                            identifiant: str
                            score_conformite: float
                            anomalies: List[str]
                            genere_le: datetime = datetime.utcnow()
                        
                        def analyser_qualite(metriques: Dict[str, Any]) -> DiagnosticReport:
                            logger.info("Début de l'analyse automatisée de qualité...")
                            anomalies = []
                            couverture = metriques.get("coverage", 0.0)
                            if couverture < 80.0:
                                anomalies.append(f"Couverture insuffisante: {couverture}% < 80%")
                                
                            dette_technique = metriques.get("technical_debt_ratio", 0.0)
                            if dette_technique > 5.0:
                                anomalies.append(f"Ratio de dette technique élevé: {dette_technique}%")
                                
                            score = max(0.0, 100.0 - (len(anomalies) * 15.0))
                            return DiagnosticReport(
                                identifiant=metriques.get("project_id", "valorio"),
                                score_conformite=score,
                                anomalies=anomalies
                            )
                        """;
                explanation = "Script Python typé avec dataclass, journalisation standard et calcul de diagnostic de conformité logicielle.";
                assumptions.add("Python 3.10 ou supérieur");
                potentialRisks.add("Format d'entrée des dictionnaires non validé par schéma strict (Pydantic)");
                suggestedImprovements.add("Intégrer Pydantic pour une validation robuste des types");
                break;

            case "java":
            default:
                generatedCode = """
                        package com.valorio.service;
                        
                        import java.time.OffsetDateTime;
                        import java.util.List;
                        import java.util.Optional;
                        import org.slf4j.Logger;
                        import org.slf4j.LoggerFactory;
                        import org.springframework.jdbc.core.simple.JdbcClient;
                        import org.springframework.stereotype.Service;
                        import org.springframework.transaction.annotation.Transactional;
                        
                        @Service
                        public class ValorioAuditService {
                        
                            private static final Logger log = LoggerFactory.getLogger(ValorioAuditService.class);
                            private final JdbcClient jdbcClient;
                        
                            public ValorioAuditService(JdbcClient jdbcClient) {
                                this.jdbcClient = jdbcClient;
                            }
                        
                            public record AuditEntry(
                                    Long id,
                                    String module,
                                    String action,
                                    String auteur,
                                    OffsetDateTime dateAction
                            ) {}
                        
                            @Transactional
                            public AuditEntry enregistrerAction(String module, String action, String auteur) {
                                if (module == null || action == null) {
                                    throw new IllegalArgumentException("Le module et l'action sont obligatoires");
                                }
                                log.info("Audit: enregistrement action [{}] sur module [{}] par [{}]", action, module, auteur);
                                
                                Long id = jdbcClient.sql(\"\"\"
                                        INSERT INTO audit_log (evenement, entite, details, cree_le)
                                        VALUES (:evenement, :entite, :details, now())
                                        RETURNING id
                                        \"\"\")
                                        .param("evenement", action)
                                        .param("entite", module)
                                        .param("details", "Exécuté par " + auteur)
                                        .query(Long.class)
                                        .single();
                                
                                return new AuditEntry(id, module, action, auteur, OffsetDateTime.now());
                            }
                        }
                        """;
                explanation = "Service Spring Boot 3.3 conforme aux patrons existants du dépôt Valorio (utilisation de JdbcClient, records Java 17, journalisation SLF4J et annotations transactionnelles).";
                assumptions.add("Spring Boot 3.3 et Java 17");
                assumptions.add("Table audit_log existante dans le schéma actif");
                potentialRisks.add("La volumétrie de la table d'audit doit être purgée périodiquement");
                suggestedImprovements.add("Ajouter un mécanisme asynchrone (@Async) pour ne pas bloquer les transactions métier");
                suggestedImprovements.add("Ajouter une méthode de recherche paginée avec filtre par date");
                break;
        }

        return new CodeGenerationResponse(
                generatedCode.trim(),
                lang,
                explanation,
                assumptions,
                potentialRisks,
                suggestedImprovements
        );
    }

    @Override
    public TestGenerationResponse generateTests(TestGenerationRequest request) {
        String lang = request.language() != null ? request.language().toLowerCase() : "java";
        String generatedTests;
        String framework;
        String explanation;
        List<String> coveredCases = List.of(
                "Cas nominal : validation des entrées et exécution avec succès",
                "Cas limite : paramètres manquants ou nuls déclenchant les vérifications",
                "Gestion d'erreur : vérification de la levée des exceptions attendues"
        );
        List<String> edgeCases = List.of(
                "Chaînes de caractères vides ou composées uniquement d'espaces",
                "Identifiants inexistants ou non trouvés",
                "Dépassement de délais ou indisponibilité de ressource"
        );
        List<String> suggestions = List.of(
                "Intégrer les tests dans le pipeline CI/CD automatisé",
                "Vérifier la couverture de code globale avec JaCoCo ou Karma coverage"
        );

        if ("typescript".equalsIgnoreCase(lang) || "angular".equalsIgnoreCase(lang)) {
            framework = "Jasmine / Karma";
            explanation = "Suite de tests unitaires Angular 20 avec TestBed et espions (spies) pour simuler les dépendances HTTP.";
            generatedTests = """
                    import { TestBed } from '@angular/core/testing';
                    import { provideHttpClient } from '@angular/common/http';
                    import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
                    
                    describe('Composant / Service Valorio', () => {
                      let httpMock: HttpTestingController;
                    
                      beforeEach(() => {
                        TestBed.configureTestingModule({
                          providers: [
                            provideHttpClient(),
                            provideHttpClientTesting()
                          ]
                        });
                        httpMock = TestBed.inject(HttpTestingController);
                      });
                    
                      afterEach(() => {
                        httpMock.verify();
                      });
                    
                      it('doit traiter les données nominales correctement', () => {
                        expect(true).toBeTrue();
                      });
                    
                      it('doit gérer les erreurs HTTP sans interrompre l application', () => {
                        expect(true).toBeTrue();
                      });
                    });
                    """;
        } else {
            framework = "JUnit 5 / Mockito";
            explanation = "Suite de tests unitaires JUnit 5 avec assertions AssertJ et mocks Mockito conformes au standard Spring Boot 3.";
            generatedTests = """
                    package com.valorio.test;
                    
                    import static org.assertj.core.api.Assertions.assertThat;
                    import static org.assertj.core.api.Assertions.assertThatThrownBy;
                    import static org.mockito.ArgumentMatchers.any;
                    import static org.mockito.BDDMockito.given;
                    import static org.mockito.Mockito.verify;
                    
                    import org.junit.jupiter.api.DisplayName;
                    import org.junit.jupiter.api.Test;
                    import org.junit.jupiter.api.extension.ExtendWith;
                    import org.mockito.Mock;
                    import org.mockito.junit.jupiter.MockitoExtension;
                    
                    @ExtendWith(MockitoExtension.class)
                    class ServiceValorioTest {
                    
                        @Test
                        @DisplayName("Exécute l'opération nominale avec succès")
                        void operationNominaleReussie() {
                            // Arrange & Act
                            boolean resultat = true;
                            
                            // Assert
                            assertThat(resultat).isTrue();
                        }
                    
                        @Test
                        @DisplayName("Rejette les paramètres invalides ou nuls")
                        void rejetteParametresInvalides() {
                            assertThatThrownBy(() -> {
                                throw new IllegalArgumentException("Paramètre obligatoire manquant");
                            }).isInstanceOf(IllegalArgumentException.class);
                        }
                    }
                    """;
        }

        return new TestGenerationResponse(
                generatedTests.trim(),
                framework,
                explanation,
                coveredCases,
                edgeCases,
                suggestions
        );
    }

    @Override
    public CodeReviewResponse reviewCode(CodeReviewRequest request) {
        String code = request.code() != null ? request.code() : "";
        List<ReviewFinding> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        int score = 88;

        // Analyse heuristique sur les motifs fréquents
        if (code.contains("System.out.println") || code.contains("console.log")) {
            findings.add(new ReviewFinding(
                    "LOG-001",
                    "MAINTAINABILITY",
                    "LOW",
                    "Utilisation directe de sorties console au lieu d'un logger structuré.",
                    "Détecté dans le corps de la méthode",
                    "Ligne variable",
                    "Remplacer les appels console/System.out par SLF4J (log.info) ou un service de journalisation centralisé.",
                    "AI_PREDICTED",
                    0.95
            ));
            score -= 5;
        }

        if (code.contains("catch (Exception e)") || code.contains("catch (e)")) {
            findings.add(new ReviewFinding(
                    "EXC-002",
                    "BUG_RISK",
                    "MEDIUM",
                    "Capture générique de l'exception (Exception) masquant potentiellement des erreurs critiques.",
                    "catch (Exception ...)",
                    "Bloc try-catch",
                    "Capturer des exceptions spécifiques ou propager l'erreur vers le gestionnaire global d'exceptions.",
                    "AI_PREDICTED",
                    0.90
            ));
            score -= 7;
        }

        if (code.toLowerCase().contains("password") && (code.contains("\"") || code.contains("'"))) {
            findings.add(new ReviewFinding(
                    "SEC-003",
                    "SECURITY",
                    "HIGH",
                    "Présence suspecte de mot de passe ou valeur sensible codée en dur.",
                    "Littéral de mot de passe détecté",
                    "Déclaration de variable",
                    "Externaliser les secrets dans des variables d'environnement ou le gestionnaire de configuration.",
                    "AI_PREDICTED",
                    0.85
            ));
            score -= 15;
        }

        if (findings.isEmpty()) {
            findings.add(new ReviewFinding(
                    "GEN-001",
                    "PERFORMANCE",
                    "LOW",
                    "La structure du code est propre et conforme aux standards. Pensez à vérifier l'indexation DB si des requêtes sont exécutées.",
                    "Analyse statique globale",
                    "N/A",
                    "Maintenir la couverture de tests et l'immutabilité des objets.",
                    "AI_PREDICTED",
                    0.90
            ));
        }

        recommendations.add("Appliquer une couverture de tests unitaires automatisés supérieure à 80%");
        recommendations.add("Vérifier les contraintes de non-nullité et validation Jakarta Bean Validation");
        recommendations.add("S'assurer de l'absence de fuites mémoire ou de connexions JDBC non fermées");

        String summary = String.format("Revue automatisée effectuée avec succès. Score global estimé : %d/100. %d observation(s) relevée(s).",
                Math.max(score, 40), findings.size());

        return new CodeReviewResponse(
                summary,
                Math.max(score, 40),
                findings,
                recommendations
        );
    }
}
