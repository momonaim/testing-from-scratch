# 🐳 Étape 7 : Conteneurisation Docker

## 🎯 Objectif
Packager l'application dans une image Docker optimisée (multi-stage build) et la faire tourner avec une base de données PostgreSQL persistante via Docker Compose.

## 📋 Pré-requis
- Étape 3 à 6 validées (application fonctionnelle et testée)
- Docker et Docker Compose installés (étape 1)

---

## 🧩 Sous-étapes détaillées

### 7.1 Ajouter le driver PostgreSQL (pour la prod, en plus de H2 pour les tests)

Dans `pom.xml` :
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] Dépendance ajoutée

### 7.2 Créer un profil de configuration "prod"

Fichier : `src/main/resources/application-docker.properties`

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
spring.thymeleaf.cache=true
```

- [ ] Fichier créé (le nom `db` correspondra au service Docker Compose)

### 7.3 Créer le Dockerfile (multi-stage build)

Fichier : `Dockerfile` (à la racine)

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

- [ ] Fichier créé
- [ ] Multi-stage build utilisé (image finale légère, sans Maven)

### 7.4 Créer un `.dockerignore`

Fichier : `.dockerignore`

```
target/
.git/
.idea/
*.iml
.vscode/
*.md
Dockerfile
docker-compose.yml
```

- [ ] Fichier créé (accélère le build, évite d'embarquer des fichiers inutiles)

### 7.5 Build et test de l'image seule

```bash
docker build -t task-manager:local .
docker images | grep task-manager
```

- [ ] L'image se construit sans erreur
- [ ] Taille de l'image raisonnable (< 300 Mo généralement avec Alpine)

### 7.6 Créer le `docker-compose.yml`

Fichier : `docker-compose.yml` (à la racine)

```yaml
version: '3.8'

services:
  app:
    build: .
    container_name: task-manager-app
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    depends_on:
      db:
        condition: service_healthy
    networks:
      - taskmanager-net

  db:
    image: postgres:15-alpine
    container_name: task-manager-db
    environment:
      POSTGRES_DB: taskdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
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

- [ ] Fichier créé
- [ ] `depends_on` avec `condition: service_healthy` (évite les erreurs de connexion au démarrage)
- [ ] Volume nommé pour la persistance des données PostgreSQL

### 7.7 Lancer la stack complète

```bash
docker compose up --build

# En arrière-plan
docker compose up --build -d

# Voir les logs
docker compose logs -f app
```

- [ ] Les deux conteneurs (`app` et `db`) démarrent sans erreur
- [ ] `http://localhost:8080/tasks` est accessible
- [ ] Créer une tâche fonctionne (données persistées dans PostgreSQL)

### 7.8 Vérifier la persistance des données

```bash
# Arrêter les conteneurs (sans supprimer les volumes)
docker compose down

# Relancer
docker compose up -d

# Vérifier que les tâches créées précédemment sont toujours là
```

- [ ] Les données survivent à un redémarrage des conteneurs

### 7.9 Nettoyage complet (pour repartir de zéro si besoin)

```bash
docker compose down -v   # supprime aussi les volumes
docker system prune -f
```

- [ ] Commande testée et comprise (à utiliser avec précaution)

### 7.10 Commit Git

```bash
git add .
git commit -m "feat: conteneurisation Docker (multi-stage build + PostgreSQL via compose)"
```

---

## 📁 Fichiers créés/modifiés
```
Dockerfile
.dockerignore
docker-compose.yml
src/main/resources/application-docker.properties
pom.xml (dépendance postgresql)
```

---

## ✅ Critères de validation de l'étape

- [ ] `docker build` réussit
- [ ] `docker compose up` démarre app + db sans erreur
- [ ] L'application est accessible et fonctionnelle sur `http://localhost:8080`
- [ ] Les données persistent après un `docker compose down` / `up` (sans `-v`)

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `Connection refused` entre `app` et `db` | Vérifier le `depends_on` + `healthcheck`, et que l'URL utilise le nom du service (`db`) et non `localhost` |
| Build très long à chaque fois | Le multi-stage build met en cache les dépendances Maven si `pom.xml` ne change pas - éviter de copier `src` avant les dépendances |
| Port 5432 déjà utilisé sur la machine hôte | Changer le mapping, ex : `"5433:5432"` |
| Image finale trop lourde | Vérifier l'utilisation de `-alpine` et du multi-stage (pas de Maven dans l'image finale) |
| Timezone incohérente entre conteneurs | Ajouter `ENV TZ=Europe/Paris` dans le Dockerfile si nécessaire |

---

## ➡️ Prochaine étape
`08-JENKINS-CICD.md` - Mettre en place la pipeline CI/CD complète
