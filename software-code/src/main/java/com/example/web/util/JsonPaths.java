package com.example.web.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Resolves data and upload directories from {@code /WEB-INF/app-settings.properties}.
 */
public final class JsonPaths {

    private static final String SETTINGS = "/WEB-INF/app-settings.properties";
    private static final String KEY_DATA = "dataDir";
    private static final String KEY_UPLOADS_CV = "uploadsCvDir";
    private static final String DEFAULT_DATA = "WEB-INF/data";
    private static final String DEFAULT_UPLOADS_CV = "WEB-INF/uploads/cv";

    private static volatile Properties cachedProps;

    private JsonPaths() {
    }

    private static Properties loadSettings(ServletContext ctx) {
        Properties p = new Properties();
        try (InputStream in = ctx.getResourceAsStream(SETTINGS)) {
            if (in != null) {
                try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    p.load(r);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + SETTINGS, e);
        }
        return p;
    }

    private static Properties props(ServletContext ctx) {
        Properties c = cachedProps;
        if (c != null) {
            return c;
        }
        synchronized (JsonPaths.class) {
            if (cachedProps == null) {
                cachedProps = loadSettings(ctx);
            }
            return cachedProps;
        }
    }

    private static Path resolveDir(ServletContext ctx, String key, String defaultRelative) {
        String configured = props(ctx).getProperty(key);
        if (configured == null || configured.isBlank()) {
            configured = defaultRelative;
        } else {
            configured = configured.trim();
        }
        Path raw = Paths.get(configured);
        if (raw.isAbsolute()) {
            return raw.normalize();
        }
        String base = ctx.getRealPath("/");
        if (base == null) {
            throw new IllegalStateException(
                    "Cannot resolve application root (getRealPath is null). Deploy the WAR expanded in Tomcat, or check container support for getRealPath.");
        }
        return Paths.get(base).resolve(configured).normalize();
    }

    public static Path uploadsCvDirectory(ServletContext ctx) {
        return resolveDir(ctx, KEY_UPLOADS_CV, DEFAULT_UPLOADS_CV);
    }

    public static Path dataDirectory(ServletContext ctx) {
        return resolveDir(ctx, KEY_DATA, DEFAULT_DATA);
    }

    public static Path jsonFile(ServletContext ctx, String fileName) {
        return dataDirectory(ctx).resolve(fileName);
    }

    /** Call from {@code contextDestroyed} so redeploy reloads {@code app-settings.properties}. */
    public static void clearSettingsCache() {
        synchronized (JsonPaths.class) {
            cachedProps = null;
        }
    }
}
