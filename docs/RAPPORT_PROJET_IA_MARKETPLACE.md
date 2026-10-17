# RAPPORT DE PROJET : MODULE MARKETPLACE & REVALORISATION IA
## Module : IA for Software Engineering
**Projet Collectif :** Plateforme de Gestion des Objets Perdus & Trouvés (*Lost & Found*)  
**Partie Individuelle :** Module 3 - Marketplace & Revalorisation Économique Assistée par IA  
**Auteur :** Étudiant en charge du Module Marketplace  
**Date de Rendu :** Octobre 2026 (Soumission Blackboard)  
**Stack Technique :** Spring Boot 3 (Java 17), Angular 17, PostgreSQL / H2, JUnit 5, Mockito  

---

## 1. Contexte & Périmètre du Module Marketplace

Dans une infrastructure de gestion des objets perdus et trouvés, une proportion importante d'objets reste non réclamée au terme des délais réglementaires de garde (ex. 90 à 180 jours selon le code civil et les arrêtés municipaux).

Le module **Marketplace** répond à un triple enjeu au sein de la plateforme unifiée :
1. **Ergonomie Épurée & Clarté Visuelle (Style Valorio) :** Une interface sur une seule page claire, intuitive, avec tableau d'annonces direct, système de chat instantané et dépôt simplifié.
2. **Économie Circulaire & Seconde Vie :** Permettre la revente solidaire à prix maîtrisé d'équipements vérifiés (smartphones, ordinateurs, vélos, instruments, etc.).
3. **Solidarité & Dons Gratuits (0 €) :** Offrir des biens de première nécessité (vêtements chauds, manteaux, fournitures, parapluies) aux étudiants et personnes vulnérables.
4. **Récompenses & Restitution Sécurisée (*Reward Bounty*) :** Permettre aux propriétaires d'objets perdus à haute valeur affective de publier des annonces avec promesse de récompense financière lors de la restitution.
5. **Négociations & Chat en direct :** Permettre aux demandeurs et acquéreurs d'échanger en direct via une fenêtre de chat intégrée.

---

## 2. Intégration de l'Intelligence Artificielle dans le Cycle de Développement

Conformément aux exigences d'évaluation du module *IA for Software Engineering*, l'IA n'est pas un simple composant cosmétique mais est intégrée à double niveau : dans la conception/génération du code et comme moteur d'inférence en production.

### 2.1 Génération de Code et Ingénierie des Prompts (*Prompt Engineering*)
- **Scaffolding & Architecture Clean :** Utilisation des LLMs pour générer le socle architectural suivant les patrons Repository, Service, DTO, Controller et JPA Entities sous Spring Boot 3.
- **Techniques d'Adaptation & Few-Shot Prompting :**
  - Mise en place d'un système de prompt structuré permettant de convertir des notes brutes textuelles saisies par un agent d'accueil (ex: *"AirPods Pro 2 trouvés gare, boitier rayé"*) en une annonce marchande valorisante avec découpage Markdown, détection automatique de la catégorie et extraction de balises (*tags*).
  - Utilisation d'un moteur hybride : intégration d'une passerelle API LLM (OpenAI / Gemini) complétée par un moteur heuristique déterministe hors-ligne garantissant une résilience à 100% lors de la démonstration machine sans dépendance réseau.

---

## 3. Fonctionnalités IA du Module Marketplace

### 3.1 Générateur d'Annonces Intelligent (*Smart Listing Generator*)
- **Endpoint :** `POST /api/marketplace/ai/generate-listing`
- **Entrées :** Mots-clés bruts de l'objet, catégorie optionnelle, état constaté.
- **Sorties :** Titre commercial engageant, description Markdown avec mentions légales de conservation, fourchette tarifaire suggérée et tags d'indexation.

### 3.2 Modèle d'Estimation Économétrique & IA (*Smart Valuation Engine*)
- **Endpoint :** `POST /api/marketplace/ai/estimate-price`
- **Formulation mathématique :**
  $$\text{Prix}_{\text{recommandé}} = \text{Benchmark}_{\text{catégorie}} \times \mu_{\text{condition}} \times (1 - \delta_{\text{âge}})$$
  où :
  - $\text{Benchmark}_{\text{catégorie}}$ : indice de référence moyen de la catégorie marchande.
  - $\mu_{\text{condition}}$ : coefficient d'usure physique (Neuf : $1.00$, Comme neuf : $0.85$, Bon état : $0.65$, État moyen : $0.40$, Pour pièces : $0.15$).
  - $\delta_{\text{âge}}$ : fonction de dépréciation technologique.
- **Explicabilité (*Explainability*) :** L'IA renvoie un diagnostic complet comprenant le pourcentage de décote appliqué et les facteurs de liquidité sur le marché secondaire.

### 3.3 Modérateur de Sécurité & Filtrage Éthique (*Content Moderation*)
- **Endpoint :** `POST /api/marketplace/ai/moderate`
- **Règles d'exclusion automatique :**
  - **Documents d'identité officiels :** Blocage strict de la revente de passeports, cartes d'identité, cartes bancaires ou permis de conduire (obligation de restitution aux autorités).
  - **Objets dangereux et illicites :** Détection d'armes, stupéfiants, substances réglementées ou contrefaçons.
  - **Suspicion d'arnaque tarifaire :** Détection des montants extravagants (ex. $> 50\,000$ €).
