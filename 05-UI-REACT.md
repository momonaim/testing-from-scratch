# ⚛️ Étape 5 : Interface utilisateur (React)

## 🎯 Objectif

Construire un frontend React (SPA) découplé du backend, qui consomme l'API REST Spring Boot via `fetch`/`axios`, avec les 4 opérations (lister, créer, modifier, supprimer).

## 📋 Pré-requis

- Étape 3 validée (API REST CRUD fonctionnelle et testée via cURL)
- Node.js 18+ et npm installés (étape 1)
- CORS activé côté backend (voir section 5.2 ci-dessous - **obligatoire**, sinon le navigateur bloquera les appels)

---

## 🧩 Sous-étapes détaillées

### 5.1 Choix d'architecture

On garde **deux projets séparés** dans le même dépôt :

```
task-manager/
├── backend/                  # Projet Spring Boot (renommé depuis la racine)
│   ├── pom.xml
│   └── src/...
├── frontend/                 # Nouveau projet React
│   ├── package.json
│   └── src/...
├── docker-compose.yml
└── Jenkinsfile
```

> 💡 Si votre projet Spring Boot est actuellement à la racine, déplacez son contenu dans un dossier `backend/` : `mkdir backend && git mv <fichiers> backend/` (adaptez selon votre état actuel). Cela simplifie grandement le Docker Compose et le pipeline CI/CD à venir.

- [ ] Réorganisation `backend/` effectuée (si nécessaire)
- [ ] `git commit -m "chore: reorganisation backend/ pour accueillir le frontend React"`

### 5.2 Activer CORS côté Spring Boot

Fichier : `backend/src/main/java/com/example/taskmanager/config/CorsConfig.java`

```java
package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // URL du serveur de dev Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

- [ ] Fichier créé
- [ ] Redémarrer le backend après cette modification

### 5.3 Générer le projet React avec Vite

```bash
cd task-manager
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install axios
```

- [ ] Le dossier `frontend/` est créé avec un projet Vite + React fonctionnel

### 5.4 Nettoyer le squelette par défaut

```bash
# Supprimer les fichiers de démo Vite non utilisés
rm src/assets/react.svg
```

Vider `src/App.css` et `src/index.css` (on les remplira à l'étape 5.7).

- [ ] Squelette nettoyé

### 5.5 Créer le service API

Fichier : `frontend/src/api/taskService.js`

```javascript
import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api/tasks";

export const taskService = {
  getAll: () => axios.get(API_BASE_URL).then((res) => res.data),

  getById: (id) => axios.get(`${API_BASE_URL}/${id}`).then((res) => res.data),

  create: (task) => axios.post(API_BASE_URL, task).then((res) => res.data),

  update: (id, task) =>
    axios.put(`${API_BASE_URL}/${id}`, task).then((res) => res.data),

  delete: (id) => axios.delete(`${API_BASE_URL}/${id}`),
};
```

- [ ] Fichier créé (centralise tous les appels HTTP)

### 5.6 Créer les composants

#### `frontend/src/components/TaskForm.jsx`

```jsx
import { useState, useEffect } from "react";

export default function TaskForm({ initialTask, onSubmit, onCancel }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    if (initialTask) {
      setTitle(initialTask.title || "");
      setDescription(initialTask.description || "");
      setCompleted(initialTask.completed || false);
    }
  }, [initialTask]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ title, description, completed });
  };

  return (
    <form onSubmit={handleSubmit} data-testid="task-form">
      <label htmlFor="title">Titre</label>
      <input
        id="title"
        data-testid="task-title-input"
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        required
      />

      <label htmlFor="description">Description</label>
      <textarea
        id="description"
        data-testid="task-description-input"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label>
        <input
          type="checkbox"
          data-testid="task-completed-checkbox"
          checked={completed}
          onChange={(e) => setCompleted(e.target.checked)}
        />
        Terminée
      </label>

      <div>
        <button type="submit" data-testid="task-submit-button">
          Enregistrer
        </button>
        <button type="button" onClick={onCancel}>
          Annuler
        </button>
      </div>
    </form>
  );
}
```

#### `frontend/src/components/TaskItem.jsx`

```jsx
export default function TaskItem({ task, onEdit, onDelete }) {
  return (
    <tr className="task-item" data-testid="task-item">
      <td
        className={task.completed ? "completed" : ""}
        data-testid="task-title"
      >
        {task.title}
      </td>
      <td data-testid="task-description">{task.description}</td>
      <td data-testid="task-status">
        {task.completed ? "✅ Terminée" : "⏳ En cours"}
      </td>
      <td>
        <button data-testid="task-edit-button" onClick={() => onEdit(task)}>
          Modifier
        </button>
        <button
          data-testid="task-delete-button"
          onClick={() => {
            if (window.confirm("Supprimer cette tâche ?")) {
              onDelete(task.id);
            }
          }}
        >
          Supprimer
        </button>
      </td>
    </tr>
  );
}
```

#### `frontend/src/components/TaskList.jsx`

```jsx
import TaskItem from "./TaskItem";

