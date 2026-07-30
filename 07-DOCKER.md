# 🐳 Étape 7 : Conteneurisation Docker (backend + frontend React)

## 🎯 Objectif

Packager le backend Spring Boot **et** le frontend React chacun dans leur propre image Docker optimisée, et les faire tourner ensemble avec PostgreSQL via Docker Compose.

## 📋 Pré-requis

- Étapes 3 à 6 validées
- Structure `backend/` + `frontend/` en place (voir étape 5.1)
- Docker et Docker Compose installés

---

## 🧩 Sous-étapes détaillées

### 7.1 Ajouter le driver PostgreSQL au backend

Dans `backend/pom.xml` :

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] Dépendance ajoutée

### 7.2 Créer un profil de configuration "docker" pour le backend

Fichier : `backend/src/main/resources/application-docker.properties`

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://db:5432/taskdb
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false

spring.h2.console.enabled=false
```

Mettre à jour `CorsConfig.java` pour autoriser aussi l'origine du frontend conteneurisé :

```java
registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "http://localhost:80", "http://localhost")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
```

- [ ] Fichier créé et CORS mis à jour

### 7.3 Dockerfile du backend

Fichier : `backend/Dockerfile`

```dockerfile
# ---- Stage 1 : Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Stage 2 : Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/task-manager-*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Fichier : `backend/.dockerignore`

```
target/
.git/
*.iml
.idea/
```

- [ ] Fichiers créés
- [ ] `docker build -t task-manager-backend:local ./backend` réussit

### 7.4 Dockerfile du frontend (build React + Nginx)

Fichier : `frontend/Dockerfile`

```dockerfile
# ---- Stage 1 : Build React ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# L'URL de l'API est injectée au build via une variable d'environnement Vite
ARG VITE_API_BASE_URL=http://localhost:8080/api/tasks
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ---- Stage 2 : Servir via Nginx ----
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

Fichier : `frontend/nginx.conf`

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Support du routing côté client (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

Fichier : `frontend/.dockerignore`

```
node_modules/
dist/
.git/
```

- [ ] Fichiers créés
- [ ] `docker build -t task-manager-frontend:local ./frontend` réussit

> ⚠️ **Point d'attention Vite/Docker** : les variables `VITE_*` sont injectées **au moment du build**, pas à l'exécution. Si l'URL du backend change entre environnements, il faut soit reconstruire l'image avec un `--build-arg` différent, soit passer par un reverse-proxy Nginx qui redirige `/api` vers le backend (voir variante ci-dessous).

### 7.5 (Recommandé) Variante : Nginx comme reverse-proxy vers l'API

Pour éviter de rebuilder le frontend à chaque changement d'URL backend, on peut faire pointer le frontend sur `/api` (chemin relatif) et laisser Nginx proxier vers le backend :

Mettre à jour `frontend/.env` :

```
VITE_API_BASE_URL=/api/tasks
```

Mettre à jour `frontend/nginx.conf` :

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

- [ ] Décider quelle variante utiliser (URL fixe au build vs reverse-proxy) - **le reverse-proxy est recommandé pour la suite (étape 8)**
- [ ] Avec le reverse-proxy, CORS n'est même plus nécessaire en production (même origine)

### 7.6 Créer le `docker-compose.yml` (à la racine)

```yaml
version: "3.8"

services:
  backend:
    build: ./backend
    container_name: task-manager-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
    depends_on:
      db:
        condition: service_healthy
    networks:
      - taskmanager-net

  frontend:
    build: ./frontend
    container_name: task-manager-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - taskmanager-net

  db:
    image: postgres:15-alpine
    container_name: task-manager-db
    environment:
      POSTGRES_DB: taskdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - taskmanager-net

volumes:
  pgdata:

networks:
  taskmanager-net:
    driver: bridge
```

> Notez que `backend` n'expose plus de port vers l'hôte (`ports`) car seul `frontend` (Nginx) est exposé publiquement, et il proxy les appels `/api` vers `backend:8080` en interne au réseau Docker. Si vous préférez accéder directement à l'API depuis l'extérieur (utile en dev), ajoutez `ports: ["8080:8080"]` au service `backend`.

- [ ] Fichier créé à la racine du repo

### 7.7 Lancer la stack complète

```bash
docker compose up --build

# En arrière-plan
docker compose up --build -d
docker compose logs -f
```

- [ ] Les 3 conteneurs démarrent (backend, frontend, db)
- [ ] `http://localhost` (port 80) affiche l'interface React
- [ ] Créer une tâche fonctionne (requête proxyée vers le backend, données persistées dans PostgreSQL)

### 7.8 Vérifier la persistance des données

```bash
docker compose down
docker compose up -d
# Vérifier que les tâches créées précédemment sont toujours là
```

- [ ] Données persistées après redémarrage (sans `-v`)

### 7.9 Nettoyage complet

```bash
docker compose down -v
docker system prune -f
```

- [ ] Commande testée

### 7.10 Commit Git

```bash
git add .
git commit -m "feat: conteneurisation Docker (backend Spring Boot + frontend React/Nginx + PostgreSQL)"
```

---

## 📁 Fichiers créés/modifiés

```
backend/Dockerfile
backend/.dockerignore
backend/src/main/resources/application-docker.properties
backend/src/main/java/.../config/CorsConfig.java
frontend/Dockerfile
frontend/nginx.conf
frontend/.dockerignore
frontend/.env
docker-compose.yml
```

---

## ✅ Critères de validation de l'étape

- [ ] `docker build` réussit pour le backend ET le frontend
- [ ] `docker compose up` démarre les 3 services sans erreur
- [ ] L'application complète est accessible via `http://localhost`
- [ ] Le proxy Nginx `/api` fonctionne correctement (pas d'erreur CORS ni 404)
- [ ] Les données persistent après un `down`/`up` (sans `-v`)

---

## ⚠️ Pièges courants

| Problème                                     | Solution                                                                                                        |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| Page blanche sur `http://localhost`          | Vérifier `try_files $uri $uri/ /index.html;` dans `nginx.conf` (nécessaire pour le routing SPA)                 |
| 502 Bad Gateway sur les appels `/api`        | Vérifier que `proxy_pass` pointe bien vers `http://backend:8080` (nom du service Docker, pas `localhost`)       |
| Variables `VITE_*` non prises en compte      | Rappel : elles sont figées au build - reconstruire l'image après modification (`docker compose build frontend`) |
| `Connection refused` entre `backend` et `db` | Vérifier `depends_on` + `healthcheck` sur le service `db`                                                       |
| Image frontend trop lourde                   | Vérifier le multi-stage build : le stage final doit être `nginx:alpine`, sans `node_modules`                    |

---

## ➡️ Prochaine étape

`08-JENKINS-CICD.md` - Mettre en place la pipeline CI/CD complète (backend + frontend)
