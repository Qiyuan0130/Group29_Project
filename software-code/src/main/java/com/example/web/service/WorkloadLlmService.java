package com.example.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.web.repo.ApplicationRepository;
import com.example.web.repo.JobRepository;
import com.example.web.repo.UserRepository;
import com.example.web.util.LlmSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletContext;

/**
 * Service class for LLM-based TA workload analysis.
 * Uses AI to evaluate TA workload distribution and provide adjustment recommendations.
 */
public final class WorkloadLlmService {

    /**
     * System prompt defining the AI's role and output format.
     * The LLM acts as an academic TA workload advisor for university recruitment.
     * 
     * Key evaluation criteria:
     * - Judges each TA primarily by total weekly hours from ACCEPTED positions
     * - Recommended workload limit: 12-15 hours per week per TA
     * 
     * Output Schema:
     * {
     *   "assessments": [
     *     {
     *       "taId": number,           // Unique identifier for the TA
     *       "taName": string,         // Full name of the TA
     *       "weeklyHoursTotal": number, // Sum of hours from accepted positions
     *       "reasonable": boolean,    // Whether workload is within guidelines
     *       "reason": string,         // Brief explanation of the assessment
     *       "adjustment": string      // Suggested changes if workload is excessive
     *     }
     *   ],
     *   "teamAdjustment": string      // Overall team-level recommendation
     * }
     * 
     * Output requirements:
     * - Return ONLY valid JSON (no markdown code fences)
     * - Write reason, adjustment, and teamAdjustment in concise English (1-2 sentences)
     */
    
    private static final String SYSTEM_PROMPT = """
            You are an academic TA workload advisor for a university recruitment system.
            Judge each TA mainly by total weekly hours from ACCEPTED positions (weeklyHoursTotal).
            Guideline: one TA should usually stay within 12-15 hours per week unless clearly justified.
            Return ONLY valid JSON (no markdown fences) with this schema:
            {
              "assessments": [
                {
                  "taId": number,
                  "taName": string,
                  "weeklyHoursTotal": number,
                  "reasonable": boolean,
                  "reason": string,
                  "adjustment": string
                }
              ],
              "teamAdjustment": string
            }
            Write reason, adjustment, and teamAdjustment in concise English (1-2 sentences each for per-TA fields).
            """;

    private WorkloadLlmService() {
    }

    public static Map<String, Object> analyze(ServletContext ctx, UserRepository ur, JobRepository jr,
            ApplicationRepository ar) throws IOException {
        LlmSettings settings = LlmSettings.load(ctx);
        if (!settings.isConfigured()) {
            throw new IllegalStateException(
                    "LLM API key missing. Create WEB-INF/app-settings.local.properties (see .example file).");
        }

        Map<String, Object> report = WorkloadService.buildReport(ur, jr, ar);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) report.get("rows");
        String userPrompt = buildUserPrompt(rows);

        String raw = LlmClient.chat(settings, SYSTEM_PROMPT, userPrompt);
        Map<String, Object> parsed = parseLlmJson(raw);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("model", settings.model);
        out.put("source", "llm");
        out.put("assessments", parsed.get("assessments"));
        out.put("teamAdjustment", parsed.get("teamAdjustment"));
        out.put("inputSummary", report.get("summary"));
        return out;
    }

    private static String buildUserPrompt(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze TA weekly workload. Data:\n");
        if (rows == null || rows.isEmpty()) {
            sb.append("(no TA users)\n");
        } else {
            for (Map<String, Object> r : rows) {
                sb.append("- taId=").append(r.get("taId"))
                        .append(", name=").append(r.get("taName"))
                        .append(", acceptedJobs=").append(r.get("acceptedJobCount"))
                        .append(", pendingApplications=").append(r.get("pendingApplicationCount"))
                        .append(", weeklyHoursTotal=").append(r.get("weeklyHoursTotal"))
                        .append(", positions=").append(r.get("assignedPositions"))
                        .append(", hoursPerJob=").append(r.get("weeklyWorkloadSummary"))
                        .append('\n');
            }
        }
        sb.append("\nFor each TA: is weeklyHoursTotal reasonable? If not, say how to adjust (reassign, reject pending, etc.).");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseLlmJson(String raw) {
        // Remove markdown fences (```json ... ```) if present, default to empty string on null
        String json = stripMarkdownFences(raw == null ? "" : raw.trim());
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            List<Map<String, Object>> assessments = new ArrayList<>();
            if (root.has("assessments") && root.get("assessments").isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray("assessments")) {
                    JsonObject o = el.getAsJsonObject();
                    Map<String, Object> row = new LinkedHashMap<>();
                    if (o.has("taId") && !o.get("taId").isJsonNull()) {
                        row.put("taId", o.get("taId").getAsLong());
                    }
                    row.put("taName", stringOrEmpty(o, "taName"));
                    row.put("weeklyHoursTotal", intOrZero(o, "weeklyHoursTotal"));
                    row.put("reasonable", o.has("reasonable") && o.get("reasonable").getAsBoolean());
                    row.put("reason", stringOrEmpty(o, "reason"));
                    row.put("adjustment", stringOrEmpty(o, "adjustment"));
                    assessments.add(row);
                }
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("assessments", assessments);
            result.put("teamAdjustment", stringOrEmpty(root, "teamAdjustment"));
            return result;
        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("assessments", List.of());
            fallback.put("teamAdjustment", raw);
            return fallback;
        }
    }

    private static String stripMarkdownFences(String s) {
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                return s.substring(firstNl + 1, lastFence).trim();
            }
        }
        return s;
    }

    private static String stringOrEmpty(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static int intOrZero(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return 0;
        }
        try {
            return o.get(key).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }
}
