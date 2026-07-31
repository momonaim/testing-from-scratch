# 🤖 Étape 6 : Tests UI avec Playwright et Selenium (frontend React)

## 🎯 Objectif

Automatiser les scénarios utilisateur clés (création, modification, suppression de tâche) sur l'interface **React**, avec **deux** frameworks différents, afin de comparer leur ergonomie et fiabilité.

## 📋 Pré-requis

- Étape 5 validée (interface React fonctionnelle avec attributs `data-testid`)

## ⚠️ Différence importante par rapport à une UI server-side (Thymeleaf)

Le frontend React est une **application séparée** du backend Spring Boot. Les tests E2E (end-to-end) doivent donc démarrer/avoir accès **aux deux serveurs en même temps** :

- Backend Spring Boot sur `http://localhost:8080`
- Frontend React (Vite) sur `http://localhost:5173`

On ne peut donc plus utiliser `@SpringBootTest(webEnvironment = RANDOM_PORT)` pour piloter le port comme avec Thymeleaf : les tests UI ciblent une **URL fixe**, et on suppose que la stack complète tourne déjà (lancée manuellement, via un script, ou via Docker Compose - voir étape 7). C'est l'approche standard des tests E2E sur une architecture frontend/backend découplée.

- [x] Bien comprendre cette différence avant de continuer

---

## 🧩 Sous-étapes détaillées

### 0. Créer un module de tests E2E dédié

Pour ne pas mélanger tests Java (backend) et tests E2E (full stack), on crée un dossier séparé à la racine :

```bash
mkdir -p e2e-tests
cd e2e-tests
npm init -y
```

- [x] Dossier `e2e-tests/` créé à la racine du repo (à côté de `backend/` et `frontend/`)

---

## PARTIE A — Playwright (JavaScript, recommandé pour du E2E sur une app React)

### A.1 Installer Playwright

```bash
cd e2e-tests
npm init playwright@latest
# Choisir : TypeScript ou JavaScript, dossier "tests", installer les navigateurs
```

- [x] Playwright installé avec navigateurs

### A.2 Configurer l'URL de base

Fichier : `e2e-tests/playwright.config.js`

```javascript
import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  use: {
    baseURL: "http://localhost:5173",
    headless: true,
    screenshot: "only-on-failure",
  },
  webServer: {
    // Optionnel : Playwright peut lui-même démarrer le frontend avant les tests
    command: "cd ../frontend && npm run dev",
    url: "http://localhost:5173",
    reuseExistingServer: true,
    timeout: 30000,
  },
});
```

- [x] Fichier configuré (le backend, lui, doit être lancé séparément - voir A.3)

### A.3 Écrire les tests

Fichier : `e2e-tests/tests/task-management.spec.js`

```javascript
import { test, expect } from "@playwright/test";

test.describe("Gestion des tâches", () => {
  test("création d'une tâche", async ({ page }) => {
    await page.goto("/");

    await page.getByTestId("new-task-button").click();
    await page.getByTestId("task-title-input").fill("Tâche Playwright");
    await page
      .getByTestId("task-description-input")
      .fill("Créée via un test E2E");
    await page.getByTestId("task-submit-button").click();

    const lastTask = page.getByTestId("task-item").last();
    await expect(lastTask.getByTestId("task-title")).toHaveText(
      "Tâche Playwright",
    );
  });

  test("modification d'une tâche", async ({ page }) => {
    await page.goto("/");

    // Créer une tâche à modifier
    await page.getByTestId("new-task-button").click();
    await page.getByTestId("task-title-input").fill("À modifier");
    await page.getByTestId("task-submit-button").click();

    // Modifier
    await page
      .getByTestId("task-item")
      .last()
      .getByTestId("task-edit-button")
      .click();
    await page.getByTestId("task-title-input").fill("Titre modifié");
    await page.getByTestId("task-submit-button").click();

    const lastTask = page.getByTestId("task-item").last();
    await expect(lastTask.getByTestId("task-title")).toHaveText(
      "Titre modifié",
    );
  });

  test("suppression d'une tâche", async ({ page }) => {
    await page.goto("/");

    await page.getByTestId("new-task-button").click();
    await page.getByTestId("task-title-input").fill("À supprimer");
    await page.getByTestId("task-submit-button").click();

    const countBefore = await page.getByTestId("task-item").count();

    page.once("dialog", (dialog) => dialog.accept());
    await page
      .getByTestId("task-item")
      .last()
      .getByTestId("task-delete-button")
      .click();

    await expect(page.getByTestId("task-item")).toHaveCount(countBefore - 1);
  });

  test("marquer une tâche comme terminée", async ({ page }) => {
    await page.goto("/");

    await page.getByTestId("new-task-button").click();
    await page.getByTestId("task-title-input").fill("À terminer");
    await page.getByTestId("task-completed-checkbox").check();
    await page.getByTestId("task-submit-button").click();

    const lastTask = page.getByTestId("task-item").last();
    await expect(lastTask.getByTestId("task-status")).toHaveText("✅ Terminée");
  });
});
```

- [x] Les 4 tests écrits

### A.4 Exécuter les tests Playwright

```bash
# S'assurer que le backend tourne (port 8080)
cd backend && mvn spring-boot:run &

# Lancer les tests (démarre automatiquement le frontend via webServer config)
cd e2e-tests
npx playwright test

# Voir le rapport HTML
npx playwright show-report
```

- [x] Tous les tests passent
- [x] Rapport HTML consultable

---

