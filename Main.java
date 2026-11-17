import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        Warehouse warehouse = new Warehouse();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/socks/income", exchange -> handleIncome(exchange, warehouse));
        server.createContext("/api/socks/outcome", exchange -> handleOutcome(exchange, warehouse));
        server.createContext("/api/socks", exchange -> handleGetSocks(exchange, warehouse));

        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен на http://localhost:8080");
    }

    private static void handleIncome(HttpExchange exchange, Warehouse warehouse) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendText(exchange, 405, "Нужен метод POST");
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseSimpleJson(body);

            String color = data.get("color");
            int cottonPart = parseIntField(data.get("cottonPart"), "cottonPart");
            int quantity = parseIntField(data.get("quantity"), "quantity");

            warehouse.addIncome(color, cottonPart, quantity);
            sendText(exchange, 200, "Приход носков добавлен");
        } catch (IllegalArgumentException e) {
            sendText(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, 500, "Ошибка сервера: " + e.getMessage());
        }
    }

    private static void handleOutcome(HttpExchange exchange, Warehouse warehouse) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendText(exchange, 405, "Нужен метод POST");
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseSimpleJson(body);

            String color = data.get("color");
            int cottonPart = parseIntField(data.get("cottonPart"), "cottonPart");
            int quantity = parseIntField(data.get("quantity"), "quantity");

            warehouse.removeOutcome(color, cottonPart, quantity);
            sendText(exchange, 200, "Носки списаны со склада");
        } catch (IllegalArgumentException e) {
            sendText(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, 500, "Ошибка сервера: " + e.getMessage());
        }
    }

    private static void handleGetSocks(HttpExchange exchange, Warehouse warehouse) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendText(exchange, 405, "Нужен метод GET");
            return;
        }
        try {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String color = params.get("color");
            String operation = params.get("operation");

            int cottonPart = parseIntField(params.get("cottonPart"), "cottonPart");

            int count = warehouse.getSocksCount(color, operation, cottonPart);
            sendText(exchange, 200, String.valueOf(count));
        } catch (IllegalArgumentException e) {
            sendText(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, 500, "Ошибка сервера: " + e.getMessage());
        }
    }

    private static Map<String, String> parseSimpleJson(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Пустое тело запроса");
        }
        String text = body.trim();
        if (!text.startsWith("{") || !text.endsWith("}")) {
            throw new IllegalArgumentException("Некорректный формат JSON");
        }

        text = text.substring(1, text.length() - 1).trim();
        Map<String, String> map = new HashMap<>();
        if (text.isEmpty()) {
            return map;
        }

        String[] pairs = text.split(",");
        for (String pair : pairs) {
            int colon = pair.indexOf(':');
            if (colon == -1) {
                throw new IllegalArgumentException("Ошибка в структуре JSON");
            }
            String key = pair.substring(0, colon).trim().replace("\"", "");
            String value = pair.substring(colon + 1).trim().replace("\"", "");
            map.put(key, value);
        }
        return map;
    }

    private static int parseIntField(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Не заполнено поле " + fieldName);
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле " + fieldName + " должно быть целым числом");
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private static void sendText(HttpExchange exchange, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}