- **Indicateur :** Attribution d'un score de sécurité (0 à 100%) et classification du niveau de risque (*LOW*, *HIGH*, *BLOCKED*).

### 3.4 Détecteur d'Anomalies Statistiques (*Anomaly Detection*)
- **Endpoint :** `POST /api/marketplace/ai/detect-anomaly`
- **Anomalies couvertes :**
  1. `PRICE_TYPE_MISMATCH` : Annonce de don solidaire gratuite enregistrée avec un prix $> 0$ €.
  2. `ZERO_PRICE_SALE` : Annonce marchande avec prix nul sans statut de don.
  3. `PRICE_ABNORMALLY_LOW` : Équipement de grande valeur (ex. iPhone 15) listé à un tarif dérisoire (ex. 2 €), signe d'une erreur de saisie ou d'une tentative de fraude.
  4. `PRICE_ABNORMALLY_HIGH` : Prix supérieur au triple de la borne haute du marché.

---

## 4. Tests et Assurance Qualité Assistés par l'IA

Conformément à la seconde consigne majeure de l'évaluation :
> *"Tests et qualité : Utilisation de l'IA pour la génération automatique de tests (unitaires, fonctionnels, ...) et la détection d'anomalies pour la vérification du module."*

### 4.1 Stratégie de Test Automatisée
Une suite complète de 10 tests unitaires et d'intégration a été conçue sous **JUnit 5** et **Mockito** :

| Classe de Test | Méthode de Test | Couverture & Rôle de l'IA | Résultat |
|---|---|---|---|
| `AiServiceTest` | `testGenerateListingFromRawPrompt` | Génération de prompt structuré et parsing | **SUCCÈS (PASS)** |
| `AiServiceTest` | `testPriceEstimationWithDepreciation` | Validation de la courbe de décote et de l'explicabilité | **SUCCÈS (PASS)** |
| `AiServiceTest` | `testModerationBlocksPassportOrIdentitySale` | Vérification du blocage strict des pièces d'identité | **SUCCÈS (PASS)** |
| `AiServiceTest` | `testModerationApprovesLegitimateItem` | Validation des annonces saines (score $\ge 90\%$) | **SUCCÈS (PASS)** |
| `AiServiceTest` | `testAnomalyDetectionDonationWithPrice` | Détection de l'incohérence prix / don solidaire | **SUCCÈS (PASS)** |
| `AiServiceTest` | `testAnomalyDetectionPriceAbnormallyLow` | Détection de prix aberrant pour équipement high-tech | **SUCCÈS (PASS)** |
| `MarketplaceItemServiceTest` | `testCreateItemWithAiEnrichment` | Enrichissement automatique persistant (JPA) | **SUCCÈS (PASS)** |
| `MarketplaceItemServiceTest` | `testGetItemByIdIncrementsViews` | Incrémentation des vues lors de la consultation | **SUCCÈS (PASS)** |
| `MarketplaceItemServiceTest` | `testUpdateStatus` | Cohérence des transitions d'état (*AVAILABLE* $\to$ *RESERVED* $\to$ *SOLD*) | **SUCCÈS (PASS)** |
| `MarketplaceApplicationTests` | `contextLoads` | Test d'intégration Spring Boot, Hibernate et DB | **SUCCÈS (PASS)** |

**Bilan d'exécution :** `10 Tests run, Failures: 0, Errors: 0, Skipped: 0` (100% de succès).

### 4.2 Apport de l'IA dans l'Assurance Qualité
- **Génération de Cas Limites (*Boundary & Edge Cases*) :** Utilisation des techniques de test assisté par IA pour identifier les entrées non conventionnelles (variations de casse sur les marques type *iPhone*, *macOS*, textes multilingues, prix négatifs ou nuls).
- **Détection Proactive d'Anomalies :** Le système intègre en production le modèle d'anomalie testé, empêchant les données corrompues d'entrer en base.

---

## 5. Architecture Logicielle & Modèle de Données

