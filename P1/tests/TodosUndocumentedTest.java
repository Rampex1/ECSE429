import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.Random.class)
public class TodosUndocumentedTest {

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
    void cleanupBeforeEach() throws Exception {
        deleteAll("/todos");
        deleteAll("/categories");
        deleteAll("/projects");
    }

    @AfterEach
    void cleanupAfterEach() throws Exception {
        deleteAll("/todos");
        deleteAll("/categories");
        deleteAll("/projects");
    }

    // ---------- Helpers ----------

    private static String parseFirstIdFromBody(String body) {
        String[] parts = body.split("\"id\":");
        if (parts.length < 2) return null;

        String idPart = parts[1].trim();
        int endIndex = idPart.length();
        for (int j = 0; j < idPart.length(); j++) {
            char c = idPart.charAt(j);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                endIndex = j;
                break;
            }
        }

        String id = idPart.substring(0, endIndex).trim();
        if (id.startsWith("\"") && id.endsWith("\"") && id.length() >= 2) {
            id = id.substring(1, id.length() - 1);
        }
        return id;
    }

    private static void deleteAll(String resourcePath) throws Exception {
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + resourcePath))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(get, HttpResponse.BodyHandlers.ofString());

        // If a resource isn't supported, don't fail unrelated tests.
        if (response.statusCode() == 404) return;

        String body = response.body();
        String[] parts = body.split("\"id\":");
        for (int i = 1; i < parts.length; i++) {
            String idPart = parts[i].trim();

            int endIndex = idPart.length();
            for (int j = 0; j < idPart.length(); j++) {
                char c = idPart.charAt(j);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    endIndex = j;
                    break;
                }
            }

            String id = idPart.substring(0, endIndex).trim();
            if (id.startsWith("\"") && id.endsWith("\"") && id.length() >= 2) {
                id = id.substring(1, id.length() - 1);
            }

            HttpRequest delete = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + resourcePath + "/" + id))
                    .DELETE()
                    .build();

            client.send(delete, HttpResponse.BodyHandlers.ofString());
        }
    }

    private static String createTodo(String title) throws Exception {
        String json = "{\"title\":\"" + title + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String id = parseFirstIdFromBody(response.body());
        assertNotNull(id, "Expected created todo to include an id.");
        return id;
    }

    // ---------- Tests (undocumented / robustness) ----------

    @Test
    void postingJsonWithXmlHeaderStillWorks() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"confused\"}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("confused"));
    }

    @Test
    void xmlBodyWithJsonHeaderFails() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("<todo><title>bad</title></todo>"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void postingWithoutContentTypeStillWorks() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"nocontenttype\"}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("nocontenttype"));
    }

    @Test
    void relationshipGetWorks_evenWhenEmpty() throws Exception {
        String todoId = createTodo("relationship-get");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + todoId + "/categories"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void relationshipPostRejectsId_undocumented() throws Exception {
        String todoId = createTodo("relationship-post-rejects-id");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + todoId + "/categories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"1\"}"))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Not allowed to create with id"));
    }

    @Test
    void patchMethodNotSupported() throws Exception {
        String todoId = createTodo("patch-not-supported");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + todoId))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        assertTrue(
                status == 404 || status == 405,
                "Expected PATCH to be unsupported (404 or 405), got " + status + " with body: " + response.body()
        );
    }

    @Test
    void traceMethodNotSupported() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .method("TRACE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}
