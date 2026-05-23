package com.example.web.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * LLM config: env vars override {@code app-settings.local.properties} (team default in repo).
 */
public final class LlmSettings {

    private static final String SETTINGS = "/WEB-INF/app-settings.properties";
    private static final String LOCAL_SETTINGS = "/WEB-INF/app-settings.local.properties";

    public final String baseUrl;
    public final String apiKey;
    public final String model;

    private LlmSettings(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public static LlmSettings load(ServletContext ctx) {
        Properties merged = new Properties();
        loadInto(merged, ctx, SETTINGS);
        loadInto(merged, ctx, LOCAL_SETTINGS);

        String baseUrl = firstNonBlank(
                System.getenv("LLM_BASE_URL"),
                merged.getProperty("llm.baseUrl"),
                "https://api.chatanywhere.tech/v1");
        String apiKey = firstNonBlank(
                System.getenv("LLM_API_KEY"),
                merged.getProperty("llm.apiKey"));
        String model = firstNonBlank(
                System.getenv("LLM_MODEL"),
                merged.getProperty("llm.model"),
                "gpt-5.1");

        return new LlmSettings(
                trimTrailingSlash(baseUrl),
                apiKey == null ? "" : apiKey.trim(),
                model == null ? "gpt-5.1" : model.trim());
    }

    private static void loadInto(Properties target, ServletContext ctx, String resource) {
        try (InputStream in = ctx.getResourceAsStream(resource)) {
            if (in == null) {
                return;
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                Properties p = new Properties();
                p.load(r);
                target.putAll(p);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + resource, e);
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
