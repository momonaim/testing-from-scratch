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

        List<WebElement> deleteButtons = driver.findElements(By.cssSelector("form.inline button"));
        deleteButtons.get(deleteButtons.size() - 1).click();
        driver.switchTo().alert().accept();

        int countAfter = driver.findElements(By.className("task-item")).size();
        assertEquals(countBefore - 1, countAfter);
    }
}
