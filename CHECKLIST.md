# ✅ Checklist Globale - Task Manager Project

> Mettez à jour ce fichier au fur et à mesure. Cochez `[x]` et changez le statut 🔴/🟡/🟢 après chaque session de travail.

**Dernière mise à jour :** _(à compléter : JJ/MM/AAAA)_

---

## 📊 Vue d'ensemble

| #   | Étape                          | Statut          | % Complété |
| --- | ------------------------------ | --------------- | ---------- |
| 01  | Prérequis & installation       | � Terminé       | 100%       |
| 02  | Setup du projet                | 🟡 En cours     | 70%        |
| 03  | API CRUD (Spring Boot)         | 🔴 Non commencé | 0%         |
| 04  | Tests API (Rest Assured)       | 🔴 Non commencé | 0%         |
| 05  | UI (Thymeleaf)                 | 🔴 Non commencé | 0%         |
| 06  | Tests UI (Playwright/Selenium) | 🔴 Non commencé | 0%         |
| 07  | Dockerisation                  | 🔴 Non commencé | 0%         |
| 08  | Pipeline CI/CD (Jenkins)       | 🔴 Non commencé | 0%         |

**Légende :** 🔴 Non commencé · 🟡 En cours · 🟢 Terminé et validé

---

## 01 - Prérequis & Installation

- [x] Java 17+ installé et vérifié
- [x] Maven 3.8+ installé et vérifié
- [x] Git installé et configuré
- [x] Docker + Docker Compose installés et fonctionnels
- [x] IDE installé (IntelliJ / VS Code) avec plugins Java/Lombok
- [x] Node.js installé (si variante Playwright JS envisagée)
- [x] Jenkins installé et accessible
- [x] Navigateur(s) installés pour tests UI
- [x] Postman ou cURL disponible

---

## 02 - Setup du projet

- [x] Projet généré via Spring Initializr
- [x] `pom.xml` vérifié (dépendances web, JPA, thymeleaf, h2, lombok)
- [x] Structure de dossiers créée (controller/model/repository/service)
- [x] `application.properties` configuré (H2 + console H2)
- [x] Classe principale vérifiée
- [x] Premier lancement réussi (`mvn spring-boot:run`)
- [x] Console H2 accessible
- [x] Dépôt Git initialisé + premier commit

---

## 03 - API CRUD (Spring Boot)

- [x] Entité `Task` créée
- [x] `TaskRepository` créé
- [x] `TaskService` créé (logique métier isolée)
- [x] `TaskController` créé (5 endpoints REST)
- [x] Gestion globale des erreurs (`GlobalExceptionHandler`)
- [x] Tests manuels cURL : POST ✅
- [x] Tests manuels cURL : GET all ✅
- [x] Tests manuels cURL : GET by id ✅
- [x] Tests manuels cURL : PUT ✅
- [x] Tests manuels cURL : DELETE ✅
- [x] Commit Git effectué

---

## 04 - Tests API (Rest Assured)

- [x] Dépendances Rest Assured ajoutées
- [x] Profil de test isolé configuré (`application-test.properties`)
- [x] Test CREATE écrit et vert
- [x] Test READ ALL écrit et vert
- [x] Test READ BY ID écrit et vert
- [x] Test 404 (id inconnu) écrit et vert
- [x] Test UPDATE écrit et vert
- [x] Test DELETE écrit et vert
- [x] Test de validation (titre manquant) décidé/écrit
- [x] `mvn test` en BUILD SUCCESS
- [x] Rapport de couverture JaCoCo généré (optionnel)
- [x] Commit Git effectué

---

## 05 - UI (Thymeleaf)

- [x] `TaskWebController` créé (routes `/tasks`)
- [x] Template `tasks.html` (liste) créé
- [x] Template `task-form.html` (création/édition) créé
- [x] Redirection racine `/` → `/tasks` (optionnel)
- [x] Test manuel navigateur : lister les tâches ✅
- [x] Test manuel navigateur : créer une tâche ✅
- [x] Test manuel navigateur : modifier une tâche ✅
- [x] Test manuel navigateur : supprimer une tâche ✅
- [x] Sélecteurs stables documentés (`.task-item`, `#title`, `#description`)
- [x] Commit Git effectué

---

## 06 - Tests UI (Playwright & Selenium)

### Playwright

- [x] Dépendance ajoutée + navigateurs installés
- [x] Test création écrit et vert
- [x] Test édition écrit et vert
- [x] Test suppression écrit et vert

### Selenium

- [x] Dépendances ajoutées (selenium-java + webdrivermanager)
- [x] Test création écrit et vert
- [x] Test suppression écrit et vert

### Général

- [x] Les deux suites tournent en headless
- [x] Comparatif Playwright vs Selenium rédigé
- [x] Commit Git effectué

---

## 07 - Dockerisation

- [ ] Dépendance PostgreSQL ajoutée
- [ ] Profil `application-docker.properties` créé
- [ ] `Dockerfile` multi-stage créé
- [ ] `.dockerignore` créé
- [ ] `docker build` réussi
- [ ] `docker-compose.yml` créé (app + db + healthcheck + volume)
- [ ] `docker compose up --build` fonctionne
- [ ] Application accessible via `http://localhost:8080`
- [ ] Persistance des données vérifiée (down/up sans `-v`)
- [ ] Commit Git effectué

---

## 08 - Pipeline CI/CD (Jenkins)

- [ ] Projet poussé sur dépôt Git distant (GitHub/GitLab)
- [ ] Plugins Jenkins installés (Git, Pipeline, Maven, Docker Pipeline, JUnit)
- [ ] Outils globaux configurés (JDK 17, Maven)
- [ ] Credentials Docker Hub configurées
- [ ] Credentials Git configurées (si repo privé)
- [ ] `Jenkinsfile` créé et poussé
- [ ] Job Pipeline créé dans Jenkins
- [ ] Déclenchement automatique configuré (webhook ou polling)
- [ ] Premier build manuel réussi (tous les stages verts)
- [ ] Notifications configurées (email/Slack) - optionnel
- [ ] Commit Git final effectué

---

## 🏁 Validation finale du projet

- [ ] Toutes les étapes ci-dessus sont à 🟢
- [ ] Le pipeline Jenkins tourne de bout en bout automatiquement sur un `git push`
- [ ] L'application est déployée et accessible
- [ ] Le README principal (`README.md`) est à jour et reflète l'état réel du projet
- [ ] Une démonstration complète a été faite (screen recording ou live demo)

---

## 📝 Notes / Blocages en cours

_(Notez ici tout point bloquant, question ouverte, ou décision technique à prendre)_

-
-
-
