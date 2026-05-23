package com.example.web.service;

import com.example.web.dto.MatchResultRow;
import com.example.web.model.Job;
import com.example.web.model.User;
import com.example.web.util.LlmSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM-based applicant–job matching for MO AI dashboard.
 * Falls back to {@link AiMatchingService} when LLM is unavailable.
 */
public final class AiMatchingLlmService {

    private static final String SYSTEM_PROMPT = """
            You are an academic TA recruitment advisor. Compare each applicant's profile against a job posting.
            Return ONLY valid JSON (no markdown fences) with this schema:
            {
              "rows": [
                {
                  "applicantId": number,
                  "matchScore": number,
                  "matched": [string],
                  "missing": [string],
                  "analysisNote": string
                }
              ]
            }
            Rules:
            - matchScore is an integer 0-100.
            - matched/missing are short skill or requirement tokens (e.g. "Java", "Python").
            - analysisNote is 1-2 concise English sentences explaining the fit.
            - Include one row per applicant listed in the user message.
            """;

    private AiMatchingLlmService() {
    }

    public static List<MatchResultRow> match(ServletContext ctx, Job job, List<User> applicants) {
        if (job == null || applicants == null || applicants.isEmpty()) {
            return List.of();
        }
        try {
            LlmSettings settings = LlmSettings.load(ctx);
            if (!settings.isConfigured()) {
                return ruleBased(job, applicants);
            }
            String raw = LlmClient.chat(settings, SYSTEM_PROMPT, buildUserPrompt(job, applicants));
            List<MatchResultRow> parsed = parseRows(raw, applicants);
            if (parsed.isEmpty()) {
                return ruleBased(job, applicants);
            }
            return parsed;
        } catch (IOException e) {
            return ruleBased(job, applicants);
        }
    }

    private static List<MatchResultRow> ruleBased(Job job, List<User> applicants) {
        List<MatchResultRow> rows = new ArrayList<>();
        for (User applicant : applicants) {
            rows.addAll(AiMatchingService.matchApplicantsForMo(applicant, job));
        }
        return rows;
    }

    private static String buildUserPrompt(Job job, List<User> applicants) {
        StringBuilder sb = new StringBuilder();
        sb.append("Job:\n");
        sb.append("- title: ").append(safe(job.title)).append('\n');
        sb.append("- course: ").append(safe(job.courseName)).append('\n');
        sb.append("- requirements: ").append(safe(job.requirements)).append('\n');
        if (job.requirementsTags != null && !job.requirementsTags.isEmpty()) {
            sb.append("- requirement tags: ").append(String.join(", ", job.requirementsTags)).append('\n');
        }
        sb.append("- requirements note: ").append(safe(job.requirementsNote)).append('\n');
        sb.append("- weekly hours: ").append(safe(job.workingHours)).append('\n');
        sb.append("\nApplicants:\n");
        for (User u : applicants) {
            sb.append("- applicantId=").append(u.id)
                    .append(", name=").append(safe(u.name))
                    .append(", major=").append(safe(u.major))
                    .append(", technicalAbility=").append(safe(u.technicalAbility))
                    .append(", education=").append(safe(u.educationBackground))
                    .append('\n');
        }
        sb.append("\nScore each applicant against the job requirements.");
        return sb.toString();
    }

    private static List<MatchResultRow> parseRows(String raw, List<User> applicants) {
        Map<Long, User> byId = new LinkedHashMap<>();
        for (User u : applicants) {
            byId.put(u.id, u);
        }

        String json = stripMarkdownFences(raw == null ? "" : raw.trim());
        List<MatchResultRow> rows = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("rows") || !root.get("rows").isJsonArray()) {
                return rows;
            }
            for (JsonElement el : root.getAsJsonArray("rows")) {
                JsonObject o = el.getAsJsonObject();
                if (!o.has("applicantId") || o.get("applicantId").isJsonNull()) {
                    continue;
                }
                long applicantId = o.get("applicantId").getAsLong();
                User u = byId.get(applicantId);
                if (u == null) {
                    continue;
                }
                MatchResultRow row = new MatchResultRow();
                row.idKey = "user-" + applicantId;
                row.title = u.name;
                row.subtitle = u.username;
                row.matchScore = formatScore(o);
                row.matchedSkills = formatSkills(o, "matched", true);
                row.missingSkills = formatSkills(o, "missing", false);
                row.analysisNote = stringOrEmpty(o, "analysisNote");
                rows.add(row);
            }
        } catch (Exception ignored) {
            return List.of();
        }
        return rows;
    }

    private static String formatScore(JsonObject o) {
        if (!o.has("matchScore") || o.get("matchScore").isJsonNull()) {
            return "—";
        }
        try {
            int score = o.get("matchScore").getAsInt();
            score = Math.max(0, Math.min(100, score));
            return score + "%";
        } catch (Exception e) {
            String s = o.get("matchScore").getAsString().trim();
            if (s.endsWith("%")) {
                return s;
            }
            return s.isEmpty() ? "—" : s + "%";
        }
    }

    private static String formatSkills(JsonObject o, String key, boolean matched) {
        if (!o.has(key) || !o.get(key).isJsonArray()) {
            return "—";
        }
        JsonArray arr = o.getAsJsonArray(key);
        if (arr.isEmpty()) {
            return "—";
        }
        String suffix = matched ? "✅" : "❌";
        List<String> parts = new ArrayList<>();
        for (JsonElement el : arr) {
            if (el.isJsonNull()) {
                continue;
            }
            String token = el.getAsString().trim();
            if (!token.isEmpty()) {
                parts.add(token + suffix);
            }
        }
        return parts.isEmpty() ? "—" : parts.stream().collect(Collectors.joining(" "));
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

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
