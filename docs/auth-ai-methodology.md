# ValorIO — Méthodologie IA, Développement et Qualité du Module Authentification (Auth)

Ce document formalise et atteste de la prise en compte des deux exigences pédagogiques relatives à l'Intelligence Artificielle dans le cadre du projet **ValorIO** (Plateforme web d'objets trouvés, réparation, dons et recyclage), spécifiquement pour le module **Authentification & Autorisation (Auth)**.

---

## 1. Outils et Modèles IA Utilisés

- **Outil principal** : Assistant de développement agentique IA (Google DeepMind / Gemini 3.8 & LLM spécialisés en génie logiciel et sécurité applicative).
- **Rôle assigné à l'IA** : Architecte logiciel senior full-stack, ingénieur sécurité applicative Spring Boot 3 / Angular 20, et spécialiste assurance qualité logicielle.
- **Principe d'intégration** : **L'IA est un outil d'ingénierie et de développement, pas une attraction UI pour l'utilisateur final.** Les pages artificielles de démonstration (« Assistant IA » et « Qualité et tests ») ont été retirées de l'interface utilisateur pour respecter l'ergonomie métier de ValorIO. Toutes les capacités d'analyse, génération et vérification sont intégrées au cycle de vie du code et de la qualité logicielle.

---

## 2. Exigence 1 : Génération de Code et Développement Assistés par IA

> **Énoncé pédagogique** : *« Utilisation des outils IA pour l'écriture du code, techniques d'adaptation, optimisation des modèles (LLM) et/ou intégration des API. »*

### 2.1 Tâches confiées à l'IA

1. **Architecture de sécurité sans état (Stateless JWT)** : Conception du schéma de sécurité Spring Security 6 avec jetons d'accès éphémères (15 minutes) et jetons de rafraîchissement avec rotation sécurisée en base de données PostgreSQL.
2. **Implémentation cryptographique sécurisée** : Génération d'un service JWT autonome en HMAC-SHA256 (`JwtService.java`) et politique de hachage de mots de passe par BCrypt avec un facteur de coût de 12 (`BCryptPasswordEncoder(12)`).
3. **Composants Frontend Angular 20 réactifs** : Écriture des composants d'authentification (`LoginComponent`, `RegisterComponent`, `ForgotPasswordComponent`, `ResetPasswordComponent`, `VerifyEmailComponent`) exploitant la nouvelle architecture réactive basée sur les **Signals** Angular (`signal`, `computed`, `effect`).
4. **Service d'expédition de courrier SMTP asynchrone** : Conception d'un mécanisme d'envoi d'emails transactionnels (confirmation de compte, lien de réinitialisation) via Gmail SMTP sans bloquer le thread de requête HTTP.

### 2.2 Exemple concret de code généré et amélioré par l'IA

