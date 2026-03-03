package com.hotel.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base HTTP handler providing:
 *  - CORS headers for browser access
 *  - JSON response helpers (send, ok, err)
 *  - Request body reader
 *  - URL path segment extractor
 */
public abstract class BaseHandler implements HttpHandler {

    protected static final String CONTENT_JSON = "application/json; charset=UTF-8";

    // ── CORS headers ──────────────────────────────────────────────────────
    protected void addCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    // ── OPTIONS preflight ─────────────────────────────────────────────────
    protected boolean handleOptions(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            addCors(ex);
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // ── Response helpers ──────────────────────────────────────────────────
    protected void send(HttpExchange ex, int status, String body) throws IOException {
        addCors(ex);
        byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CONTENT_JSON);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    protected void ok(HttpExchange ex, String json)    throws IOException { send(ex, 200, json); }
    protected void created(HttpExchange ex, String json) throws IOException { send(ex, 201, json); }
    protected void badRequest(HttpExchange ex, String msg) throws IOException {
        send(ex, 400, "{\"success\":false,\"message\":\"" + esc(msg) + "\"}");
    }
    protected void notFound(HttpExchange ex, String msg) throws IOException {
        send(ex, 404, "{\"success\":false,\"message\":\"" + esc(msg) + "\"}");
    }
    protected void serverError(HttpExchange ex, String msg) throws IOException {
        send(ex, 500, "{\"success\":false,\"message\":\"" + esc(msg) + "\"}");
    }

    // ── Request body ──────────────────────────────────────────────────────
    protected String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody();
             BufferedReader br = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    // ── URL helpers ───────────────────────────────────────────────────────
    /**
     * Returns path segments after removing the context root.
     * e.g. /api/staff/42  with context /api/staff  →  ["42"]
     */
    protected String[] getPathSegments(HttpExchange ex) {
        String full    = ex.getRequestURI().getPath();
        String context = ex.getHttpContext().getPath();
        String rest    = full.substring(context.length());
        if (rest.startsWith("/")) rest = rest.substring(1);
        if (rest.isBlank()) return new String[0];
        return rest.split("/");
    }

    /**
     * Parses query string into a map. e.g. ?status=active&role=admin
     */
    protected Map<String,String> queryParams(HttpExchange ex) {
        String query = ex.getRequestURI().getQuery();
        Map<String,String> map = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    // ── JSON escape ───────────────────────────────────────────────────────
    protected static String esc(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
