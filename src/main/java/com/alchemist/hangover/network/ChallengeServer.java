package com.alchemist.hangover.network;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

/** Registro REST locale delle sfide. Statistiche di sessione, separate dai salvataggi H2. */
public final class ChallengeServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final AtomicIntegerArray wins = new AtomicIntegerArray(4);
    private final AtomicIntegerArray losses = new AtomicIntegerArray(4);

    public ChallengeServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 16);
        server.setExecutor(workers);
        server.createContext("/results", this::record);
        server.createContext("/stats", this::stats);
    }
    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }

    private void record(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestURI().getPath().equals("/results")) { reply(exchange, 404, "Risorsa assente"); return; }
            if (!exchange.getRequestMethod().equals("POST")) { exchange.getResponseHeaders().set("Allow", "POST"); reply(exchange, 405, "Usare POST"); return; }
            byte[] body = exchange.getRequestBody().readNBytes(257);
            if (body.length > 256) { reply(exchange, 413, "Richiesta troppo grande"); return; }
            String value = new String(body, StandardCharsets.UTF_8);
            var match = java.util.regex.Pattern.compile("chapter=([1-4])&outcome=(win|loss)").matcher(value);
            if (!match.matches()) { reply(exchange, 400, "Capitolo o risultato non valido"); return; }
            int chapter = Integer.parseInt(match.group(1)) - 1;
            (match.group(2).equals("win") ? wins : losses).incrementAndGet(chapter);
            reply(exchange, 201, "Risultato registrato");
        }
    }
    private void stats(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestURI().getPath().equals("/stats")) { reply(exchange, 404, "Risorsa assente"); return; }
            if (!exchange.getRequestMethod().equals("GET")) { exchange.getResponseHeaders().set("Allow", "GET"); reply(exchange, 405, "Usare GET"); return; }
            StringBuilder text = new StringBuilder("REGISTRO DELLE SFIDE\nStatistiche dalla partenza del servizio\n\n");
            String[] names = {"Hræsvelgr", "Rán", "Ymir", "Surtur"};
            for (int i = 0; i < names.length; i++) text.append(names[i]).append(": ")
                    .append(wins.get(i)).append(" vittorie, ").append(losses.get(i)).append(" sconfitte\n");
            reply(exchange, 200, text.toString());
        }
    }
    private static void reply(HttpExchange exchange, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    @Override public void close() { server.stop(0); workers.shutdownNow(); }
    public static void main(String[] args) throws IOException {
        int port = Integer.getInteger("alchemist.registry.port", 8765);
        ChallengeServer service = new ChallengeServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(service::close));
        service.start();
        System.out.println("Registro sfide su http://127.0.0.1:" + service.port() + " — CTRL+C per chiudere");
    }
}
