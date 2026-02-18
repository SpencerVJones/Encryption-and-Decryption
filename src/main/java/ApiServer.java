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
        server.createContext("/api/info", this::handleApiInfo);
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

        writeHtmlResponse(exchange, 200, landingPageHtml());
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

    private void handleApiInfo(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "encryption-api");
        payload.put("status", "ok");
        payload.put("endpoints", Map.of(
                "health", "GET /health",
                "info", "GET /api/info",
                "generateKey", "POST /api/key",
                "encrypt", "POST /api/encrypt",
                "decrypt", "POST /api/decrypt"
        ));
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

    private void writeHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] responseBody = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBody.length);
        exchange.getResponseBody().write(responseBody);
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

    private String landingPageHtml() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Encryption API Live Demo</title>
                  <style>
                    @import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700&family=IBM+Plex+Mono:wght@400;600&display=swap');
                    :root {
                      --bg-1: #0f172a;
                      --bg-2: #111827;
                      --bg-3: #172554;
                      --card: rgba(255, 255, 255, 0.08);
                      --card-border: rgba(255, 255, 255, 0.18);
                      --text: #e5e7eb;
                      --muted: #9ca3af;
                      --accent: #14b8a6;
                      --accent-2: #38bdf8;
                      --good: #22c55e;
                      --bad: #ef4444;
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      min-height: 100vh;
                      font-family: "Space Grotesk", "Helvetica Neue", sans-serif;
                      color: var(--text);
                      background:
                        radial-gradient(circle at 15% 20%, #0ea5e9 0%, transparent 32%),
                        radial-gradient(circle at 85% 15%, #14b8a6 0%, transparent 28%),
                        linear-gradient(135deg, var(--bg-1), var(--bg-2) 45%, var(--bg-3));
                      padding: 32px 18px;
                    }
                    .frame {
                      max-width: 1040px;
                      margin: 0 auto;
                      display: grid;
                      gap: 18px;
                      animation: load-in 360ms ease-out;
                    }
                    @keyframes load-in {
                      from { opacity: 0; transform: translateY(16px); }
                      to { opacity: 1; transform: translateY(0); }
                    }
                    .hero, .panel {
                      background: var(--card);
                      border: 1px solid var(--card-border);
                      border-radius: 18px;
                      backdrop-filter: blur(12px);
                    }
                    .hero {
                      padding: 20px 22px;
                      display: grid;
                      gap: 6px;
                    }
                    .hero h1 {
                      margin: 0;
                      font-size: clamp(1.4rem, 3vw, 2.1rem);
                      letter-spacing: 0.3px;
                    }
                    .hero p {
                      margin: 0;
                      color: var(--muted);
                    }
                    .panel {
                      padding: 18px;
                      display: grid;
                      gap: 14px;
                    }
                    .grid {
                      display: grid;
                      grid-template-columns: repeat(2, minmax(0, 1fr));
                      gap: 12px;
                    }
                    .full {
                      grid-column: 1 / -1;
                    }
                    label {
                      display: grid;
                      gap: 6px;
                      color: var(--muted);
                      font-size: 0.92rem;
                    }
                    input, select, textarea, button {
                      border: 1px solid rgba(255, 255, 255, 0.16);
                      border-radius: 12px;
                      background: rgba(15, 23, 42, 0.58);
                      color: var(--text);
                      font: inherit;
                    }
                    input, select {
                      padding: 10px 12px;
                      min-height: 42px;
                    }
                    textarea {
                      width: 100%;
                      min-height: 150px;
                      padding: 12px;
                      resize: vertical;
                      font-family: "IBM Plex Mono", monospace;
                    }
                    .actions {
                      display: flex;
                      flex-wrap: wrap;
                      gap: 10px;
                    }
                    button {
                      cursor: pointer;
                      padding: 10px 14px;
                      font-weight: 600;
                      transition: transform 120ms ease, background 180ms ease, border-color 180ms ease;
                    }
                    button.primary {
                      background: linear-gradient(120deg, var(--accent), var(--accent-2));
                      color: #08141f;
                      border-color: transparent;
                    }
                    button.secondary {
                      background: rgba(15, 23, 42, 0.65);
                    }
                    button:hover {
                      transform: translateY(-1px);
                      border-color: rgba(255, 255, 255, 0.34);
                    }
                    .status {
                      margin: 0;
                      padding: 10px 12px;
                      border-radius: 10px;
                      font-family: "IBM Plex Mono", monospace;
                      font-size: 0.86rem;
                      background: rgba(15, 23, 42, 0.58);
                      border: 1px solid rgba(255, 255, 255, 0.14);
                    }
                    .status.good { border-color: rgba(34, 197, 94, 0.6); color: #bbf7d0; }
                    .status.bad { border-color: rgba(239, 68, 68, 0.6); color: #fecaca; }
                    .status.info { border-color: rgba(56, 189, 248, 0.6); color: #bae6fd; }
                    .meta {
                      color: var(--muted);
                      font-size: 0.86rem;
                    }
                    @media (max-width: 900px) {
                      .grid { grid-template-columns: 1fr; }
                    }
                  </style>
                </head>
                <body>
                  <main class="frame">
                    <section class="hero">
                      <h1>Encryption and Decryption Live Demo</h1>
                      <p>Use this web UI to generate keys, encrypt messages, and decrypt payloads with AES, DES, RSA, or Caesar.</p>
                      <p class="meta">Health: <code>/health</code> | API docs: <code>/api/info</code></p>
                    </section>

                    <section class="panel">
                      <div class="grid">
                        <label>
                          Algorithm
                          <select id="algorithm">
                            <option>AES</option>
                            <option>DES</option>
                            <option>RSA</option>
                            <option>Caesar</option>
                          </select>
                        </label>
                        <label id="field-shift">
                          Shift (Caesar only)
                          <input id="shift" type="number" min="1" max="94" placeholder="Optional for encrypt, defaults to 3 for decrypt">
                        </label>
                        <label id="field-key" class="full">
                          Symmetric Key (base64 for AES/DES)
                          <input id="key" type="text" placeholder="Leave empty for auto-generated key during encrypt">
                        </label>
                        <label id="field-public" class="full">
                          Public Key (base64 for RSA encrypt)
                          <textarea id="publicKey" placeholder="Optional. If empty, API will generate an RSA key pair."></textarea>
                        </label>
                        <label id="field-private" class="full">
                          Private Key (base64 for RSA decrypt)
                          <textarea id="privateKey" placeholder="Required to decrypt RSA ciphertext."></textarea>
                        </label>
                        <label class="full">
                          Input Text
                          <textarea id="inputText" placeholder="Type plain text for encrypt or ciphertext for decrypt"></textarea>
                        </label>
                        <label class="full">
                          Output
                          <textarea id="outputText" readonly placeholder="Results appear here"></textarea>
                        </label>
                      </div>

                      <div class="actions">
                        <button class="secondary" id="generateBtn" type="button">Generate Key</button>
                        <button class="primary" id="encryptBtn" type="button">Encrypt</button>
                        <button class="secondary" id="decryptBtn" type="button">Decrypt</button>
                        <button class="secondary" id="useOutputBtn" type="button">Use Output as Input</button>
                        <button class="secondary" id="copyBtn" type="button">Copy Output</button>
                      </div>

                      <p id="status" class="status info">Ready for live demo.</p>
                    </section>
                  </main>

                  <script>
                    const byId = (id) => document.getElementById(id);
                    const algorithm = byId("algorithm");
                    const shift = byId("shift");
                    const key = byId("key");
                    const publicKey = byId("publicKey");
                    const privateKey = byId("privateKey");
                    const inputText = byId("inputText");
                    const outputText = byId("outputText");
                    const statusEl = byId("status");

                    const fieldKey = byId("field-key");
                    const fieldShift = byId("field-shift");
                    const fieldPublic = byId("field-public");
                    const fieldPrivate = byId("field-private");

                    function setStatus(message, tone = "info") {
                      statusEl.className = "status " + tone;
                      statusEl.textContent = message;
                    }

                    function updateFieldVisibility() {
                      const value = algorithm.value;
                      const symmetric = value === "AES" || value === "DES";
                      const rsa = value === "RSA";
                      const caesar = value === "Caesar";

                      fieldKey.style.display = symmetric ? "grid" : "none";
                      fieldPublic.style.display = rsa ? "grid" : "none";
                      fieldPrivate.style.display = rsa ? "grid" : "none";
                      fieldShift.style.display = caesar ? "grid" : "none";
                    }

                    function buildPayload(includeText) {
                      const payload = { algorithm: algorithm.value };
                      if (includeText) {
                        payload.text = inputText.value;
                      }
                      if (key.value.trim()) {
                        payload.key = key.value.trim();
                      }
                      if (publicKey.value.trim()) {
                        payload.publicKey = publicKey.value.trim();
                      }
                      if (privateKey.value.trim()) {
                        payload.privateKey = privateKey.value.trim();
                      }
                      if (shift.value.trim()) {
                        payload.shift = Number(shift.value.trim());
                      }
                      return payload;
                    }

                    async function callApi(path, payload) {
                      const response = await fetch(path, {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                      });

                      const data = await response.json().catch(() => ({
                        success: false,
                        error: "Invalid server response"
                      }));

                      if (!response.ok || data.success === false) {
                        throw new Error(data.error || "Request failed");
                      }
                      return data;
                    }

                    function syncReturnedKeys(data) {
                      if (data.key) {
                        key.value = data.key;
                      }
                      if (data.publicKey) {
                        publicKey.value = data.publicKey;
                      }
                      if (data.privateKey) {
                        privateKey.value = data.privateKey;
                      }
                      if (typeof data.shift === "number") {
                        shift.value = String(data.shift);
                      }
                    }

                    byId("generateBtn").addEventListener("click", async () => {
                      try {
                        setStatus("Generating key...", "info");
                        const data = await callApi("/api/key", { algorithm: algorithm.value });
                        syncReturnedKeys(data);
                        setStatus("Key generated for " + algorithm.value + ".", "good");
                      } catch (error) {
                        setStatus(error.message, "bad");
                      }
                    });

                    byId("encryptBtn").addEventListener("click", async () => {
                      if (!inputText.value.trim()) {
                        setStatus("Input text is required for encryption.", "bad");
                        return;
                      }

                      try {
                        setStatus("Encrypting...", "info");
                        const data = await callApi("/api/encrypt", buildPayload(true));
                        outputText.value = data.result || "";
                        syncReturnedKeys(data);
                        setStatus("Encryption complete.", "good");
                      } catch (error) {
                        setStatus(error.message, "bad");
                      }
                    });

                    byId("decryptBtn").addEventListener("click", async () => {
                      if (!inputText.value.trim()) {
                        setStatus("Input text is required for decryption.", "bad");
                        return;
                      }

                      try {
                        setStatus("Decrypting...", "info");
                        const data = await callApi("/api/decrypt", buildPayload(true));
                        outputText.value = data.result || "";
                        syncReturnedKeys(data);
                        setStatus("Decryption complete.", "good");
                      } catch (error) {
                        setStatus(error.message, "bad");
                      }
                    });

                    byId("useOutputBtn").addEventListener("click", () => {
                      if (!outputText.value.trim()) {
                        setStatus("No output text available.", "bad");
                        return;
                      }
                      inputText.value = outputText.value;
                      setStatus("Output moved to input.", "info");
                    });

                    byId("copyBtn").addEventListener("click", async () => {
                      if (!outputText.value.trim()) {
                        setStatus("No output text available.", "bad");
                        return;
                      }

                      try {
                        await navigator.clipboard.writeText(outputText.value);
                        setStatus("Output copied to clipboard.", "good");
                      } catch (error) {
                        setStatus("Clipboard access blocked by browser.", "bad");
                      }
                    });

                    algorithm.addEventListener("change", () => {
                      updateFieldVisibility();
                      setStatus("Algorithm switched to " + algorithm.value + ".", "info");
                    });

                    updateFieldVisibility();
                  </script>
                </body>
                </html>
                """;
    }

    @FunctionalInterface
    private interface ApiOperation {
        CryptoService.CryptoResponse apply(CryptoService.CryptoRequest request) throws Exception;
    }
}
