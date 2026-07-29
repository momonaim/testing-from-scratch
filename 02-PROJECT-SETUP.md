# 📁 Étape 2 : Setup du projet Spring Boot

## 🎯 Objectif
Générer le squelette du projet, mettre en place la structure de dossiers, et vérifier que l'application démarre correctement (sans logique métier pour l'instant).

## 📋 Pré-requis
- Étape 1 (`01-PREREQUISITES.md`) entièrement validée

---

## 🧩 Sous-étapes détaillées

### 2.1 Générer le projet via Spring Initializr

Aller sur https://start.spring.io/ et configurer :

| Champ | Valeur |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | Dernière version stable 3.x |
| Group | `com.example` |
| Artifact | `task-manager` |
| Packaging | Jar |
| Java | 17 |

**Dépendances à ajouter :**
- [ ] Spring Web
- [ ] Spring Data JPA
- [ ] H2 Database
- [ ] Thymeleaf
- [ ] Lombok (optionnel mais recommandé)
- [ ] Spring Boot DevTools (optionnel, confort de dev)

Télécharger le zip, l'extraire, puis l'ouvrir dans votre IDE.

**Alternative en ligne de commande (via curl) :**
```bash
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,h2,thymeleaf,lombok,devtools \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.3.0 \
  -d groupId=com.example \
  -d artifactId=task-manager \
  -d name=task-manager \
  -d packageName=com.example.taskmanager \
  -d javaVersion=17 \
  -o task-manager.zip

unzip task-manager.zip -d task-manager
cd task-manager
```

- [ ] Le dossier `task-manager/` existe avec un `pom.xml` valide

### 2.2 Vérifier le `pom.xml`

Ouvrir `pom.xml` et confirmer la présence des dépendances suivantes (les ajouter manuellement si besoin) :

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

- [ ] `mvn clean install` s'exécute sans erreur (à la racine du projet)

### 2.3 Créer la structure de dossiers cible

```bash
mkdir -p src/main/java/com/example/taskmanager/controller
mkdir -p src/main/java/com/example/taskmanager/model
mkdir -p src/main/java/com/example/taskmanager/repository
mkdir -p src/main/java/com/example/taskmanager/service
mkdir -p src/test/java/com/example/taskmanager/api
mkdir -p src/test/java/com/example/taskmanager/ui
```

Structure finale attendue :
```
task-manager/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/taskmanager/
│   │   │   ├── TaskManagerApplication.java
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   └── test/
│       └── java/com/example/taskmanager/
│           ├── api/
│           └── ui/
```

- [ ] Tous les dossiers ci-dessus existent

### 2.4 Configurer `application.properties`

Créer/éditer `src/main/resources/application.properties` :

```properties
# Serveur
server.port=8080

# Base de données H2 (en mémoire pour le développement)
spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Console H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Thymeleaf (désactiver le cache en dev pour voir les changements en direct)
spring.thymeleaf.cache=false
```

- [ ] Le fichier existe et contient bien ces propriétés

### 2.5 Vérifier la classe principale

`src/main/java/com/example/taskmanager/TaskManagerApplication.java` doit ressembler à :

```java
package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
```

### 2.6 Premier lancement (smoke test)

```bash
mvn spring-boot:run
```

- [ ] La console affiche `Tomcat started on port(s): 8080`
- [ ] `http://localhost:8080` répond (404 attendu, c'est normal, il n'y a pas encore de contrôleur)
- [ ] `http://localhost:8080/h2-console` affiche la console H2 (JDBC URL : `jdbc:h2:mem:taskdb`)

### 2.7 Initialiser Git

```bash
git init
cat <<EOF > .gitignore
target/
.idea/
*.iml
.vscode/
.DS_Store
EOF

git add .
git commit -m "chore: initial Spring Boot project setup"
```

- [ ] `git log` affiche le commit initial
- [ ] `target/` n'est pas suivi par Git

---

## ✅ Critères de validation de l'étape

- [ ] `mvn clean install` réussit
- [ ] L'application démarre avec `mvn spring-boot:run` sans erreur
- [ ] La console H2 est accessible
- [ ] Le premier commit Git est fait

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| Port 8080 déjà utilisé | Modifier `server.port` dans `application.properties` |
| Erreur Lombok "cannot find symbol" | Vérifier que le plugin Lombok est installé et activé dans l'IDE (annotation processing) |
| `spring.jpa.hibernate.ddl-auto=update` en production | À utiliser uniquement en dev ; en prod préférer `validate` + migrations Flyway/Liquibase |

---

## ➡️ Prochaine étape
`03-API-CRUD.md` - Développement de l'API REST CRUD complète
