package com.example.taskmanager.api;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.basePath = "/api/tasks";
    }

    @Test
    void shouldCreateTask() {
        Task task = new Task();
        task.setTitle("Write roadmap");
        task.setDescription("Complete the project setup");
        task.setCompleted(false);

        given()
                .contentType(ContentType.JSON)
                .body(task)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Write roadmap"))
                .body("description", equalTo("Complete the project setup"))
                .body("completed", equalTo(false));
    }

    @Test
    void shouldReturnAllTasks() {
        createTask("First task", "First description", false);
        createTask("Second task", "Second description", true);

        given()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    void shouldReturnTaskById() {
        Long taskId = createTask("Task to fetch", "Fetch description", false);

        given()
                .when()
                .get("/" + taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId.intValue()))
                .body("title", equalTo("Task to fetch"))
                .body("description", equalTo("Fetch description"))
                .body("completed", equalTo(false));
    }

    @Test
    void shouldReturn404ForUnknownTaskId() {
        given()
                .when()
                .get("/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Task not found with id 99999"));
    }

    @Test
    void shouldUpdateTask() {
        Long taskId = createTask("Old title", "Old description", false);

        Task updatedTask = new Task();
        updatedTask.setTitle("New title");
        updatedTask.setDescription("New description");
        updatedTask.setCompleted(true);

        given()
                .contentType(ContentType.JSON)
                .body(updatedTask)
                .when()
                .put("/" + taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId.intValue()))
                .body("title", equalTo("New title"))
                .body("description", equalTo("New description"))
                .body("completed", equalTo(true));
    }

    @Test
    void shouldDeleteTask() {
        Long taskId = createTask("Task to delete", "Delete description", false);

        given()
                .when()
                .delete("/" + taskId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/" + taskId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Task not found with id " + taskId));
    }

    @Test
    void shouldRejectTaskWithoutTitle() {
        Task task = new Task();
        task.setDescription("Missing title");
        task.setCompleted(false);

        given()
                .contentType(ContentType.JSON)
                .body(task)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("title", equalTo("Title is required"));
    }

    private Long createTask(String title, String description, boolean completed) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);

        Number taskId = given()
                .contentType(ContentType.JSON)
                .body(task)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        return taskId.longValue();
    }
}