export default function TaskList({ tasks, onEdit, onDelete }) {
  if (tasks.length === 0) {
    return <p>Aucune tâche pour le moment.</p>;
  }

  return (
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
        {tasks.map((task) => (
          <TaskItem
            key={task.id}
            task={task}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </tbody>
    </table>
  );
}
```

- [ ] Les 3 composants sont créés

### 5.7 Créer le composant principal `App.jsx`

Fichier : `frontend/src/App.jsx`

```jsx
import { useState, useEffect } from "react";
import { taskService } from "./api/taskService";
import TaskList from "./components/TaskList";
import TaskForm from "./components/TaskForm";
import "./App.css";

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [editingTask, setEditingTask] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState(null);

  const loadTasks = async () => {
    try {
      const data = await taskService.getAll();
      setTasks(data);
      setError(null);
    } catch (err) {
      setError("Impossible de charger les tâches. Le backend est-il démarré ?");
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleCreate = () => {
    setEditingTask(null);
    setShowForm(true);
  };

  const handleEdit = (task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleSubmit = async (taskData) => {
    if (editingTask) {
      await taskService.update(editingTask.id, taskData);
    } else {
      await taskService.create(taskData);
    }
    setShowForm(false);
    setEditingTask(null);
    loadTasks();
  };

  const handleDelete = async (id) => {
    await taskService.delete(id);
    loadTasks();
  };

  return (
    <div className="container">
      <h1>📋 Mes tâches</h1>

      {error && <p className="error">{error}</p>}

      {!showForm && (
        <button data-testid="new-task-button" onClick={handleCreate}>
          + Nouvelle tâche
        </button>
      )}

      {showForm && (
        <TaskForm
          initialTask={editingTask}
          onSubmit={handleSubmit}
          onCancel={() => {
            setShowForm(false);
            setEditingTask(null);
          }}
        />
      )}

      <TaskList tasks={tasks} onEdit={handleEdit} onDelete={handleDelete} />
    </div>
  );
}
```

- [x] Fichier créé - gère l'état global (liste, formulaire, erreurs)

### 5.8 Ajouter un minimum de style

Fichier : `frontend/src/App.css`

```css
.container {
  max-width: 800px;
  margin: 40px auto;
  font-family: Arial, sans-serif;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
th,
td {
  padding: 10px;
  border-bottom: 1px solid #ddd;
  text-align: left;
}
.completed {
  text-decoration: line-through;
  color: #888;
}
button {
  padding: 6px 12px;
  margin-right: 6px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}
.error {
  color: #dc2626;
}
form {
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}
```

- [x] Fichier créé

### 5.9 Lancer le frontend en mode dev

```bash
# Terminal 1 - backend
cd backend && mvn spring-boot:run

# Terminal 2 - frontend
cd frontend && npm run dev
```

- [x] Frontend accessible sur `http://localhost:5173`
- [x] La liste des tâches se charge (appel réussi vers `http://localhost:8080/api/tasks`)
- [x] Aucune erreur CORS dans la console du navigateur

### 5.10 Tests manuels dans le navigateur

- [x] Cliquer "+ Nouvelle tâche" ouvre le formulaire
- [x] Créer une tâche → apparaît dans la liste
- [x] Cliquer "Modifier" → formulaire pré-rempli → sauvegarder → changement visible
- [x] Cliquer "Supprimer" → confirmation → la tâche disparaît

### 5.11 Variables d'environnement (bonnes pratiques)

Fichier : `frontend/.env`

```
VITE_API_BASE_URL=http://localhost:8080/api/tasks
```

Mettre à jour `taskService.js` :

```javascript
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/tasks";
```

- [x] Variable d'environnement utilisée (facilite le passage en prod/Docker plus tard)

### 5.12 Commit Git

```bash
git add .
git commit -m "05b: feat: interface React (liste, creation, edition, suppression)"
```

---

## 📁 Fichiers créés/modifiés

```
backend/src/main/java/com/example/taskmanager/config/CorsConfig.java
frontend/package.json
frontend/.env
frontend/src/api/taskService.js
frontend/src/components/TaskForm.jsx
frontend/src/components/TaskItem.jsx
frontend/src/components/TaskList.jsx
frontend/src/App.jsx
frontend/src/App.css
```

---

## ✅ Critères de validation de l'étape

- [x] Les 4 opérations CRUD fonctionnent depuis l'interface React
- [x] Aucune erreur CORS
- [x] Les attributs `data-testid` sont en place sur tous les éléments interactifs (nécessaires pour l'étape 6)
- [x] `npm run build` génère un build de production sans erreur (`frontend/dist/`)

---

## ⚠️ Pièges courants

| Problème                                                  | Solution                                                                                                                |
| --------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Erreur CORS dans la console                               | Vérifier `CorsConfig.java` et que l'origine autorisée correspond exactement au port du serveur Vite (`5173` par défaut) |
| `Network Error` sur les appels axios                      | Vérifier que le backend tourne bien sur le port 8080 et que `VITE_API_BASE_URL` est correct                             |
| État de la liste non rafraîchi après création/suppression | Toujours rappeler `loadTasks()` après chaque opération d'écriture                                                       |
| Checkbox "Terminée" non réactive                          | Vérifier que `checked` est bien contrôlé par le state React (`completed`) et non par un attribut HTML statique          |

---

## ➡️ Prochaine étape

`06-TESTS-UI-PLAYWRIGHT-SELENIUM.md` - Automatiser les tests de cette interface React
