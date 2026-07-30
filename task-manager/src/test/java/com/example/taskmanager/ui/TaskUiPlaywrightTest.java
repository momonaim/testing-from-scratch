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
                new BrowserType.LaunchOptions().setHeadless(true));
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
        page.navigate("http://localhost:" + port + "/tasks/new");
        page.locator("#title").fill("Tâche à modifier");
        page.locator("button[type='submit']").click();

        // click the edit link specifically to avoid matching task text
        page.locator(".task-item").last().locator("a.btn-edit").click();
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

        page.onDialog(Dialog::accept);
        // click the delete button specifically to avoid matching task text
        page.locator(".task-item").last().locator("button.btn-danger").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        int countAfter = page.locator(".task-item").count();

        Assertions.assertEquals(countBefore - 1, countAfter);
    }
}
