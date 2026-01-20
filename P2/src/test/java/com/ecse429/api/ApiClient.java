package com.ecse429.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:4567";

    public static RequestSpecification request() {
        return RestAssured.given().baseUri(BASE_URL).contentType("application/json");
    }

    public static void clearAllTodos() {
        // Fetch all todos and delete them one by one to ensure a clean state
        Response response = request().get("/todos");
        response.jsonPath().getList("todos.id").forEach(id -> {
            request().delete("/todos/" + id);
        });
    }
    
    public static boolean isServerRunning() {
        try {
            return request().get("/todos").getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
