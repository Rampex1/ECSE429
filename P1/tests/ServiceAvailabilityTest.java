import org.junit.jupiter.api.Test;
import java.net.http.*;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceAvailabilityTest {

    @Test
    void serviceNotRunning_failsGracefully() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:9999/todos"))
                    .GET()
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            fail("Service should not be reachable.");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}

