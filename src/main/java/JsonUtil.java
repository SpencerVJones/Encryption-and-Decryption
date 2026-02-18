import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonUtil {
    private JsonUtil() {
    }

    public static CryptoService.CryptoRequest parseCryptoRequest(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Request body is required");
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON body");
        }

        CryptoService.CryptoRequest request = new CryptoService.CryptoRequest();
        request.algorithm = extractString(trimmed, "algorithm");
        request.text = extractString(trimmed, "text");
        request.key = extractString(trimmed, "key");
        request.publicKey = extractString(trimmed, "publicKey");
        request.privateKey = extractString(trimmed, "privateKey");
        request.shift = extractInteger(trimmed, "shift");
        return request;
    }

    public static String toJson(CryptoService.CryptoResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", response.success);
        payload.put("algorithm", response.algorithm);
        payload.put("result", response.result);
        payload.put("key", response.key);
        payload.put("publicKey", response.publicKey);
        payload.put("privateKey", response.privateKey);
        payload.put("shift", response.shift);
        payload.put("error", response.error);
        return toJson(payload);
    }

    public static String toJson(Map<String, Object> payload) {
        StringBuilder builder = new StringBuilder();
        appendJsonValue(builder, payload);
        return builder.toString();
    }

    private static void appendJsonValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof String) {
            builder.append('"').append(escape((String) value)).append('"');
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }
        if (value instanceof Map<?, ?>) {
            appendMap(builder, (Map<?, ?>) value);
            return;
        }
        builder.append('"').append(escape(value.toString())).append('"');
    }

    private static void appendMap(StringBuilder builder, Map<?, ?> map) {
        builder.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"')
                    .append(escape(String.valueOf(entry.getKey())))
                    .append('"')
                    .append(':');
            appendJsonValue(builder, entry.getValue());
        }
        builder.append('}');
    }

    private static String extractString(String json, String fieldName) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return unescape(matcher.group(1));
    }

    private static Integer extractInteger(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }

    private static String unescape(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch != '\\' || i == value.length() - 1) {
                decoded.append(ch);
                continue;
            }

            char next = value.charAt(++i);
            switch (next) {
                case '"':
                    decoded.append('"');
                    break;
                case '\\':
                    decoded.append('\\');
                    break;
                case '/':
                    decoded.append('/');
                    break;
                case 'b':
                    decoded.append('\b');
                    break;
                case 'f':
                    decoded.append('\f');
                    break;
                case 'n':
                    decoded.append('\n');
                    break;
                case 'r':
                    decoded.append('\r');
                    break;
                case 't':
                    decoded.append('\t');
                    break;
                case 'u':
                    if (i + 4 >= value.length()) {
                        throw new IllegalArgumentException("Invalid unicode escape in JSON");
                    }
                    String hex = value.substring(i + 1, i + 5);
                    decoded.append((char) Integer.parseInt(hex, 16));
                    i += 4;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid escape in JSON");
            }
        }
        return decoded.toString();
    }
}
