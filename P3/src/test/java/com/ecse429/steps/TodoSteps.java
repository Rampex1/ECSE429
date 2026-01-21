package com.ecse429.steps;

import com.ecse429.api.ApiClient;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

public class TodoSteps {

    // --- GIVEN STEPS ---

    @Given("the todo service is running")
    public void serviceRunning() {
        Assertions.assertTrue(ApiClient.isServerRunning(), "ERROR: The API service is not running on localhost:4567.");
        TestContext.lastResponse = ApiClient.request().get("/todos");
    }

    @Given("the following todos exist in the system:")
    public void createExistingTodos(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String title = row.get("title");
            Assertions.assertNotNull(title, "todo title missing in DataTable");

            String description = row.get("description");
            String doneStatus = row.get("doneStatus");

            StringBuilder body = new StringBuilder();
            body.append("{\"title\":\"").append(escapeJson(title)).append("\"");
            if (description != null) {
                body.append(",\"description\":\"").append(escapeJson(description)).append("\"");
            }
            if (doneStatus != null) {
                body.append(",\"doneStatus\":").append(Boolean.parseBoolean(doneStatus));
            }
            body.append("}");

            ApiClient.request().body(body.toString()).post("/todos");
        }
    }

    @Given("the following projects exist in the system:")
    public void createExistingProjects(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String title = row.get("title");
            Assertions.assertNotNull(title, "project title missing in DataTable");

            String description = row.getOrDefault("description", "");
            String body = String.format("{\"title\":\"%s\",\"description\":\"%s\"}", escapeJson(title), escapeJson(description));
            ApiClient.request().body(body).post("/projects");
        }
    }

    // --- WHEN STEPS ---

    @When("I create a todo with title {string} and description {string}")
    public void createTodo(String title, String desc) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", escapeJson(title), escapeJson(desc));
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos");
    }

    @When("I create a todo with title {string} using XML format")
    public void createTodoXml(String title) {
        String xmlBody = "<todo><title>" + title + "</title></todo>";
        TestContext.lastResponse = RestAssured.given()
                .config(RestAssuredConfig.config().encoderConfig(
                        EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)
                ))
                .baseUri("http://localhost:4567")
                .header("Content-Type", "application/xml")
                .header("Accept", "application/json")
                .body(xmlBody.getBytes(StandardCharsets.UTF_8))
                .post("/todos");
    }

    @When("I attempt to create a todo with no title and only description {string}")
    public void createTodoNoTitle(String desc) {
        String body = String.format("{\"description\":\"%s\"}", escapeJson(desc));
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos");
    }

    @When("I request all todos")
    public void getAllTodos() {
        TestContext.lastResponse = ApiClient.request().get("/todos");
    }

    @When("I request the todo with title {string}")
    public void getTodoByTitle(String title) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.id");
        Assertions.assertNotNull(id, "No todo found with title: " + title);
        TestContext.lastResponse = ApiClient.request().get("/todos/" + id);
    }

    @When("I request a todo with invalid ID {string}")
    public void requestTodoWithInvalidId(String id) {
        TestContext.lastResponse = ApiClient.request().get("/todos/" + id);
    }

    @When("I update the todo {string} to have doneStatus {string}")
    public void updateTodoDoneStatus(String title, String status) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.id");
        Assertions.assertNotNull(id, "No todo found with title: " + title);

        String body = String.format("{\"doneStatus\": %s}", status);
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos/" + id);
    }

    @When("I change the title of {string} to {string}")
    public void changeTodoTitle(String oldTitle, String newTitle) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + oldTitle + "' }.id");
        Assertions.assertNotNull(id, "No todo found with title: " + oldTitle);

        String body = String.format("{\"title\": \"%s\"}", escapeJson(newTitle));
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos/" + id);
    }

    @When("I try to update todo with ID {string} to title {string}")
    public void tryUpdateNonexistentTodo(String id, String title) {
        String body = String.format("{\"title\": \"%s\"}", escapeJson(title));
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos/" + id);
    }

    @When("I delete the todo with title {string}")
    public void deleteByTitle(String title) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.id");
        Assertions.assertNotNull(id, "No todo found with title: " + title);

        TestContext.lastId = id;
        TestContext.lastResponse = ApiClient.request().delete("/todos/" + id);
    }

    @When("I delete the todo with title {string} again")
    public void deleteSameTodoAgain(String title) {
        TestContext.lastResponse = ApiClient.request().delete("/todos/" + TestContext.lastId);
    }

    @When("I delete the todo with ID {string}")
    public void deleteById(String id) {
        TestContext.lastResponse = ApiClient.request().delete("/todos/" + id);
    }

    @When("I add a new category with title {string} to todo {string}")
    public void addNewCategoryToTodo(String catTitle, String todoTitle) {
        Response list = ApiClient.request().get("/todos");
        String todoId = list.jsonPath().getString("todos.find { it.title == '" + todoTitle + "' }.id");
        String body = String.format("{\"title\":\"%s\"}", escapeJson(catTitle));
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos/" + todoId + "/categories");
    }

    @When("I attempt to add a category {string} to todo {string}")
    public void attemptInvalidCategory(String type, String todoTitle) {
        Response list = ApiClient.request().get("/todos");
        String todoId = list.jsonPath().getString("todos.find { it.title == '" + todoTitle + "' }.id");
        String body = type.contains("title") ? "{}" : "{\"id\":\"1\"}";
        TestContext.lastResponse = ApiClient.request().body(body).post("/todos/" + todoId + "/categories");
    }

    @When("I create a todo with title {string} under project {string}")
    public void createTodoUnderProject(String todoTitle, String projTitle) {
        Response list = ApiClient.request().get("/projects");
        String projId = list.jsonPath().getString("projects.find { it.title == '" + projTitle + "' }.id");
        String body = String.format("{\"title\":\"%s\"}", escapeJson(todoTitle));
        TestContext.lastResponse = ApiClient.request().body(body).post("/projects/" + projId + "/tasks");
    }

    @When("I request tasks for project {string}")
    public void requestTasksForProject(String title) {
        Response list = ApiClient.request().get("/projects");
        String projId = list.jsonPath().getString("projects.find { it.title == '" + title + "' }.id");
        TestContext.lastResponse = ApiClient.request().get("/projects/" + projId + "/tasks");
    }

    @When("I attempt to add an existing todo with id {string} as a task to project {string}")
    public void attemptAddTodoWithIdToProject(String id, String projTitle) {
        Response list = ApiClient.request().get("/projects");
        String projId = list.jsonPath().getString("projects.find { it.title == '" + projTitle + "' }.id");
        String body = String.format("{\"id\":\"%s\"}", id);
        TestContext.lastResponse = ApiClient.request().body(body).post("/projects/" + projId + "/tasks");
    }

    @When("I request categories for todo {string}")
    public void i_request_categories_for_todo(String title) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.id");
        TestContext.lastResponse = ApiClient.request().get("/todos/" + id + "/categories");
    }

    // --- THEN STEPS ---

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int code) {
        Assertions.assertEquals(code, TestContext.lastResponse.getStatusCode());
    }

    @Then("the response should contain title {string}")
    public void verifyTitle(String title) {
        TestContext.lastResponse.then().body(containsString(title));
    }

    @Then("the response should contain error message {string}")
    public void verifyError(String msg) {
        TestContext.lastResponse.then().body("errorMessages[0]", containsString(msg));
    }

    @Then("the todo should exist in the system")
    public void todoShouldExistInSystem() {
        String id = TestContext.lastResponse.jsonPath().getString("id");
        Assertions.assertNotNull(id, "No id found in last response");
        Response check = ApiClient.request().get("/todos/" + id);
        Assertions.assertEquals(200, check.getStatusCode());
    }

    @Then("the response should contain {string} todos")
    public void responseShouldContainTodos(String expectedCount) {
        int count = TestContext.lastResponse.jsonPath().getList("todos").size();
        Assertions.assertEquals(Integer.parseInt(expectedCount), count);
    }

    @Then("the todo {string} should have status {string}")
    public void todoShouldHaveStatus(String title, String status) {
        Response list = ApiClient.request().get("/todos");
        String actualStatus = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.doneStatus");
        Assertions.assertEquals(status, actualStatus);
    }

    @Then("the todo {string} should no longer exist")
    public void todoShouldNoLongerExist(String title) {
        Response res = ApiClient.request().get("/todos/" + TestContext.lastId);
        Assertions.assertEquals(404, res.getStatusCode());
    }

    @Then("the response should contain category {string}")
    public void responseShouldContainCategory(String categoryTitle) {
        TestContext.lastResponse.then().body(containsString(categoryTitle));
    }

    private static String getTodoIdByTitle(String title) {
        Response list = ApiClient.request().get("/todos");
        String id = list.jsonPath().getString("todos.find { it.title == '" + title + "' }.id");
        Assertions.assertNotNull(id, "No todo found with title: " + title);
        return id;
    }

    private static String getProjectIdByTitle(String title) {
        Response list = ApiClient.request().get("/projects");
        String id = list.jsonPath().getString("projects.find { it.title == '" + title + "' }.id");
        Assertions.assertNotNull(id, "No project found with title: " + title);
        return id;
    }

    private static String escapeJson(String value) {
        if (value == null) return null;
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
