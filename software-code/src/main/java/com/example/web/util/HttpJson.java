package com.example.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class HttpJson {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private HttpJson() {
    }

    public static <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException {
        try (Reader reader = req.getReader()) {
            return GSON.fromJson(reader, type);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON", e);
        }
    }

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        if (body != null) {
            resp.getWriter().write(GSON.toJson(body));
        }
    }

    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        write(resp, status, new ErrorBody(message));
    }

    public static class ErrorBody {
        public final String error;

        public ErrorBody(String error) {
            this.error = error;
        }
    }
}
