package com.ecse429;

import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodoNonFunctionalTest {

    private static final String BASE_URL = "http://localhost:4567";
    private static HttpClient client;

    @BeforeAll
    static void setupClient() {
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    void resetSystem() throws Exception {
        deleteAllTodos();
    }

    // ---------- Core helpers ----------

    private static void createTodo(String title) throws Exception {
        String json = "{\"title\":\"" + title + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    private static int getTodoCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body().split("\"id\":").length - 1;
    }

    private static void deleteAllTodos() throws Exception {
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(get, HttpResponse.BodyHandlers.ofString());

        String[] parts = response.body().split("\"id\":");

        for (int i = 1; i < parts.length; i++) {
            String id = parts[i].trim().split("[,}\\] ]")[0].replace("\"", "");

            HttpRequest delete = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/todos/" + id))
                    .DELETE()
                    .build();

            client.send(delete, HttpResponse.BodyHandlers.ofString());
        }
    }

    // ================= PERFORMANCE =================
    @Test
    void performance_create100Todos() throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            createTodo("Perf " + i);
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("[PERFORMANCE] 100 todos in " + duration + " ms");

        assertEquals(100, getTodoCount());
        assertTrue(duration < 5000);
    }

    // ================= SCALABILITY =================

    @Test
    void scalability_create10Todos() throws Exception {
        runScalabilityTest(10);
    }

    @Test
    void scalability_create100Todos() throws Exception {
        runScalabilityTest(100);
    }

    @Test
    void scalability_create500Todos() throws Exception {
        runScalabilityTest(500);
    }

    private void runScalabilityTest(int amount) throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < amount; i++) {
            createTodo("Scale " + i);
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println("[SCALABILITY] " + amount + " todos in " + duration + " ms");

        assertEquals(amount, getTodoCount());
    }

    // ================= RELIABILITY =================

    @Test
    void reliability_repeatedCreateDelete() throws Exception {
        for (int cycle = 0; cycle < 20; cycle++) {
            for (int i = 0; i < 20; i++) {
                createTodo("Rel " + cycle + "-" + i);
            }
            deleteAllTodos();
        }

        assertEquals(0, getTodoCount());
        System.out.println("[RELIABILITY] repeated create/delete completed");
    }

    @Test
    void reliability_serviceResponsiveAfterLoad() throws Exception {
        for (int i = 0; i < 300; i++) {
            createTodo("Load " + i);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("todos"));

        System.out.println("[RELIABILITY] service responsive after load");
    }
}
