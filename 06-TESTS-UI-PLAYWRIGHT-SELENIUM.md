# 🤖 Étape 6 : Tests UI avec Playwright et Selenium

## 🎯 Objectif
Automatiser les scénarios utilisateur clés (création, modification, suppression de tâche) avec **deux** frameworks différents, afin de comparer leur ergonomie et fiabilité.

## 📋 Pré-requis
- Étape 5 validée (interface web fonctionnelle avec sélecteurs stables)

---

## 🧩 Sous-étapes détaillées

## PARTIE A — Playwright (Java)

### A.1 Ajouter la dépendance

Dans `pom.xml` :
```xml
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.45.0</version>
    <scope>test</scope>
</dependency>
```

- [ ] Dépendance ajoutée

### A.2 Installer les navigateurs Playwright

```bash
mvn compile
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

- [ ] Chromium/Firefox/WebKit installés sans erreur

### A.3 Écrire le test de création

Fichier : `src/test/java/com/example/taskmanager/ui/TaskUiPlaywrightTest.java`

```java
package com.example.taskmanager.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskUiPlaywrightTest {

    @LocalServerPort
    private int port;

    static Playwright playwright;
    static Browser browser;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        page = browser.newPage();
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    @Test
    void shouldCreateTaskViaUi() {
        page.navigate("http://localhost:" + port + "/tasks/new");

        page.locator("#title").fill("Tâche Playwright");
        page.locator("#description").fill("Créée via un test Playwright");
        page.locator("button[type='submit']").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        assertThat(page.locator(".task-item").last()).containsText("Tâche Playwright");
    }

    @Test
    void shouldEditTaskViaUi() {
        // Créer une tâche d'abord
        page.navigate("http://localhost:" + port + "/tasks/new");
        page.locator("#title").fill("Tâche à modifier");
        page.locator("button[type='submit']").click();

        // Cliquer sur "Modifier" pour la dernière ligne créée
        page.locator(".task-item").last().locator("text=Modifier").click();
        page.locator("#title").fill("Tâche modifiée");
        page.locator("button[type='submit']").click();

        assertThat(page.locator(".task-item").last()).containsText("Tâche modifiée");
    }

    @Test
    void shouldDeleteTaskViaUi() {
        page.navigate("http://localhost:" + port + "/tasks/new");
        page.locator("#title").fill("Tâche à supprimer");
        page.locator("button[type='submit']").click();

        int countBefore = page.locator(".task-item").count();

        page.onDialog(Dialog::accept); // accepter le confirm()
        page.locator(".task-item").last().locator("text=Supprimer").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        int countAfter = page.locator(".task-item").count();

        Assertions.assertEquals(countBefore - 1, countAfter);
    }
}
```

- [ ] Les 3 tests (créer, modifier, supprimer) sont écrits

### A.4 Exécuter les tests Playwright

```bash
mvn test -Dtest=TaskUiPlaywrightTest
```

- [ ] Tous les tests passent en mode headless

---

## PARTIE B — Selenium WebDriver

### B.1 Ajouter les dépendances

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

- [ ] Dépendances ajoutées

### B.2 Écrire le test de création

Fichier : `src/test/java/com/example/taskmanager/ui/TaskUiSeleniumTest.java`

```java
package com.example.taskmanager.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskUiSeleniumTest {

    @LocalServerPort
    private int port;

    WebDriver driver;

    @BeforeAll
    static void setupDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void closeBrowser() {
        driver.quit();
    }

    @Test
    void shouldCreateTaskViaUi() {
        driver.get("http://localhost:" + port + "/tasks/new");

        driver.findElement(By.id("title")).sendKeys("Tâche Selenium");
        driver.findElement(By.id("description")).sendKeys("Créée via un test Selenium");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        List<WebElement> items = driver.findElements(By.className("task-item"));
        assertTrue(items.stream().anyMatch(el -> el.getText().contains("Tâche Selenium")));
    }

    @Test
    void shouldDeleteTaskViaUi() {
        driver.get("http://localhost:" + port + "/tasks/new");
        driver.findElement(By.id("title")).sendKeys("À supprimer via Selenium");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        int countBefore = driver.findElements(By.className("task-item")).size();

        // Gérer le confirm() JS
        List<WebElement> deleteButtons = driver.findElements(By.cssSelector("form.inline button"));
        deleteButtons.get(deleteButtons.size() - 1).click();
        driver.switchTo().alert().accept();

        int countAfter = driver.findElements(By.className("task-item")).size();
        assertEquals(countBefore - 1, countAfter);
    }
}
```

- [ ] Les 2 tests (créer, supprimer) sont écrits

### B.3 Exécuter les tests Selenium

```bash
mvn test -Dtest=TaskUiSeleniumTest
```

- [ ] Tous les tests passent en mode headless

---

### C. Comparatif Playwright vs Selenium (à documenter dans votre rendu)

| Critère | Playwright | Selenium |
|---|---|---|
| Vitesse d'exécution | Plus rapide (protocole natif CDP) | Plus lent (protocole WebDriver W3C) |
| Attente automatique (auto-wait) | Oui, natif | Non, nécessite des `WebDriverWait` explicites |
| Gestion des dialogues JS | `page.onDialog()` simple | `driver.switchTo().alert()` |
| Setup des drivers | Géré automatiquement | Nécessite WebDriverManager |
| Maturité écosystème | Plus récent | Très mature, large communauté |
| Support multi-navigateurs | Chromium, Firefox, WebKit | Chrome, Firefox, Edge, Safari |

- [ ] Section comparative rédigée dans le README ou un fichier dédié

### D. Commit Git

```bash
git add .
git commit -m "test: automatisation UI avec Playwright et Selenium"
```

---

## 📁 Fichiers créés/modifiés
```
src/test/java/com/example/taskmanager/ui/TaskUiPlaywrightTest.java
src/test/java/com/example/taskmanager/ui/TaskUiSeleniumTest.java
pom.xml (dépendances playwright, selenium-java, webdrivermanager)
```

---

## ✅ Critères de validation de l'étape

- [ ] Tests Playwright : création, édition, suppression - tous verts
- [ ] Tests Selenium : création, suppression - tous verts
- [ ] Les deux suites tournent en mode headless (compatible CI/CD)
- [ ] Comparatif rédigé

---

## ⚠️ Pièges courants

| Problème | Solution |
|---|---|
| `NoSuchElementException` immédiat avec Selenium | Ajouter un `WebDriverWait` explicite au lieu de dépendre uniquement de l'implicit wait |
| Popup `confirm()` bloque Selenium | Utiliser `driver.switchTo().alert().accept()` juste après le clic |
| Playwright : navigateurs non installés en CI | Ajouter l'étape d'installation (`playwright install --with-deps`) dans le pipeline Jenkins |
| Tests UI lents/instables | Toujours utiliser des attentes explicites sur les éléments plutôt que des `Thread.sleep()` |
| Port aléatoire (`RANDOM_PORT`) mais IP figée en dur | Toujours construire l'URL avec `"http://localhost:" + port` |

---

## ➡️ Prochaine étape
`07-DOCKER.md` - Conteneuriser l'application
