import org.junit.jupiter.api.*;
import java.net.http.*;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodosApiTest {

    private static final String BASE_URL = "http://localhost:4567";
    private static HttpClient client;

    @BeforeAll
    static void checkServiceRunning() throws Exception {
        client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Service is not running.");
    }

    @BeforeEach
    void setup() throws Exception {
        deleteAllTodos();
        createTodo("Setup Todo", "Before each test");
    }

    @AfterEach
    void cleanup() throws Exception {
        deleteAllTodos();
    }

    // ---------- Helper methods ----------

    private static String getFirstTodoId() throws Exception {
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(get, HttpResponse.BodyHandlers.ofString());

        String body = response.body();
        String[] parts = body.split("\"id\":");

        if (parts.length > 1) {
            String idPart = parts[1].trim();
            // Extract ID - it could be followed by comma, }, ], or whitespace
            int endIndex = idPart.length();
            for (int j = 0; j < idPart.length(); j++) {
                char c = idPart.charAt(j);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    endIndex = j;
                    break;
                }
            }
            String id = idPart.substring(0, endIndex).trim();
            // Remove quotes if present
            if (id.startsWith("\"") && id.endsWith("\"")) {
                id = id.substring(1, id.length() - 1);
            }
            return id;
        }
        return "1"; // fallback
    }

    private static void createTodo(String title, String desc) throws Exception {
        String json = "{\"title\":\"" + title + "\",\"description\":\"" + desc + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void deleteAllTodos() throws Exception {
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(get, HttpResponse.BodyHandlers.ofString());

        String body = response.body();
        String[] parts = body.split("\"id\":");

        for (int i = 1; i < parts.length; i++) {
            String idPart = parts[i].trim();
            // Extract ID - it could be followed by comma, }, ], or whitespace
            int endIndex = idPart.length();
            for (int j = 0; j < idPart.length(); j++) {
                char c = idPart.charAt(j);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    endIndex = j;
                    break;
                }
            }
            String id = idPart.substring(0, endIndex).trim();
            // Remove quotes if present
            if (id.startsWith("\"") && id.endsWith("\"")) {
                id = id.substring(1, id.length() - 1);
            }

            HttpRequest delete = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/todos/" + id))
                    .DELETE()
                    .build();

            client.send(delete, HttpResponse.BodyHandlers.ofString());
        }
    }

    // ---------- Tests ----------

    @Test
    void createTodo_validJson_returns201() throws Exception {
        String json = "{\"title\":\"JUnit Todo\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("JUnit Todo"));
    }

    @Test
    void getTodos_returnsList() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("todos"));
    }

    @Test
    void postUpdate_allowsPartialUpdate() throws Exception {
        String todoId = getFirstTodoId();
        String json = "{\"doneStatus\":true}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + todoId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("true"));
    }

    @Test
    void putUpdate_missingTitle_returns400() throws Exception {
        String todoId = getFirstTodoId();
        String json = "{\"description\":\"Updated\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + todoId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void deleteTwice_returns404() throws Exception {
        deleteAllTodos();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/1"))
                .DELETE()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void getTodos_asXml_returnsXml() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Accept", "application/xml")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("<todos>"));
    }

    @Test
    void malformedJson_returns400() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }
}

