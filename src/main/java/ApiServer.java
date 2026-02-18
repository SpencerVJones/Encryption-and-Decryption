import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class ApiServer {
    private final CryptoService cryptoService = new CryptoService();

    public static void main(String[] args) throws IOException {
        new ApiServer().start();
    }

    public void start() throws IOException {
        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", this::handleIndex);
        server.createContext("/health", this::handleHealth);
        server.createContext("/api/key", jsonPostHandler(request -> cryptoService.generateKey(request.algorithm)));
        server.createContext("/api/encrypt", jsonPostHandler(cryptoService::encrypt));
        server.createContext("/api/decrypt", jsonPostHandler(cryptoService::decrypt));

        int poolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
        server.setExecutor(Executors.newFixedThreadPool(poolSize));
        server.start();

        System.out.println("Encryption API running on port " + port);
    }

    private int resolvePort() {
        String configuredPort = System.getenv("PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return 8080;
        }
        try {
            return Integer.parseInt(configuredPort);
        } catch (NumberFormatException exception) {
            return 8080;
        }
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "encryption-api");
        payload.put("status", "ok");
        payload.put("docs", Map.of(
                "health", "GET /health",
                "generateKey", "POST /api/key",
                "encrypt", "POST /api/encrypt",
                "decrypt", "POST /api/decrypt"
        ));
        writeJsonResponse(exchange, 200, payload);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "encryption-api");
        payload.put("status", "ok");
        payload.put("timestamp", Instant.now().toString());
        writeJsonResponse(exchange, 200, payload);
    }

    private HttpHandler jsonPostHandler(ApiOperation operation) {
        return exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                writeNoContent(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                CryptoService.CryptoRequest request = JsonUtil.parseCryptoRequest(requestBody);
                CryptoService.CryptoResponse response = operation.apply(request);
                response.success = true;
                writeJsonResponse(exchange, 200, response);
            } catch (IllegalArgumentException | GeneralSecurityException exception) {
                sendError(exchange, 400, exception.getMessage());
            } catch (Exception exception) {
                sendError(exchange, 500, "Unexpected error");
            }
        };
    }

    private void writeNoContent(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private void writeJsonResponse(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        addCorsHeaders(exchange);
        byte[] responseBody = serializeJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBody.length);
        exchange.getResponseBody().write(responseBody);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        CryptoService.CryptoResponse response = new CryptoService.CryptoResponse();
        response.success = false;
        response.error = message;
        writeJsonResponse(exchange, statusCode, response);
    }

    private String serializeJson(Object payload) {
        if (payload instanceof CryptoService.CryptoResponse) {
            return JsonUtil.toJson((CryptoService.CryptoResponse) payload);
        }
        if (payload instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> castPayload = (Map<String, Object>) payload;
            return JsonUtil.toJson(castPayload);
        }
        return "{\"success\":false,\"error\":\"Serialization failure\"}";
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    @FunctionalInterface
    private interface ApiOperation {
        CryptoService.CryptoResponse apply(CryptoService.CryptoRequest request) throws Exception;
    }
}
