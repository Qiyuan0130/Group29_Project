package com.example.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes JSON files on disk (no database). Callers control concurrency; this layer is file I/O only.
 */
public final class JsonFileStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonFileStore() {
    }

    public static <T> T read(ServletContext ctx, String fileName, Class<T> type) throws IOException {
        Path path = JsonPaths.jsonFile(ctx, fileName);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException e) {
            throw new IOException("Invalid JSON in file: " + fileName, e);
        }
    }

    public static <T> T read(ServletContext ctx, String fileName, Type type) throws IOException {
        Path path = JsonPaths.jsonFile(ctx, fileName);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException e) {
            throw new IOException("Invalid JSON in file: " + fileName, e);
        }
    }

    public static void write(ServletContext ctx, String fileName, Object value) throws IOException {
        Path path = JsonPaths.jsonFile(ctx, fileName);
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(value, writer);
        }
    }
}
