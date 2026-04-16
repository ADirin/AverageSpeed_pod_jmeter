package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AvgSpd1_POD_Jmeter {

    private static final Logger logger =
            Logger.getLogger(AvgSpd1_POD_Jmeter.class.getName());

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(
                System.getenv().getOrDefault("APP_PORT", "8082")
        );
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // UI + API + health endpoints
        server.createContext("/", new RootHandler());
        server.createContext("/calculate", new CalculateHandler());
        server.createContext("/api/avgSpeed", new ApiCalculateHandler());
        server.createContext("/health", new HealthHandler());

        // Fixed thread pool = better under load tests
        server.setExecutor(Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        ));

        server.start();
        logger.info("Average Speed server started on port " + port);
    }

    // =========================================================
    // Root HTML page (manual browser use)
    // =========================================================
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Average Speed Calculator</title>
                </head>
                <body>
                    <h2>Average Speed Calculator</h2>
                    <form action="/calculate" method="get">
                        Distance (km):
                        <input type="number" step="any" name="distance" required><br>
                        Time (hours):
                        <input type="number" step="any" name="time" required><br>
                        <button type="submit">Calculate</button>
                    </form>
                </body>
                </html>
            """;

            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        }
    }

    // =========================================================
    // HTML calculation endpoint
    // =========================================================
    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;

            try {
                double distance = 0;
                double time = 0;

                String query = exchange.getRequestURI().getQuery();
                if (query == null || query.isEmpty()) {
                    throw new IllegalArgumentException();
                }

                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length != 2) continue;

                    String key = URLDecoder.decode(
                            kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(
                            kv[1], StandardCharsets.UTF_8);

                    if ("distance".equals(key)) {
                        distance = Double.parseDouble(value);
                    } else if ("time".equals(key)) {
                        time = Double.parseDouble(value);
                    }
                }

                double result = avgSpd(distance, time);

                response = String.format("""
                    <html>
                    <body>
                        <h2>Result</h2>
                        <p>Distance: %.2f km</p>
                        <p>Time: %.2f hours</p>
                        <p><strong>Average Speed: %.2f km/h</strong></p>
                        <a href="/">Calculate Again</a>
                    </body>
                    </html>
                """, distance, time, result);

                exchange.sendResponseHeaders(200, response.getBytes().length);

            } catch (Exception e) {
                response = """
                    <html>
                    <body>
                        <h2>Error</h2>
                        <p>Invalid input.</p>
                        <a href="/">Try Again</a>
                    </body>
                    </html>
                """;
                exchange.sendResponseHeaders(400, response.getBytes().length);
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // =========================================================
    // ✅ JSON API — THIS IS WHAT JMETER USES
    // =========================================================
    static class ApiCalculateHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            long start = System.nanoTime();
            String response;
            int status = 200;

            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    throw new IllegalArgumentException();
                }

                String query = exchange.getRequestURI().getQuery();
                if (query == null) {
                    throw new IllegalArgumentException();
                }

                double distance = 0;
                double time = 0;

                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length != 2) continue;

                    String key = URLDecoder.decode(
                            kv[0], StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(
                            kv[1], StandardCharsets.UTF_8);

                    if ("distance".equals(key)) {
                        distance = Double.parseDouble(val);
                    } else if ("time".equals(key)) {
                        time = Double.parseDouble(val);
                    }
                }

                double avg = avgSpd(distance, time);

                response = String.format("""
                    {
                      "distance": %.2f,
                      "time": %.2f,
                      "averageSpeed": %.2f
                    }
                """, distance, time, avg);

            } catch (Exception e) {
                status = 400;
                response = """
                    {
                      "error": "Invalid input"
                    }
                """;
            }

            long durationMs =
                    (System.nanoTime() - start) / 1_000_000;
            logger.info("API request processed in " + durationMs + " ms");

            exchange.getResponseHeaders()
                    .set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // =========================================================
    // Health check (K8s + JMeter)
    // =========================================================
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    // =========================================================
    // Business logic
    // =========================================================
    public static double avgSpd(double distance, double time) {
        if (time <= 0) {
            throw new IllegalArgumentException(
                    "Time must be greater than zero");
        }
        return distance / time;
    }
}
