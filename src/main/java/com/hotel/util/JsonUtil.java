package com.hotel.util;

import java.util.*;

/**
 * Lightweight JSON utility — no external libraries.
 *
 * JsonUtil.obj()      → starts a new JSON object builder
 * JsonUtil.arr()      → starts a new JSON array builder
 * .add(key, value)    → adds a field
 * .build()            → returns the JSON string
 *
 * Also provides: parse(json) → Map<String,String>  (flat one-level)
 */
public class JsonUtil {

    // ── Object Builder ────────────────────────────────────────────────────
    public static ObjBuilder obj() { return new ObjBuilder(); }

    public static class ObjBuilder {
        private final StringBuilder sb = new StringBuilder("{");
        private boolean first = true;

        private ObjBuilder field(String key) {
            if (!first) sb.append(',');
            sb.append('"').append(esc(key)).append('"').append(':');
            first = false;
            return this;
        }

        public ObjBuilder add(String key, String value) {
            field(key);
            if (value == null) sb.append("null");
            else sb.append('"').append(esc(value)).append('"');
            return this;
        }

        public ObjBuilder add(String key, Number value) {
            field(key);
            if (value == null) sb.append("null");
            else sb.append(value);
            return this;
        }

        public ObjBuilder add(String key, boolean value) {
            field(key); sb.append(value); return this;
        }

        /** Embed a raw JSON fragment (array, object, or null literal). */
        public ObjBuilder addRaw(String key, String rawJson) {
            field(key);
            sb.append(rawJson == null ? "null" : rawJson);
            return this;
        }

        public String build() { return sb.append('}').toString(); }
        public String toString() { return build(); }
    }

    // ── Array Builder ─────────────────────────────────────────────────────
    public static ArrBuilder arr() { return new ArrBuilder(); }

    public static class ArrBuilder {
        private final StringBuilder sb = new StringBuilder("[");
        private boolean first = true;

        private void sep() { if (!first) sb.append(','); first = false; }

        public ArrBuilder addRaw(String rawJson) {
            sep(); sb.append(rawJson == null ? "null" : rawJson); return this;
        }

        public ArrBuilder add(String value) {
            sep();
            if (value == null) sb.append("null");
            else sb.append('"').append(esc(value)).append('"');
            return this;
        }

        public String build() { return sb.append(']').toString(); }
        public String toString() { return build(); }
    }

    // ── Flat Parser (depth-1) ─────────────────────────────────────────────
    /**
     * Parses a flat JSON object {"k":"v","k2":123} into Map<String,String>.
     * Handles string and number values; ignores nested objects/arrays.
     */
    public static Map<String, String> parse(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        int i = 0, len = json.length();
        while (i < len) {
            // skip whitespace/comma
            while (i < len && (json.charAt(i) == ',' || Character.isWhitespace(json.charAt(i)))) i++;
            if (i >= len) break;

            // key
            if (json.charAt(i) != '"') { i++; continue; }
            i++; // skip opening quote
            StringBuilder key = new StringBuilder();
            while (i < len && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') i++;
                if (i < len) key.append(json.charAt(i));
                i++;
            }
            i++; // skip closing quote

            // colon
            while (i < len && (json.charAt(i) == ':' || Character.isWhitespace(json.charAt(i)))) i++;
            if (i >= len) break;

            // value
            StringBuilder val = new StringBuilder();
            if (json.charAt(i) == '"') {
                i++; // skip opening quote
                while (i < len && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') { i++; }
                    if (i < len) val.append(json.charAt(i));
                    i++;
                }
                i++; // skip closing quote
            } else if (json.charAt(i) == '{' || json.charAt(i) == '[') {
                // skip nested — not needed in this app
                int depth = 0;
                while (i < len) {
                    char c = json.charAt(i);
                    if (c == '{' || c == '[') depth++;
                    else if (c == '}' || c == ']') { depth--; if (depth == 0) { i++; break; } }
                    i++;
                }
            } else {
                // number / bool / null
                while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}') {
                    val.append(json.charAt(i)); i++;
                }
            }
            map.put(key.toString(), val.toString().trim());
        }
        return map;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    /** Escapes a string for JSON embedding. */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Formats a double/decimal as two-decimal-place string (no trailing zeros). */
    public static String fmtMoney(double v) {
        return String.format("%.2f", v);
    }

    // Quick success/fail wrappers
    public static String success(String message) {
        return obj().add("success", true).add("message", message).build();
    }
    public static String error(String message) {
        return obj().add("success", false).add("message", message).build();
    }
}
