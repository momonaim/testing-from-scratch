# 🗺️ Roadmap Complète : Task Manager - From Scratch

Ce dossier contient la décomposition complète du projet **Task Manager** en étapes indépendantes, chacune avec son propre fichier Markdown détaillé. Suivez l'ordre ci-dessous, et cochez votre progression dans `CHECKLIST.md`.

---

## 📂 Organisation des fichiers

| # | Fichier | Contenu | Durée estimée |
|---|---------|---------|----------------|
| 01 | `01-PREREQUISITES.md` | Installation des outils (Java, Maven, Git, Docker, IDE...) | 30 - 60 min |
| 02 | `02-PROJECT-SETUP.md` | Génération du squelette Spring Boot, structure des dossiers | 20 - 30 min |
| 03 | `03-API-CRUD.md` | Entité, Repository, Service, Controller REST (CRUD complet) | 2 - 3 h |
| 04 | `04-TESTS-RESTASSURED.md` | Tests automatisés de l'API avec Rest Assured | 1 - 2 h |
| 05 | `05-UI-THYMELEAF.md` | Interface web MVC avec Thymeleaf (liste, ajout, édition, suppression) | 2 - 3 h |
| 06 | `06-TESTS-UI-PLAYWRIGHT-SELENIUM.md` | Tests UI automatisés (Playwright + Selenium) | 2 - 3 h |
| 07 | `07-DOCKER.md` | Conteneurisation de l'app + base de données | 1 - 2 h |
| 08 | `08-JENKINS-CICD.md` | Pipeline CI/CD complète (build, test, package, deploy) | 2 - 4 h |
| ✅ | `CHECKLIST.md` | Suivi de la progression sur toutes les étapes/sous-étapes | - |

**Durée totale estimée : ~12 à 20 heures** (réparties sur plusieurs sessions)

---

## 🎯 Principe de la roadmap

Chaque fichier d'étape suit la même structure pour rester actionnable :

1. **Objectif** de l'étape
2. **Pré-requis** (ce qui doit déjà être fait avant de commencer)
3. **Sous-étapes détaillées** (checklist actionnable, commande par commande)
4. **Fichiers à créer/modifier** (chemins exacts)
5. **Critères de validation** (comment savoir que l'étape est terminée)
6. **Pièges courants** (troubleshooting)
7. **Prochaine étape**

---

## 🔄 Ordre d'exécution recommandé

```
01 Prérequis
   ↓
02 Setup du projet
   ↓
03 API CRUD (Spring Boot)
   ↓
04 Tests API (Rest Assured)  ──┐
   ↓                           │ (peuvent être menés en parallèle
05 UI (Thymeleaf)               │  une fois l'API stable)
   ↓                           │
06 Tests UI (Playwright/Selenium) ┘
   ↓
07 Dockerisation
   ↓
08 Pipeline CI/CD (Jenkins)
```

> 💡 **Astuce** : Ne passez à l'étape suivante que lorsque les critères de validation de l'étape en cours sont tous cochés dans `CHECKLIST.md`. Cela évite d'accumuler de la dette technique invisible.

---

## 📌 Convention utilisée dans tous les fichiers

- `[ ]` = tâche à faire
- `[x]` = tâche terminée
- 🟢 = étape validée / testée
- 🟡 = étape en cours
- 🔴 = bloquée / à investiguer

---

**Prochaine étape → `01-PREREQUISITES.md`**
