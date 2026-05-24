package com.example.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Helpers for reading JSON request bodies and writing JSON HTTP responses.
 */
public final class HttpJson {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private HttpJson() {
    }

    /**
     * Deserialises the request body as JSON into the given type.
     *
     * @param req HTTP request
     * @param type target class
     * @return parsed object
     * @throws IOException if JSON is malformed
     */
    public static <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException {
        try (Reader reader = req.getReader()) {
            return GSON.fromJson(reader, type);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON", e);
        }
    }

    /**
     * Writes a JSON response with UTF-8 encoding and no-cache headers.
     */
    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        if (body != null) {
            resp.getWriter().write(GSON.toJson(body));
        }
    }

    /** Writes {@code {"error":"message"}} with the given HTTP status. */
    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        write(resp, status, new ErrorBody(message));
    }

    /** Standard API error JSON shape. */
    public static class ErrorBody {
        /** Human-readable error message. */
        public final String error;

        public ErrorBody(String error) {
            this.error = error;
        }
    }
}
