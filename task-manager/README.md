# Task Manager — UI Test Comparatif

Petit projet de démonstration (Spring Boot backend + Thymeleaf UI) avec suites de tests UI comparatives.

## Playwright vs Selenium — Résumé

Cette section compare les deux approches que nous utilisons pour automatiser l'interface Thymeleaf.

| Critère                      |                                                 Playwright |                                      Selenium |
| ---------------------------- | ---------------------------------------------------------: | --------------------------------------------: |
| Vitesse d'exécution          |                                    Plus rapide (CDP natif) |            Généralement plus lent (WebDriver) |
| Attente automatique          |                                            Oui (auto-wait) |                 Non (nécessite WebDriverWait) |
| Gestion des dialogues JS     |                                   `page.onDialog()` simple |                   `driver.switchTo().alert()` |
| Installation des navigateurs | Playwright installe automatiquement (`playwright install`) | Nécessite WebDriverManager ou drivers manuels |
| Stabilité/ergonomie          |                Excellente pour tests modernes, bonnes APIs |              Très mature, large compatibilité |

## Recommandation

- Pour des tests E2E rapides et modernes, privilégier Playwright (meilleure auto-wait et installateurs).
- Pour compatibilité maximale avec anciens outils CI ou besoins spécifiques de navigateur, Selenium reste un bon choix.

## Exécution (local)

Installer les navigateurs Playwright (si vous utilisez Playwright):

```bash
cd task-manager
mvn compile
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

Exécuter les tests Playwright:

```bash
mvn -Dtest=TaskUiPlaywrightTest test
```

Exécuter les tests Selenium:

```bash
mvn -Dtest=TaskUiSeleniumTest test
```

---

Fichier comparatif généré automatiquement depuis `06-TESTS-UI-PLAYWRIGHT-SELENIUM.md`.
