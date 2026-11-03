package com.alchemist.hangover.network;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;

/** Client facoltativo. Nessun accesso a Swing o GameState dai thread di rete. */
public final class ChallengeClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1)).build();
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(16), task -> {
                Thread thread = new Thread(task, "alchemist-registry");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());
    private ChallengeClient() { }
    private static URI endpoint(String path) {
        int port = Integer.getInteger("alchemist.registry.port", 8765);
        return URI.create("http://127.0.0.1:" + port + path);
    }
    public static void report(int chapter, boolean won) {
        if (!Boolean.getBoolean("alchemist.registry")) return;
        if (chapter < 1 || chapter > 4) throw new IllegalArgumentException("Capitolo non valido");
        try {
            WORKER.execute(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder(endpoint("/results"))
                            .timeout(Duration.ofSeconds(2))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString("chapter=" + chapter + "&outcome=" + (won ? "win" : "loss"))).build();
                    int code = HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
                    if (code != 201) System.err.println("[Registro] Risultato non registrato: HTTP " + code);
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                catch (Exception ex) { System.err.println("[Registro] Servizio non disponibile; il gioco continua offline."); }
            });
        } catch (RejectedExecutionException ex) {
            System.err.println("[Registro] Coda piena; risultato non inviato.");
        }
    }
    /** Chiamare fuori dall'EDT. Gli errori vengono mostrati dal chiamante. */
    public static String fetchStats() throws java.io.IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint("/stats"))
                .timeout(Duration.ofSeconds(2)).GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new java.io.IOException("HTTP " + response.statusCode());
        return response.body();
    }
}
