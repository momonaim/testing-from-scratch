# 🧩 Étape 3 : API Spring Boot (CRUD complet)

## 🎯 Objectif
Développer une API RESTful complète permettant de créer, lire, mettre à jour et supprimer des tâches, avec une architecture propre (Controller / Service / Repository).

## 📋 Pré-requis
- Étape 2 validée (le projet démarre correctement)

---

## 🧩 Sous-étapes détaillées

### 3.1 Créer l'entité `Task`

Fichier : `src/main/java/com/example/taskmanager/model/Task.java`

```java
package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
```

- [ ] Fichier créé, compile sans erreur

### 3.2 Créer le Repository

Fichier : `src/main/java/com/example/taskmanager/repository/TaskRepository.java`

```java
package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCompleted(boolean completed);
}
```

- [ ] Fichier créé

### 3.3 Créer la couche Service

Fichier : `src/main/java/com/example/taskmanager/service/TaskService.java`

```java
package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
    }

    public Task create(Task task) {
        return repository.save(task);
    }

    public Task update(Long id, Task updatedTask) {
        Task existing = getById(id);
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setCompleted(updatedTask.isCompleted());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Task not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
```

- [ ] Fichier créé, logique métier isolée du contrôleur

### 3.4 Créer le Controller REST

Fichier : `src/main/java/com/example/taskmanager/controller/TaskController.java`

```java
package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody Task task) {
        return service.create(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @RequestBody Task task) {
        return service.update(id, task);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

- [ ] Fichier créé

### 3.5 Gestion des erreurs globale (bonnes pratiques)

Fichier : `src/main/java/com/example/taskmanager/controller/GlobalExceptionHandler.java`

```java
package com.example.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Une erreur inattendue est survenue"));
    }
}
```

- [ ] Une erreur 404 propre (JSON) est renvoyée pour une tâche inexistante

### 3.6 Tests manuels avec cURL

```bash
# Lancer l'application
mvn spring-boot:run

# Créer une tâche
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Ma première tâche","description":"Test manuel"}'

# Lister toutes les tâches
curl http://localhost:8080/api/tasks

# Récupérer une tâche par ID
curl http://localhost:8080/api/tasks/1

# Mettre à jour une tâche
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Tâche modifiée","description":"Mise à jour","completed":true}'

# Supprimer une tâche
curl -X DELETE http://localhost:8080/api/tasks/1

# Vérifier la suppression (doit retourner 404)
curl -i http://localhost:8080/api/tasks/1
```

- [ ] POST retourne bien un 201 avec le JSON de la tâche créée
- [ ] GET (liste) retourne bien un tableau JSON
- [ ] GET (par id) retourne la bonne tâche
- [ ] PUT met bien à jour les champs
- [ ] DELETE retourne 204, puis un GET suivant retourne 404

### 3.7 Commit Git

```bash
git add .
git commit -m "feat: API REST CRUD complète pour les tâches"
```

---

## 📁 Fichiers créés/modifiés
```
src/main/java/com/example/taskmanager/model/Task.java
src/main/java/com/example/taskmanager/repository/TaskRepository.java
src/main/java/com/example/taskmanager/service/TaskService.java
src/main/java/com/example/taskmanager/controller/TaskController.java
src/main/java/com/example/taskmanager/controller/GlobalExceptionHandler.java
```

---

## ✅ Critères de validation de l'étape

- [ ] Les 5 endpoints CRUD fonctionnent (GET all, GET by id, POST, PUT, DELETE)
- [ ] Les erreurs 404 sont gérées proprement
- [ ] La console H2 confirme la persistance des données (table `TASKS`)
- [ ] Le code est commité sur Git

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `NullPointerException` sur `id` non trouvé | Vérifier que `GlobalExceptionHandler` est bien scanné (package correct) |
| Lombok `@Data` ne génère pas les getters/setters dans l'IDE | Vérifier l'installation du plugin Lombok + activer "annotation processing" |
| Table `tasks` non créée en base | Vérifier `spring.jpa.hibernate.ddl-auto=update` dans `application.properties` |
| Réponse 200 au lieu de 201 sur POST | Vérifier l'annotation `@ResponseStatus(HttpStatus.CREATED)` |

---

## ➡️ Prochaine étape
`04-TESTS-RESTASSURED.md` - Automatiser les tests de cette API avec Rest Assured
