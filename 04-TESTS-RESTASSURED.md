# 🔬 Étape 4 : Tests API avec Rest Assured

## 🎯 Objectif
Couvrir automatiquement tous les endpoints CRUD avec des tests Rest Assured + JUnit 5, y compris les cas d'erreur.

## 📋 Pré-requis
- Étape 3 validée (API CRUD fonctionnelle et testée manuellement)

---

## 🧩 Sous-étapes détaillées

### 4.1 Ajouter les dépendances de test

Dans `pom.xml`, ajouter (si pas déjà présent via `spring-boot-starter-test`) :

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-path</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] `mvn dependency:tree` confirme la présence de `rest-assured`

### 4.2 Configurer une base de test isolée (optionnel mais recommandé)

Créer `src/test/resources/application-test.properties` :

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
```

- [ ] Fichier créé (garantit une base propre à chaque run de test)

### 4.3 Créer la classe de test de base

Fichier : `src/test/java/com/example/taskmanager/api/TaskControllerTest.java`

```java
package com.example.taskmanager.api;

import com.example.taskmanager.model.Task;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TaskControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.basePath = "/api/tasks";
    }

    // --- CREATE ---
    @Test
    void shouldCreateTask() {
        Task task = new Task();
        task.setTitle("Écrire les tests Rest Assured");
        task.setDescription("Couverture complète du CRUD");

        given()
            .contentType(ContentType.JSON)
            .body(task)
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("title", equalTo("Écrire les tests Rest Assured"))
            .body("id", notNullValue());
    }

    // --- READ ALL ---
    @Test
    void shouldReturnAllTasks() {
        given()
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("$", isA(java.util.List.class));
    }

    // --- READ BY ID ---
    @Test
    void shouldReturnTaskById() {
        Long id = createTaskAndGetId("Tâche à récupérer", "desc");

        given()
        .when()
            .get("/" + id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id.intValue()))
            .body("title", equalTo("Tâche à récupérer"));
    }

    @Test
    void shouldReturn404ForUnknownTaskId() {
        given()
        .when()
            .get("/99999")
        .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    // --- UPDATE ---
    @Test
    void shouldUpdateTask() {
        Long id = createTaskAndGetId("Ancienne tâche", "ancienne desc");

        Task updated = new Task();
        updated.setTitle("Tâche mise à jour");
        updated.setDescription("Nouvelle description");
        updated.setCompleted(true);

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/" + id)
        .then()
            .statusCode(200)
            .body("title", equalTo("Tâche mise à jour"))
            .body("completed", equalTo(true));
    }

    // --- DELETE ---
    @Test
    void shouldDeleteTask() {
        Long id = createTaskAndGetId("À supprimer", "desc");

        given()
        .when()
            .delete("/" + id)
        .then()
            .statusCode(204);

        given()
        .when()
            .get("/" + id)
        .then()
            .statusCode(404);
    }

    // --- Helper ---
    private Long createTaskAndGetId(String title, String description) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);

        return given()
            .contentType(ContentType.JSON)
            .body(task)
        .when()
            .post()
        .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }
}
```

- [ ] Fichier créé avec les 6 tests ci-dessus minimum

### 4.4 Ajouter des tests de validation (cas limites)

```java
@Test
void shouldRejectTaskWithoutTitle() {
    Task task = new Task();
    task.setDescription("Pas de titre");

    given()
        .contentType(ContentType.JSON)
        .body(task)
    .when()
        .post()
    .then()
        .statusCode(anyOf(is(400), is(500))); // à affiner selon validation ajoutée
}
```

> 💡 Pour un vrai contrôle 400, ajoutez `@NotBlank` sur `title` dans `Task.java` + `@Valid` sur le paramètre du controller + dépendance `spring-boot-starter-validation`.

- [ ] Décider si la validation Bean Validation est ajoutée à ce stade (recommandé)

### 4.5 Exécuter les tests

```bash
mvn test -Dtest=TaskControllerTest

# Ou tous les tests du projet
mvn test
```

- [ ] Tous les tests passent (BUILD SUCCESS)
- [ ] Le rapport est visible dans `target/surefire-reports/`

### 4.6 Générer un rapport de couverture (optionnel - JaCoCo)

Ajouter dans `pom.xml` :
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

```bash
mvn test
# Rapport dans target/site/jacoco/index.html
```

- [ ] Rapport de couverture généré et consultable

### 4.7 Commit Git

```bash
git add .
git commit -m "test: couverture Rest Assured complète du CRUD API"
```

---

## 📁 Fichiers créés/modifiés
```
src/test/java/com/example/taskmanager/api/TaskControllerTest.java
src/test/resources/application-test.properties
pom.xml (dépendances rest-assured, json-path, jacoco)
```

---

## ✅ Critères de validation de l'étape

- [ ] Tests CREATE, READ (all + by id), UPDATE, DELETE tous verts
- [ ] Test de cas d'erreur (404) présent et vert
- [ ] `mvn test` termine en `BUILD SUCCESS`
- [ ] Code commité

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| Tests qui échouent de façon aléatoire (flaky) | S'assurer que chaque test crée ses propres données plutôt que de dépendre de l'ordre d'exécution |
| Port déjà utilisé pendant les tests | `RANDOM_PORT` évite ce problème - ne jamais hardcoder le port 8080 dans les tests |
| Base H2 partagée entre tests → état pollué | Utiliser `create-drop` en profil test, ou `@Transactional` + rollback sur chaque test |
| `id` de type `Long` vs `int` dans les assertions JSON | Rest Assured/JsonPath renvoie souvent un `Integer` - utiliser `.intValue()` pour comparer |

---

## ➡️ Prochaine étape
`05-UI-THYMELEAF.md` - Construire l'interface utilisateur web
