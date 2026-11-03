package com.alchemist.hangover.network;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class ChallengeServerTest {
    @Test void recordsValidatedResultsAndRejectsInvalidRequests() throws Exception {
        try (ChallengeServer server = new ChallengeServer(0)) {
            server.start();
            HttpClient client = HttpClient.newHttpClient();
            String base = "http://127.0.0.1:" + server.port();
            assertEquals(201, post(client, base, "chapter=1&outcome=win"));
            assertEquals(201, post(client, base, "chapter=4&outcome=loss"));
            assertEquals(400, post(client, base, "chapter=9&outcome=win"));
            assertEquals(413, post(client, base, "x".repeat(257)));
            var stats = client.send(HttpRequest.newBuilder(URI.create(base + "/stats"))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, stats.statusCode());
            assertTrue(stats.body().contains("Hræsvelgr: 1 vittorie, 0 sconfitte"));
            assertTrue(stats.body().contains("Surtur: 0 vittorie, 1 sconfitte"));
            assertEquals(405, client.send(HttpRequest.newBuilder(URI.create(base + "/results"))
                    .GET().build(), HttpResponse.BodyHandlers.discarding()).statusCode());
        }
    }
    private int post(HttpClient client, String base, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + "/results"))
                .timeout(Duration.ofSeconds(3)).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