## PARTIE B — Selenium WebDriver (Java, pour comparaison)

> On garde Selenium côté Java dans le module `backend`, mais en le pointant vers l'URL fixe du frontend React plutôt que vers des pages Thymeleaf.

### B.1 Ajouter les dépendances (dans `backend/pom.xml`)

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.23.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

- [x] Dépendances ajoutées

### B.2 Écrire le test

Fichier : `backend/src/test/java/com/example/taskmanager/ui/TaskUiSeleniumTest.java`

```java
package com.example.taskmanager.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ⚠️ Ce test suppose que le frontend (http://localhost:5173)
// ET le backend (http://localhost:8080) tournent déjà.
// Pas de @SpringBootTest ici : on cible une stack déjà démarrée (E2E).
public class TaskUiSeleniumTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    WebDriver driver;
    WebDriverWait wait;

    @BeforeAll
    static void setupDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void closeBrowser() {
        driver.quit();
    }

    @Test
    void shouldCreateTaskViaUi() {
        driver.get(FRONTEND_URL);

        driver.findElement(By.cssSelector("[data-testid='new-task-button']")).click();
        driver.findElement(By.cssSelector("[data-testid='task-title-input']")).sendKeys("Tâche Selenium");
        driver.findElement(By.cssSelector("[data-testid='task-description-input']")).sendKeys("Créée via Selenium");
        driver.findElement(By.cssSelector("[data-testid='task-submit-button']")).click();

        List<WebElement> items = driver.findElements(By.cssSelector("[data-testid='task-item']"));
        assertTrue(items.stream().anyMatch(el -> el.getText().contains("Tâche Selenium")));
    }

    @Test
    void shouldDeleteTaskViaUi() {
        driver.get(FRONTEND_URL);
        driver.findElement(By.cssSelector("[data-testid='new-task-button']")).click();
        driver.findElement(By.cssSelector("[data-testid='task-title-input']")).sendKeys("À supprimer Selenium");
        driver.findElement(By.cssSelector("[data-testid='task-submit-button']")).click();

        int countBefore = driver.findElements(By.cssSelector("[data-testid='task-item']")).size();

        List<WebElement> deleteButtons = driver.findElements(By.cssSelector("[data-testid='task-delete-button']"));
        deleteButtons.get(deleteButtons.size() - 1).click();
        driver.switchTo().alert().accept();

        int countAfter = driver.findElements(By.cssSelector("[data-testid='task-item']")).size();
        assertEquals(countBefore - 1, countAfter);
    }
}
```

- [x] Les 2 tests écrits

### B.3 Exécuter les tests Selenium

```bash
# Backend + frontend doivent tourner (voir B.2)
cd backend
mvn test -Dtest=TaskUiSeleniumTest
```

- [x] Tests verts

---

## C. Comparatif Playwright vs Selenium (mis à jour pour React)

| Critère                                            | Playwright                                   | Selenium                                              |
| -------------------------------------------------- | -------------------------------------------- | ----------------------------------------------------- |
| Langage des tests                                  | JS/TS natif, cohérent avec le frontend React | Java, cohérent avec le backend                        |
| Sélecteurs recommandés                             | `getByTestId()` natif, très lisible          | `By.cssSelector("[data-testid='...']")`, plus verbeux |
| Démarrage auto du serveur de dev                   | Oui (`webServer` dans la config)             | Non, doit être lancé manuellement                     |
| Attente automatique (auto-wait)                    | Oui, natif                                   | Nécessite `WebDriverWait` explicite                   |
| Gestion des dialogues JS                           | `page.once('dialog', ...)`                   | `driver.switchTo().alert()`                           |
| Intégration naturelle avec un monorepo JS/frontend | Très forte                                   | Plus adaptée à un contexte 100% Java                  |

- [ ] Section comparative rédigée

### D. Commit Git

```bash
git add .
git commit -m "06b: test: automatisation E2E React avec Playwright et Selenium"
```

---

## 📁 Fichiers créés/modifiés

```
e2e-tests/playwright.config.js
e2e-tests/tests/task-management.spec.js
backend/src/test/java/com/example/taskmanager/ui/TaskUiSeleniumTest.java
backend/pom.xml (dépendances selenium-java, webdrivermanager)
```

---

## ✅ Critères de validation de l'étape

- [x] Tests Playwright : création, édition, suppression, statut - tous verts
- [x] Tests Selenium : création, suppression - tous verts
- [x] Les deux suites tournent en headless
- [x] Comparatif rédigé

---

## ⚠️ Pièges courants

| Problème                                        | Solution                                                                                                                                |
| ----------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Tests échouent car le backend n'est pas démarré | Toujours démarrer `mvn spring-boot:run` avant de lancer les tests E2E (ou automatiser via un script `start-stack.sh`)                   |
| Playwright ne trouve pas le frontend            | Vérifier que le port 5173 est libre et que `webServer.url` correspond bien                                                              |
| `data-testid` absent d'un élément               | Revenir à l'étape 5 et vérifier que chaque élément interactif a bien son attribut                                                       |
| Popup `confirm()` bloque Selenium               | `driver.switchTo().alert().accept()` juste après le clic sur "Supprimer"                                                                |
| Tests flaky sur les délais réseau (axios)       | Utiliser les attentes natives de Playwright (`expect(...).toHaveText()` réessaie automatiquement) plutôt que des `waitForTimeout` fixes |

---

## ➡️ Prochaine étape

`07-DOCKER.md` - Conteneuriser le frontend React + le backend Spring Boot
