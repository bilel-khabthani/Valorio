# FICHE DE VALIDATION INDIVIDUELLE : MODULE MARKETPLACE
## Module : IA for Software Engineering
**Projet :** Lost & Found (Gestion des Objets Perdus & Trouvés)  
**Partie Individuelle :** Module 3 - Marketplace & Revalorisation Solidaire  
**Technologies :** Spring Boot 3, Angular 17, PostgreSQL / H2, JUnit 5, Mockito  

---

## 🎯 Résumé pour la Démonstration (À dire au début)
> *"Bonjour Monsieur/Madame. Dans le cadre de notre projet Lost & Found, ma partie est le **Marketplace**. Son rôle est de permettre aux étudiants et demandeurs de récupérer, acheter à prix solidaire ou recevoir en don les objets trouvés non réclamés à l'issue du délai légal de garde, ainsi que d'échanger directement via un système de chat. Mon travail a été centré sur deux piliers de l'IA : la génération de code et l'adaptation des modèles LLM d'un côté, et l'assurance qualité avec détection d'anomalies et tests automatisés de l'autre."*

---

## 1. Génération de Code et Développement avec l'IA

### 1.1 Utilisation des outils IA pour l'écriture du code
* **Architecture Backend (Spring Boot 3) :**
  * L'IA a été utilisée pour concevoir l'architecture en couches (*Controller, Service, Repository, DTO, Entity*).
  * Génération des entités JPA [`MarketplaceItem.java`](file:///c:/Users/user/Desktop/AI/backend/src/main/java/com/lostandfound/marketplace/model/MarketplaceItem.java) et [`MarketplaceOffer.java`](file:///c:/Users/user/Desktop/AI/backend/src/main/java/com/lostandfound/marketplace/model/MarketplaceOffer.java).
* **Architecture Frontend (Angular 17) :**
  * Conception d'une interface ergonomique mono-page (Style Valorio) épurée, réactive et connectée via `HttpClient`.

### 1.2 Techniques d'adaptation et Ingénierie des Prompts (*Prompt Engineering*)
* **Problématique métier :** Un utilisateur saisit souvent une description brute ou rapide (ex: *"Sac à dos Dell noir avec chargeur oublié en amphi"*).
* **Technique d'adaptation mise en œuvre :**
  * Mise en place de techniques de *few-shot prompting* et de normalisation NLP dans [`AiService.java`](file:///c:/Users/user/Desktop/AI/backend/src/main/java/com/lostandfound/marketplace/service/AiService.java).
  * L'algorithme extrait automatiquement l'objet central, détecte la catégorie marchande (`ELECTRONICS`, `BAGS_LUGGAGE`, etc.), évalue l'état d'usure et formate une annonce commerciale claire.

### 1.3 Optimisation des Modèles et Intégration API (Architecture Hybride)
* **Architecture Hybride (Cloud + Local) :**
  * Le service est conçu pour se connecter à une API de LLM externe (OpenAI / Gemini) si une clé est configurée.
  * **Optimisation & Résilience :** Pour éviter tout échec réseau ou problème de quota lors de la démonstration, un moteur heuristique local déterministe prend le relais de manière transparente sans aucune coupure de service.

---

## 2. Tests, Qualité et Détection d'Anomalies avec l'IA

### 2.1 Utilisation de l'IA pour la génération automatique de tests
* L'IA a été exploitée pour générer les scénarios de tests unitaires et fonctionnels, en ciblant particulièrement les cas limites (*edge cases*) et les variations linguistiques (casse des marques type *iPhone*, *macOS*, prix nuls, données incomplètes).
* **Suite de tests :** 10 tests automatisés avec **JUnit 5** et **Mockito** dans [`AiServiceTest.java`](file:///c:/Users/user/Desktop/AI/backend/src/test/java/com/lostandfound/marketplace/service/AiServiceTest.java) et [`MarketplaceItemServiceTest.java`](file:///c:/Users/user/Desktop/AI/backend/src/test/java/com/lostandfound/marketplace/service/MarketplaceItemServiceTest.java).
* **Preuve d'exécution (À exécuter devant l'enseignant) :**
  ```powershell
  cd c:\Users\user\Desktop\AI\backend
  .\mvnw.cmd test
  ```
  > **Résultat :** `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` (**100% SUCCÈS**).

### 2.2 Détection d'Anomalies et Sécurité en Temps Réel
Le module intègre un moteur d'analyse automatique pour fiabiliser les données :
1. **Modération de contenu et filtre légal :**
   * L'IA détecte et bloque automatiquement la mise en vente de documents d'identité officiels (passeports, cartes d'identité, permis) ou d'objets illicites.
2. **Détection d'anomalies de tarification (*Outlier Detection*) :**
   * Détection des incohérences de type : un don solidaire gratuit enregistré avec un prix $> 0$ € (`PRICE_TYPE_MISMATCH`).
   * Détection des prix anormalement bas pour du matériel haut de gamme (ex: iPhone à 2 €) afin de prévenir les erreurs de saisie et les fraudes.
3. **Modèle de tarification juste (*Fair Pricing*) :**
   * Estimation algorithmique de la décote de vétusté :
     $$\text{Prix}_{\text{estimé}} = \text{Benchmark}_{\text{catégorie}} \times \mu_{\text{condition}}$$
   * Affichage d'un badge de conformité et d'un score de sécurité (ex. 98% vérifié IA).

---

## 3. Déroulement Chronologique de votre Présentation Orale (5 Minutes)

| Étape | Ce que vous montrez à l'écran | Ce que vous dites |
|---|---|---|
| **Min 0 - 1** | Page principale [http://localhost:4200](http://localhost:4200) | *"Voici la plateforme Marketplace (style Valorio). Elle regroupe les objets trouvés non réclamés. Chaque objet affiche son statut, son prix solidaire ou don gratuit, et son score de vérification IA."* |
| **Min 1 - 2.5** | Clic sur **`+ Déposer un objet égaré / annonce`** | *"Pour le dépôt, j'ai intégré un assistant IA 1-clic. Si un utilisateur tape une phrase simple comme 'Sac à dos Dell noir avec chargeur', un simple clic sur 'Remplir avec l'IA' extrait automatiquement la catégorie Sacs, estime l'état, calcule le prix juste (42 €) et génère la fiche complète."* -> Cliquer sur **`Publier l'annonce`**. |
| **Min 2.5 - 3.5** | Clic sur le bouton **`Chat`** sur une ligne | *"Pour la partie transaction, chaque objet dispose d'un espace de Chat direct permettant au demandeur et à l'acquéreur d'échanger en temps réel pour convenir du lieu de retrait."* -> Taper un message et l'envoyer. |
| **Min 3.5 - 5** | Terminal dans `backend` avec `.\mvnw.cmd test` | *"Pour la partie Qualité et IA, j'ai utilisé l'IA pour générer 10 cas de tests automatisés couvrant les cas limites, le blocage des passeports, la décote et la détection des prix aberrants. Tous les tests passent à 100%."* |

---

## 4. Réponses aux Questions Probables de l'Enseignant

* **Q : Pourquoi avoir choisi une architecture Spring Boot + Angular ?**  
  * **R :** *"Pour garantir une séparation claire des responsabilités : une API REST robuste, typée et sécurisée avec Spring Boot 3 et JPA/Hibernate côté backend, et une interface réactive et ergonomique avec Angular 17 côté frontend."*
* **Q : Comment fonctionne votre détection d'anomalies ?**  
  * **R :** *"Elle compare en temps réel les données saisies avec les modèles statistiques de référence (prix moyen par catégorie et coefficient d'usure). Si l'écart dépasse un seuil de tolérance (ex. iPhone à 2€ ou don avec prix payant), l'anomalie est signalée avec des recommandations de correction."*
* **Q : Votre application fonctionne-t-elle sans connexion Internet ?**  
  * **R :** *"Oui, grâce à notre conception hybride, l'application fonctionne à 100% en local sur une seule machine avec Spring Boot et H2/PostgreSQL, tout en restant interfaçable avec les API OpenAI ou Gemini."*
