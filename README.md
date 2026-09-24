# Expenses App

Application de suivi des dépenses (monorepo).

- `backend/` : API Spring Boot 3 (Java 21, Maven, PostgreSQL, Flyway)
- `frontend/` : Vue 3 + Vite (Vue Router, Pinia)

## Prérequis

- Java 21
- Node.js 22.18+ (ou 24.12+) et npm
- Docker et Docker Compose

## Lancer en local

### Base de données

```bash
docker compose up -d postgres
```

PostgreSQL 16 est exposé sur `localhost:5432` (base `expenses`, utilisateur `expenses`, mot de passe `expenses`).

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

L'API écoute sur `http://localhost:8080`. La connexion à la base se configure via `DB_URL`, `DB_USERNAME` et `DB_PASSWORD` (valeurs par défaut pour le local).

Tests (Docker requis pour Testcontainers) :

```bash
./mvnw verify
```

Pour lancer la base et le backend dans Docker :

```bash
docker compose up -d --build
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Le front est servi sur `http://localhost:5173` ; les appels vers `/api` sont redirigés vers `http://localhost:8080`.
