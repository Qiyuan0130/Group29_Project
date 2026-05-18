package com.example.web.service;

import com.example.web.util.LlmSettings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * OpenAI-compatible chat completions client.
 */
public final class LlmClient {

    private static final Gson GSON = new Gson();
    private static final Duration TIMEOUT = Duration.ofSeconds(90);

    private LlmClient() {
    }

    public static String chat(LlmSettings settings, String systemPrompt, String userPrompt) throws IOException {
        if (!settings.isConfigured()) {
            throw new IllegalStateException(
                    "LLM is not configured. Copy app-settings.local.properties.example to app-settings.local.properties and set llm.apiKey.");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", settings.model);
        body.addProperty("temperature", 0.3);
        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", systemPrompt);
        messages.add(sys);
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userPrompt);
        messages.add(user);
        body.add("messages", messages);

        String url = settings.baseUrl + "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("LLM request interrupted", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM HTTP " + response.statusCode() + ": " + truncate(response.body(), 500));
        }

        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
        if (json == null || !json.has("choices") || json.getAsJsonArray("choices").isEmpty()) {
            throw new IOException("LLM returned empty choices");
        }
        JsonObject message = json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            throw new IOException("LLM returned no message content");
        }
        return message.get("content").getAsString();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