### 5.1 Diagramme Entité-Association (JPA)
```
+-------------------------------------------------+
|                MARKETPLACE_ITEMS                |
+-------------------------------------------------+
| PK id                 : BIGINT (Auto-increment) |
|    title              : VARCHAR(200)            |
|    description        : TEXT                    |
|    category           : VARCHAR(50) [ENUM]      |
|    item_condition     : VARCHAR(50) [ENUM]      |
|    item_type          : VARCHAR(50) [ENUM]      |
|    status             : VARCHAR(50) [ENUM]      |
|    price              : NUMERIC(10,2)           |
|    ai_estimated_price : NUMERIC(10,2)           |
|    ai_price_fairness  : VARCHAR(50)             |
|    ai_verification_sc : DOUBLE                  |
|    ai_moderation_notes: VARCHAR(500)            |
|    original_object_id : VARCHAR(100)            |
|    location           : VARCHAR(200)            |
|    seller_name        : VARCHAR(150)            |
|    seller_email       : VARCHAR(150)            |
|    image_url          : VARCHAR(1000)           |
|    tags               : VARCHAR(500)            |
|    unclaimed_days     : INTEGER                 |
|    views_count        : INTEGER                 |
|    created_at         : TIMESTAMP               |
|    updated_at         : TIMESTAMP               |
+-------------------------------------------------+
                        | 1
                        |
                        | *
+-------------------------------------------------+
|                MARKETPLACE_OFFERS               |
+-------------------------------------------------+
| PK id                 : BIGINT (Auto-increment) |
| FK item_id            : BIGINT                  |
|    buyer_name         : VARCHAR(150)            |
|    buyer_email        : VARCHAR(150)            |
|    buyer_phone        : VARCHAR(50)             |
|    offered_price      : NUMERIC(10,2)           |
|    message            : TEXT                    |
|    status             : VARCHAR(30) [ENUM]      |
|    created_at         : TIMESTAMP               |
+-------------------------------------------------+
```

### 5.2 Endpoints REST de l'API
- `GET /api/marketplace/items` : Recherche multicritères (recherche plein texte, catégorie, type, statut, prix min/max).
- `GET /api/marketplace/items/{id}` : Consultation détaillée avec compteur de consultations.
- `POST /api/marketplace/items` : Création avec audit automatique et enrichissement IA.
- `PUT /api/marketplace/items/{id}` : Mise à jour complète de l'annonce.
- `PATCH /api/marketplace/items/{id}/status` : Transition d'état (Disponible / Réservé / Vendu).
- `DELETE /api/marketplace/items/{id}` : Suppression d'une annonce.
- `GET /api/marketplace/items/stats` : Métriques globales agrégées.
- `GET /api/marketplace/offers` : Liste des négociations et offres d'acquisition.
- `POST /api/marketplace/offers/item/{itemId}` : Soumission d'une proposition sur un objet.
- `PATCH /api/marketplace/offers/{id}/status` : Acceptation ou refus avec mise à jour automatique du statut de l'objet.
- `POST /api/marketplace/ai/generate-listing` : Générateur LLM d'annonces.
- `POST /api/marketplace/ai/estimate-price` : Évaluation économétrique de décote.
- `POST /api/marketplace/ai/moderate` : Modérateur éthique et filtre de sécurité.
- `POST /api/marketplace/ai/detect-anomaly` : Moteur de détection d'anomalies.

---

## 6. Guide de Démonstration pour la Soutenance Orale (Interface Valorio)

Lors de la présentation individuelle devant l'enseignant, suivre ce déroulé fluide et direct de 5 minutes :
1. **Introduction & Ergonomie Valorio (1 min) :**
   - Présenter le rôle du Marketplace dans le projet global *Lost & Found* (revalorisation des objets après expiration du délai légal et dons solidaires).
   - Montrer l'interface épurée sur une seule page (`http://localhost:4200/`) avec la barre de navigation, le tableau direct `# | OBJET | DEMANDEUR | CENTRE / LIEU | STATUT / PRIX | ACTIONS` et les objets préchargés (Ordinateur portable, iPhone 13, Manteau solidaire, Vélo Rockrider).
2. **Démonstration du Dépôt d'Annonce & Remplissage IA 1-Clic (1.5 min) :**
   - Cliquer sur le bouton bleu **`+ Déposer un objet égaré / annonce`**.
   - Taper dans l'encadré IA : *"Sac à dos Dell noir oublié en amphi B"* puis cliquer sur **`Remplir avec l'IA`**.
   - Montrer à l'enseignant que le titre, la catégorie, la condition et le prix estimé se remplissent instantanément.
   - Cliquer sur **`Publier l'annonce`** : elle s'insère immédiatement en haut du tableau.
3. **Démonstration du Chat en Direct (1 min) :**
   - Cliquer sur le bouton **`Chat`** sur l'une des lignes du tableau.
   - Montrer la fenêtre de discussion épurée permettant aux étudiants et demandeurs d'échanger en direct pour récupérer l'objet.
   - Taper un message (ex: *"Bonjour, je peux passer vendredi à 14h"*) et l'envoyer.
4. **Preuve de Qualité & Tests Automatiques (1.5 min) :**
   - Exécuter en terminal : `.\mvnw.cmd test` dans le dossier `backend`.
   - Montrer l'affichage `BUILD SUCCESS (10 Tests run, 0 failures, 100% de réussite)` couvrant les algorithmes d'IA (génération, décote, modération des pièces d'identité et détection d'anomalies de prix).

---

## 7. Conclusion

Le module Marketplace répond rigoureusement à l'ensemble du cahier des charges :
- **Autonomie & Intégration :** Fonctionnel sur une seule machine avec Spring Boot 3 et Angular 17.
- **Robustesse des Données :** Compatible PostgreSQL en production et H2 en mémoire pour la soutenance sans dépendances externes complexes.
- **Excellence IA :** Implémentation concrète de 4 fonctionnalités IA majeures (Génération, Estimation, Modération, Détection d'anomalies).
- **Assurance Qualité :** 10 tests unitaires et d'intégration validant le code avec 100% de réussite.
