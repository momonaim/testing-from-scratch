package com.example.taskmanager.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * React E2E test: assumes the backend runs on http://localhost:8080
 * and the frontend Vite dev server runs on http://localhost:5173.
 */
public class TaskUiSeleniumReactTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldCreateTaskViaUi() {
        String title = "Tâche Selenium React " + UUID.randomUUID();

        driver.get(FRONTEND_URL);

        driver.findElement(By.cssSelector("[data-testid='new-task-button']")).click();
        driver.findElement(By.cssSelector("[data-testid='task-title-input']")).sendKeys(title);
        driver.findElement(By.cssSelector("[data-testid='task-description-input']"))
                .sendKeys("Créée via Selenium sur React");
        driver.findElement(By.cssSelector("[data-testid='task-submit-button']")).click();

        By titleLocator = taskTitleLocator(title);
        wait.until(ExpectedConditions.numberOfElementsToBe(titleLocator, 1));

        List<WebElement> items = driver.findElements(titleLocator);
        assertTrue(items.stream().anyMatch(el -> title.equals(el.getText())));
    }

    @Test
    void shouldDeleteTaskViaUi() {
        String title = "À supprimer Selenium React " + UUID.randomUUID();

        driver.get(FRONTEND_URL);

        driver.findElement(By.cssSelector("[data-testid='new-task-button']")).click();
        driver.findElement(By.cssSelector("[data-testid='task-title-input']")).sendKeys(title);
        driver.findElement(By.cssSelector("[data-testid='task-submit-button']")).click();

        By titleLocator = taskTitleLocator(title);
        wait.until(ExpectedConditions.numberOfElementsToBe(titleLocator, 1));

        WebElement deleteButton = driver.findElement(titleLocator)
                .findElement(By.xpath("./ancestor::tr[1]//button[@data-testid='task-delete-button']"));

        deleteButton.click();
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.numberOfElementsToBe(titleLocator, 0));
        assertEquals(0, driver.findElements(titleLocator).size());
    }

    private By taskTitleLocator(String title) {
        return By.xpath("//td[@data-testid='task-title' and normalize-space()=" + xpathLiteral(title) + "]");
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        String[] parts = value.split("'");
        StringBuilder builder = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append(", \"'\", ");
            }
            builder.append("'").append(parts[i]).append("'");
        }
        builder.append(")");
        return builder.toString();
    }
}