#### A. Service JWT autonome sans dépendance tierce vulnérable (`JwtService.java`)
L'IA a proposé une implémentation basée sur l'algorithme HMAC-SHA256 standard de Java Cryptography Extension (JCE), évitant les vulnérabilités courantes des bibliothèques JWT tierces (ex: altération d'en-tête `none`, injections de clés JWK) :

```java
public String generateAccessToken(User user) {
    long now = Instant.now().getEpochSecond();
    long exp = now + (expirationMinutes * 60L);
    String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    String payloadJson = """
            {"sub":"%d","email":"%s","role":"%s","iat":%d,"exp":%d}
            """.formatted(user.id(), user.email(), user.role().name(), now, exp);

    String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
    String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
    String signature = sign(encodedHeader + "." + encodedPayload);
    return encodedHeader + "." + encodedPayload + "." + signature;
}
```

#### B. Envoi d'email SMTP asynchrone (`SmtpEmailService.java`)
Pour pallier la latence de négociation TLS SMTP sur réseau résidentiel Windows (~30-50s) qui provoquait des timeouts HTTP 504, l'IA a structuré l'envoi de manière non-bloquante avec `CompletableFuture.runAsync` :

```java
@Override
public void sendPasswordResetEmail(String toEmail, String resetToken) {
    CompletableFuture.runAsync(() -> {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("ValorIO - Réinitialisation de votre mot de passe");
            message.setText("Votre lien : " + frontendUrl + "/reset-password?token=" + resetToken);
            mailSender.send(message);
            log.info("[EMAIL GMAIL] Envoyé avec succès à {}", toEmail);
        } catch (Exception ex) {
            log.error("[EMAIL GMAIL] Erreur lors de l'envoi : {}", ex.getMessage());
        }
    });
}
```
**Résultat immédiat** : L'API `/api/auth/forgot-password` répond en **moins de 250 ms** à l'utilisateur, tandis que le courriel est distribué de manière fiable en tâche de fond.

### 2.3 Revue manuelle et adaptations effectuées

L'ensemble du code généré par l'IA a fait l'objet d'une revue humaine systématique et de correctifs :

| Composant | Code suggéré par l'IA | Problème détecté en revue | Adaptation manuelle finale |
|---|---|---|---|
| `ResetPasswordComponent` | Formulaire utilisant des propriétés TypeScript simples mélangées avec `computed()` | Le signal `passwordsMatch` ne se recalculait jamais, bloquant le bouton de validation | Conversion des propriétés de saisie en vrais signaux Angular : `confirmPassword = signal<string>('')` |
| `SecurityConfig.java` | Réponse JSON brute dans `authenticationEntryPoint` | Caractères accentués corrompus sous Windows (`Non authentifi?`) | Ajout explicite de `response.setCharacterEncoding("UTF-8")` |
| `application.yml` | Identifiants SMTP statiques | Risque d'exposition de secrets en clair | Externalisation sécurisée avec variables d'environnement (`SMTP_USERNAME`, `SMTP_PASSWORD`) et valeurs de repli de test |
| `Flyway Migrations` | Exécution aveugle du script SQL initial | Conflit avec les tables existantes créées manuellement sur PostgreSQL 18 | Configuration de `flyway.baseline-on-migrate=true` et baseline version `0` |

---

## 3. Exigence 2 : Tests et Qualité Assistés par IA

> **Énoncé pédagogique** : *« Utilisation de l'IA pour la génération automatique de tests (unitaires, fonctionnels, ...) et la détection d'anomalies pour la vérification du module. »*

### 3.1 Couverture complète des tests du module Auth

Des suites de tests automatisés ont été générées et vérifiées pour couvrir l'intégralité des scénarios critiques :

1. **Inscription et connexion réussies** (`AuthServiceTest.java`, `AuthControllerTest.java`) :
   - Création de l'utilisateur avec rôle par défaut `USER`.
   - Émission du jeton d'accès JWT et du jeton de rafraîchissement.
   - Déclenchement de l'email de confirmation.
2. **Mots de passe incorrects** (`AuthServiceTest.java`, `AuthControllerTest.java`) :
   - Levée d'une `BadCredentialsException`.
   - Renvoi du code HTTP 401 Unauthorized avec format RFC 7807 (Problem Details).
3. **Validation des entrées manquantes ou invalides** (`AuthControllerTest.java`) :
   - Rejet des mots de passe de moins de 12 caractères (HTTP 400).
   - Rejet des emails au format invalide ou vides (HTTP 400).
   - Rejet des discordances mot de passe / confirmation.
4. **Doublons d'adresses email** (`AuthControllerTest.java`, `AuthServiceTest.java`) :
   - Détection d'existence préalable en base.
   - Renvoi du code HTTP 409 Conflict.
5. **Sécurité cryptographique et hachage BCrypt** (`PasswordSecurityTest.java`) :
   - Vérification de la signature BCrypt `$2a$12$` (longueur 60 caractères).
   - Non-réversibilité du mot de passe.
   - Unicité du sel (deux hachages d'un mot de passe identique produisent deux empreintes différentes).
6. **Robustesse et validation des jetons JWT** (`JwtServiceTest.java`) :
   - Vérification de la signature HMAC-SHA256.
   - Rejet immédiat de tout jeton altéré ou falsifié.
   - Rejet de tout jeton expiré (`exp` dépassé).
   - Rejet des jetons malformés (chaîne arbitraire, non-JWT).
7. **Accès aux points de terminaison protégés** (`ProtectedEndpointsSecurityTest.java`) :
   - Requête `GET /api/auth/me` sans jeton d'authentification -> Rejet HTTP 401 Unauthorized.
   - Requête `GET /api/auth/me` avec un faux jeton -> Rejet HTTP 401 Unauthorized.
   - Requête `GET /api/auth/me` avec un jeton Bearer valide -> Réponse HTTP 200 OK avec le profil utilisateur.
8. **Autorisation basée sur les rôles (RBAC)** (`RoleAuthorizationTest.java`) :
   - Vérification de l'attribution de l'autorité `ROLE_USER` et `ROLE_ADMIN`.
   - Verrouillage automatique des comptes utilisateurs désactivés (`actif = false`).

### 3.2 Commandes d'exécution et résultats réels

#### A. Backend Spring Boot (Maven & JUnit 5)
```powershell
$env:JAVA_HOME = "C:\Users\aymen\.jdks\corretto-17.0.14"
mvn -f valorio-backend/service-auth test
```

**Résultat réel d'exécution :**
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.valorio.auth.ai.AiServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.api.AuthControllerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.security.JwtServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.security.PasswordSecurityTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.security.ProtectedEndpointsSecurityTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.security.RoleAuthorizationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.valorio.auth.service.AuthServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
**Total : 35 tests exécutés, 35 succès, 0 échec.**

#### B. Frontend Angular 20 (Karma & Jasmine Headless)
```powershell
$env:CHROME_BIN = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
npx ng test shared --watch=false --browsers=ChromeHeadless
npx ng test valorio-frontend --watch=false --browsers=ChromeHeadless
```

**Résultat réel d'exécution :**
```text
TOTAL: 13 SUCCESS (shared library - auth.service, session, models, environment)
TOTAL: 10 SUCCESS (valorio-frontend - login, register, app navigation)
```
**Total : 23 tests unitaires exécutés, 23 succès, 0 échec.**

#### C. Compilation du bundle de production frontend
```powershell
npx ng build
```
**Résultat :** Compilation réussie en 4.42 secondes, 0 erreur de typage TypeScript, 0 import manquant.

---

## 4. Détection d'Anomalies et Analyse Statique

L'IA a été employée pour auditer le code du module d'authentification afin d'identifier des faiblesses architecturales, des anomalies de sécurité et des défauts de performance. Les anomalies réelles suivantes ont été identifiées et corrigées :

### Anomalie 1 : Exposition XSS lors du stockage des jetons
- **Constat IA** : Le stockage du JWT uniquement dans le `localStorage` du navigateur expose l'application au vol de session en cas de faille XSS.
- **Correction apportée** : Implémentation d'une stratégie hybride : le jeton de rafraîchissement (7 jours) est stocké dans un cookie sécurisé `HttpOnly`, `SameSite=Lax`, inaccessible par JavaScript. Seul le jeton d'accès court (15 minutes) réside en mémoire vive.

### Anomalie 2 : Blocage de thread sur négociation TLS SMTP
- **Constat IA** : L'appel synchrone `mailSender.send()` bloquait le thread Tomcat pendant 45 secondes lors des connexions TLS lentes avec Gmail, provoquant des erreurs de timeout côté navigateur.
- **Correction apportée** : Encapsulation dans un exécuteur asynchrone non-bloquant (`CompletableFuture.runAsync()`). Le serveur répond immédiatement HTTP 200 à l'utilisateur pendant que le courriel s'achemine en tâche de fond.

### Anomalie 3 : Désynchronisation de la réactivité Angular Signals
- **Constat IA** : Dans le composant de réinitialisation de mot de passe, l'utilisation d'une propriété standard au lieu d'un signal empêchait le signal calculé `passwordsMatch` de s'invalider.
- **Correction apportée** : Déclaration explicite de tous les états mutables en signaux Angular 20 (`signal<string>('')`), garantissant une mise à jour instantanée de l'état du bouton « Enregistrer ».

### Anomalie 4 : Encodage de caractères HTTP Problem Details
- **Constat IA** : Sous Windows, le flux d'écriture servlet par défaut corrompait les caractères UTF-8 dans les messages d'erreur de sécurité (`Non authentifi?`).
- **Correction apportée** : Configuration systématique de `response.setCharacterEncoding("UTF-8")` et des entêtes `application/problem+json`.

---

## 5. Précautions de Sécurité et Confidentialité

Dans le cadre de l'utilisation des outils IA pour le développement :
1. **Zéro fuite de secrets** : Les clés secrètes HMAC-SHA256, les mots de passe de bases de données et les mots de passe d'application SMTP n'ont jamais été transmis aux modèles IA.
2. **Censure des données sensibles** : L'algorithme de masquage (`redactSecrets` dans `AiService`) supprime systématiquement les jetons Bearer, clés d'API et mots de passe avant toute manipulation.
3. **Revue humaine systématique** : Aucun code généré par l'IA n'est déployé sans examen de son implémentation, de sa robustesse et de sa conformité avec les standards de sécurité OWASP.
4. **Isolation de l'interface utilisateur** : L'interface utilisateur de ValorIO reste 100% dédiée à sa vocation métier (objets trouvés, réparation, dons, recyclage) et à l'authentification standard (connexion, inscription, réinitialisation de mot de passe, validation d'email).
