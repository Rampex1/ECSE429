package com.ecse429;

import org.junit.jupiter.api.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoPerformanceTest {

    private static final String BASE_URL = "http://localhost:4567";
    private static final String RESULTS_CSV = "test_results.csv";
    private static HttpClient client;
    private static Random random = new Random();
    private static List<String> createdIds = new ArrayList<>();
    private static PrintWriter csvWriter;

    @BeforeAll
    static void setupClient() throws Exception {
        client = HttpClient.newHttpClient();
        
        // Initialize CSV file
        csvWriter = new PrintWriter(new FileWriter(RESULTS_CSV));
        csvWriter.println("TestName,Operation,ObjectCount,TotalTime_ms,AvgTime_ms,UsedMemory_MB,FreeMemory_MB,TotalMemory_MB");
        
        System.out.println("=".repeat(80));
        System.out.println("TODO API PERFORMANCE TEST SUITE");
        System.out.println("=".repeat(80));
    }

    @AfterAll
    static void cleanup() {
        if (csvWriter != null) {
            csvWriter.close();
        }
        System.out.println("\nResults written to " + RESULTS_CSV);
    }

    @BeforeEach
    void resetSystem() throws Exception {
        deleteAllTodos();
        createdIds.clear();
    }

    @AfterEach
    void printSystemStats() {
        System.out.println("-".repeat(80));
        printMemoryStats();
        System.out.println();
    }

    // ========== RANDOM DATA GENERATORS ==========

    private static String randomTitle() {
        String[] prefixes = {"Buy", "Create", "Review", "Complete", "Update", "Fix", "Plan", "Organize"};
        String[] subjects = {"groceries", "report", "presentation", "meeting notes", "code", "budget", "schedule", "tasks"};
        return prefixes[random.nextInt(prefixes.length)] + " " + subjects[random.nextInt(subjects.length)] + " " + random.nextInt(1000);
    }

    private static String randomDescription() {
        String[] descriptions = {
            "High priority task that needs immediate attention",
            "Scheduled for next week review session",
            "Dependent on other tasks completion",
            "Optional enhancement for future consideration",
            "Critical blocker requiring resolution",
            "Routine maintenance activity",
            "New feature request from stakeholder",
            "Technical debt that should be addressed"
        };
        return descriptions[random.nextInt(descriptions.length)];
    }

    private static boolean randomDoneStatus() {
        return random.nextBoolean();
    }

    // ========== HELPER METHODS ==========

    private static String createTodoWithRandomData() throws Exception {
        String json = String.format(
            "{\"title\":\"%s\",\"description\":\"%s\",\"doneStatus\":%b}",
            randomTitle(),
            randomDescription(),
            randomDoneStatus()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        String id = extractId(response.body());
        createdIds.add(id);
        return id;
    }

    private static void updateTodo(String id) throws Exception {
        String json = String.format(
            "{\"title\":\"%s\",\"description\":\"%s\",\"doneStatus\":%b}",
            randomTitle(),
            randomDescription(),
            randomDoneStatus()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    private static void deleteTodo(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    private static String extractId(String json) {
        int idIndex = json.indexOf("\"id\":\"") + 6;
        int endIndex = json.indexOf("\"", idIndex);
        return json.substring(idIndex, endIndex);
    }

    private static void deleteAllTodos() throws Exception {
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(get, HttpResponse.BodyHandlers.ofString());
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

    private static void printMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        
        System.out.printf("[JVM MEMORY] Used: %d MB | Free: %d MB | Total: %d MB%n", 
            usedMemory, freeMemory, totalMemory);
    }

    private static void logResults(String testName, String operation, int count, long duration) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        double avgTime = (double) duration / count;
        
        csvWriter.printf("%s,%s,%d,%d,%.2f,%d,%d,%d%n",
            testName, operation, count, duration, avgTime, usedMemory, freeMemory, totalMemory);
        csvWriter.flush();
    }

    // ========== CREATE PERFORMANCE TESTS ==========

    @Test
    @Order(1)
    void createPerformance_50Objects() throws Exception {
        measureCreatePerformance(50, "Create_50");
    }

    @Test
    @Order(2)
    void createPerformance_100Objects() throws Exception {
        measureCreatePerformance(100, "Create_100");
    }

    @Test
    @Order(3)
    void createPerformance_200Objects() throws Exception {
        measureCreatePerformance(200, "Create_200");
    }

    @Test
    @Order(4)
    void createPerformance_500Objects() throws Exception {
        measureCreatePerformance(500, "Create_500");
    }

    @Test
    @Order(5)
    void createPerformance_1000Objects() throws Exception {
        measureCreatePerformance(1000, "Create_1000");
    }

    private void measureCreatePerformance(int count, String testName) throws Exception {
        System.out.printf("%n[CREATE TEST] Creating %d todos with random data...%n", count);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            createTodoWithRandomData();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTime = (double) duration / count;
        
        System.out.printf("[RESULT] Total time: %d ms | Average per object: %.2f ms%n", 
            duration, avgTime);
        
        logResults(testName, "CREATE", count, duration);
        
        assertEquals(count, createdIds.size());
    }

    // ========== UPDATE PERFORMANCE TESTS ==========

    @Test
    @Order(6)
    void updatePerformance_50Objects() throws Exception {
        measureUpdatePerformance(50, "Update_50");
    }

    @Test
    @Order(7)
    void updatePerformance_100Objects() throws Exception {
        measureUpdatePerformance(100, "Update_100");
    }

    @Test
    @Order(8)
    void updatePerformance_200Objects() throws Exception {
        measureUpdatePerformance(200, "Update_200");
    }

    @Test
    @Order(9)
    void updatePerformance_500Objects() throws Exception {
        measureUpdatePerformance(500, "Update_500");
    }

    private void measureUpdatePerformance(int count, String testName) throws Exception {
        System.out.printf("%n[UPDATE TEST] Updating %d todos with random data...%n", count);
        
        // First create the objects
        for (int i = 0; i < count; i++) {
            createTodoWithRandomData();
        }
        
        long startTime = System.currentTimeMillis();
        
        // Now update them
        for (String id : createdIds) {
            updateTodo(id);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTime = (double) duration / count;
        
        System.out.printf("[RESULT] Total time: %d ms | Average per object: %.2f ms%n", 
            duration, avgTime);
        
        logResults(testName, "UPDATE", count, duration);
    }

    // ========== DELETE PERFORMANCE TESTS ==========

    @Test
    @Order(10)
    void deletePerformance_50Objects() throws Exception {
        measureDeletePerformance(50, "Delete_50");
    }

    @Test
    @Order(11)
    void deletePerformance_100Objects() throws Exception {
        measureDeletePerformance(100, "Delete_100");
    }

    @Test
    @Order(12)
    void deletePerformance_200Objects() throws Exception {
        measureDeletePerformance(200, "Delete_200");
    }

    @Test
    @Order(13)
    void deletePerformance_500Objects() throws Exception {
        measureDeletePerformance(500, "Delete_500");
    }

    private void measureDeletePerformance(int count, String testName) throws Exception {
        System.out.printf("%n[DELETE TEST] Deleting %d todos...%n", count);
        
        // First create the objects
        for (int i = 0; i < count; i++) {
            createTodoWithRandomData();
        }
        
        long startTime = System.currentTimeMillis();
        
        // Now delete them
        for (String id : createdIds) {
            deleteTodo(id);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTime = (double) duration / count;
        
        System.out.printf("[RESULT] Total time: %d ms | Average per object: %.2f ms%n", 
            duration, avgTime);
        
        logResults(testName, "DELETE", count, duration);
    }

    // ========== COMBINED SCALABILITY TEST ==========

    @Test
    @Order(14)
    void scalability_comprehensiveTest() throws Exception {
        int[] testSizes = {10, 50, 100, 200, 300, 500, 1000};
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE SCALABILITY TEST");
        System.out.println("=".repeat(80));
        
        for (int size : testSizes) {
            System.out.printf("%n--- Testing with %d objects ---%n", size);
            
            // CREATE
            long createStart = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                createTodoWithRandomData();
            }
            long createTime = System.currentTimeMillis() - createStart;
            logResults("Comprehensive_" + size, "CREATE", size, createTime);
            
            // UPDATE
            long updateStart = System.currentTimeMillis();
            for (String id : createdIds) {
                updateTodo(id);
            }
            long updateTime = System.currentTimeMillis() - updateStart;
            logResults("Comprehensive_" + size, "UPDATE", size, updateTime);
            
            // DELETE
            long deleteStart = System.currentTimeMillis();
            for (String id : createdIds) {
                deleteTodo(id);
            }
            long deleteTime = System.currentTimeMillis() - deleteStart;
            logResults("Comprehensive_" + size, "DELETE", size, deleteTime);
            
            System.out.printf("CREATE: %d ms (%.2f ms/obj) | UPDATE: %d ms (%.2f ms/obj) | DELETE: %d ms (%.2f ms/obj)%n",
                createTime, (double)createTime/size,
                updateTime, (double)updateTime/size,
                deleteTime, (double)deleteTime/size);
            
            createdIds.clear();
        }
    }
}
