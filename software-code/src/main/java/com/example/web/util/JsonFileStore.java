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
 * 读写本地 JSON 文件（无数据库）。线程安全由调用方在业务层控制；此处仅做文件级读写。
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
            throw new IOException("JSON 解析失败: " + fileName, e);
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
            throw new IOException("JSON 解析失败: " + fileName, e);
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
