# Lost & Found — Guide de démarrage

## Prérequis
- Java 22 (installé ✓)
- Node 20 + Angular CLI 18 (installés ✓)
- PostgreSQL installé et en cours d'exécution

## 1. Configurer la base de données PostgreSQL

Ouvrir pgAdmin ou psql et exécuter :

```sql
CREATE DATABASE lostandfound;
```

Si ton utilisateur PostgreSQL n'est pas `postgres` ou ton mot de passe est différent,
modifier le fichier : `backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lostandfound
spring.datasource.username=postgres     ← modifier si besoin
spring.datasource.password=postgres     ← modifier si besoin
```

## 2. Démarrer le backend Spring Boot

Ouvrir un terminal dans le dossier `backend/` :

```cmd
mvnw.cmd spring-boot:run
```

Le backend démarre sur http://localhost:8081

Pour vérifier : ouvrir http://localhost:8081/api/stats dans le navigateur

## 3. Démarrer le frontend Angular

Ouvrir un **second** terminal dans le dossier `frontend/` :

```cmd
npm start
```

L'application s'ouvre sur http://localhost:4200

## Structure du projet

```
objets/
├── backend/          ← Spring Boot (port 8081)
│   └── src/main/java/com/lostandfound/backend/
│       ├── entity/       Item.java, Claim.java
│       ├── repository/   ItemRepository, ClaimRepository
│       ├── service/      ItemService, ClaimService, StatsService
│       ├── controller/   ItemController, ClaimController, StatsController
│       ├── dto/          ItemRequest/Response, ClaimRequest/Response, DashboardStats
│       ├── enums/        ItemType, ItemStatus, ClaimStatus
│       └── config/       CorsConfig, GlobalExceptionHandler
│
└── frontend/         ← Angular 18 (port 4200)
    └── src/app/
        ├── models/    item.model.ts, claim.model.ts, stats.model.ts
        ├── services/  item.service.ts, claim.service.ts, stats.service.ts
        ├── components/navbar/
        └── pages/    dashboard/, items-list/, item-form/, item-detail/, claims/
```

## API REST disponibles

| Méthode | URL                         | Description                  |
|---------|-----------------------------|------------------------------|
| GET     | /api/items                  | Tous les objets               |
| GET     | /api/items/search           | Recherche avec filtres        |
| GET     | /api/items/:id              | Détail d'un objet             |
| POST    | /api/items                  | Créer un objet                |
| PUT     | /api/items/:id              | Modifier un objet             |
| PATCH   | /api/items/:id/return       | Confirmer restitution         |
| DELETE  | /api/items/:id              | Supprimer un objet            |
| GET     | /api/claims                 | Toutes les réclamations       |
| GET     | /api/claims/item/:itemId    | Réclamations d'un objet       |
| POST    | /api/claims                 | Créer une réclamation         |
| PATCH   | /api/claims/:id/approve     | Approuver une réclamation     |
| PATCH   | /api/claims/:id/reject      | Rejeter une réclamation       |
| GET     | /api/stats                  | Statistiques du tableau bord  |
