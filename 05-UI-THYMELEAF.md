# 🎨 Étape 5 : Interface utilisateur (Thymeleaf)

## 🎯 Objectif
Construire une interface web permettant de lister, créer, modifier et supprimer des tâches, branchée sur la même couche Service que l'API REST.

## 📋 Pré-requis
- Étape 3 validée (Service et Repository fonctionnels)
- Étape 4 idéalement validée (mais pas bloquant)

---

## 🧩 Sous-étapes détaillées

### 5.1 Créer le Controller MVC

Fichier : `src/main/java/com/example/taskmanager/controller/TaskWebController.java`

```java
package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskWebController {

    private final TaskService service;

    public TaskWebController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public String listTasks(Model model) {
        model.addAttribute("tasks", service.getAll());
        return "tasks";
    }

    @GetMapping("/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new Task());
        return "task-form";
    }

    @PostMapping
    public String createTask(@ModelAttribute Task task) {
        service.create(task);
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", service.getById(id));
        return "task-form";
    }

    @PostMapping("/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        service.update(id, task);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/tasks";
    }
}
```

- [ ] Fichier créé (routes séparées de l'API `/api/tasks`)

### 5.2 Créer le layout de base (fragment réutilisable)

Fichier : `src/main/resources/templates/fragments/layout.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:fragment="head(title)" th:text="${title}">Task Manager</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
        .completed { text-decoration: line-through; color: #888; }
        .btn { padding: 6px 12px; border-radius: 4px; text-decoration: none; color: white; }
        .btn-primary { background: #2563eb; }
        .btn-danger { background: #dc2626; }
        .btn-edit { background: #f59e0b; }
        form.inline { display: inline; }
    </style>
</head>
</html>
```

- [ ] Fichier créé

### 5.3 Créer la page de liste des tâches

Fichier : `src/main/resources/templates/tasks.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Mes tâches</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
        .completed { text-decoration: line-through; color: #888; }
        .btn { padding: 6px 12px; border-radius: 4px; text-decoration: none; color: white; }
        .btn-primary { background: #2563eb; }
        .btn-danger { background: #dc2626; }
        .btn-edit { background: #f59e0b; }
        form.inline { display: inline; }
    </style>
</head>
<body>
    <h1>📋 Mes tâches</h1>
    <a class="btn btn-primary" th:href="@{/tasks/new}">+ Nouvelle tâche</a>

    <table>
        <thead>
            <tr>
                <th>Titre</th>
                <th>Description</th>
                <th>Statut</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="task : ${tasks}" class="task-item" th:classappend="${task.completed} ? 'completed' : ''">
                <td th:text="${task.title}">Titre</td>
                <td th:text="${task.description}">Description</td>
                <td th:text="${task.completed} ? '✅ Terminée' : '⏳ En cours'">Statut</td>
                <td>
                    <a class="btn btn-edit" th:href="@{/tasks/{id}/edit(id=${task.id})}">Modifier</a>
                    <form class="inline" th:action="@{/tasks/{id}/delete(id=${task.id})}" method="post">
                        <button class="btn btn-danger" type="submit"
                                onclick="return confirm('Supprimer cette tâche ?');">Supprimer</button>
                    </form>
                </td>
            </tr>
        </tbody>
    </table>

    <p th:if="${tasks.isEmpty()}">Aucune tâche pour le moment.</p>
</body>
</html>
```

- [ ] Fichier créé
- [ ] Chaque ligne a bien la classe `task-item` (nécessaire pour les futurs tests UI)

### 5.4 Créer le formulaire (création + édition)

Fichier : `src/main/resources/templates/task-form.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Formulaire tâche</title>
</head>
<body>
    <h1 th:text="${task.id} != null ? 'Modifier la tâche' : 'Nouvelle tâche'">Formulaire</h1>

    <form th:action="${task.id} != null ? @{/tasks/{id}(id=${task.id})} : @{/tasks}"
          th:object="${task}" method="post">

        <label for="title">Titre</label><br>
        <input type="text" id="title" th:field="*{title}" required /><br><br>

        <label for="description">Description</label><br>
        <textarea id="description" th:field="*{description}"></textarea><br><br>

        <label>
            <input type="checkbox" th:field="*{completed}" />
            Terminée
        </label><br><br>

        <button type="submit">Enregistrer</button>
        <a th:href="@{/tasks}">Annuler</a>
    </form>
</body>
</html>
```

- [ ] Fichier créé
- [ ] Les champs ont les `id` attendus (`title`, `description`) pour les futurs tests UI

### 5.5 Rediriger la racine vers `/tasks` (optionnel, confort UX)

Ajouter dans `TaskWebController` ou créer un contrôleur dédié :

```java
@GetMapping("/")
public String home() {
    return "redirect:/tasks";
}
```

- [ ] `http://localhost:8080/` redirige vers `/tasks`

### 5.6 Tests manuels dans le navigateur

```bash
mvn spring-boot:run
```

Puis dans le navigateur :
- [ ] `http://localhost:8080/tasks` affiche la liste (vide au départ)
- [ ] Cliquer sur "+ Nouvelle tâche" ouvre le formulaire
- [ ] Créer une tâche → redirection vers la liste → la tâche apparaît
- [ ] Cliquer "Modifier" → le formulaire est pré-rempli → modifier → sauvegarder → changement visible
- [ ] Cliquer "Supprimer" → confirmation → la tâche disparaît de la liste

### 5.7 Commit Git

```bash
git add .
git commit -m "feat: interface web Thymeleaf (liste, creation, edition, suppression)"
```

---

## 📁 Fichiers créés/modifiés
```
src/main/java/com/example/taskmanager/controller/TaskWebController.java
src/main/resources/templates/tasks.html
src/main/resources/templates/task-form.html
src/main/resources/templates/fragments/layout.html
```

---

## ✅ Critères de validation de l'étape

- [ ] Les 4 opérations (liste, création, édition, suppression) fonctionnent depuis le navigateur
- [ ] Les sélecteurs CSS/id utilisés (`.task-item`, `#title`, `#description`, `button[type='submit']`) sont stables et documentés pour les tests UI
- [ ] Aucune erreur dans la console du navigateur ni dans les logs Spring Boot

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `th:field` ne fonctionne pas sur un objet `null` | Toujours initialiser `model.addAttribute("task", new Task())` pour le formulaire de création |
| Le formulaire POST ne trouve pas la route PUT | Thymeleaf/HTML natif ne supporte pas PUT dans un `<form>` - on utilise POST vers `/tasks/{id}` mappé en `@PostMapping` côté serveur (pas de `HiddenHttpMethodFilter` nécessaire ici) |
| Cache Thymeleaf empêche de voir les changements de template | Vérifier `spring.thymeleaf.cache=false` en dev |
| Checkbox `completed` non cochée n'envoie aucune valeur | Comportement HTML normal - Spring gère cela via le binding standard de formulaire (valeur par défaut `false`) |

---

## ➡️ Prochaine étape
`06-TESTS-UI-PLAYWRIGHT-SELENIUM.md` - Automatiser les tests de cette interface
