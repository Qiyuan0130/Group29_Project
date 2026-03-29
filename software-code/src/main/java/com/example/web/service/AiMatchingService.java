package com.example.web.service;

import com.example.web.dto.MatchResultRow;
import com.example.web.model.Job;
import com.example.web.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 可解释的规则匹配（非大模型），用于 TA/MO 端 AI 分析演示。
 */
public final class AiMatchingService {

    private AiMatchingService() {
    }

    public static List<MatchResultRow> matchJobsForTa(User ta, List<Job> jobs) {
        List<MatchResultRow> rows = new ArrayList<>();
        String profile = safe(ta.technicalAbility) + " " + safe(ta.major);
        for (Job j : jobs) {
            rows.add(matchOneRow("job-" + j.id, j.title, safe(j.courseName), profile, safe(j.requirements)));
        }
        return rows;
    }

    public static List<MatchResultRow> matchApplicantsForMo(User applicant, Job job) {
        List<MatchResultRow> rows = new ArrayList<>();
        String profile = safe(applicant.technicalAbility) + " " + safe(applicant.major);
        rows.add(matchOneRow("user-" + applicant.id, applicant.name, applicant.username, profile, safe(job.requirements)));
        return rows;
    }

    private static MatchResultRow matchOneRow(String idKey, String title, String subtitle, String profile, String requirements) {
        Set<String> reqTokens = tokenize(requirements);
        Set<String> profTokens = tokenize(profile);
        if (reqTokens.isEmpty()) {
            reqTokens.add("general");
        }
        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();
        for (String t : reqTokens) {
            if (profTokens.contains(t) || profile.toLowerCase(Locale.ROOT).contains(t)) {
                matched.add(t);
            } else {
                missing.add(t);
            }
        }
        int score = reqTokens.isEmpty() ? 0 : (int) Math.round(100.0 * matched.size() / reqTokens.size());
        MatchResultRow row = new MatchResultRow();
        row.idKey = idKey;
        row.title = title;
        row.subtitle = subtitle;
        row.matchScore = score + "%";
        row.matchedSkills = matched.isEmpty() ? "—" : matched.stream().map(s -> s + "✅").collect(Collectors.joining(" "));
        row.missingSkills = missing.isEmpty() ? "—" : missing.stream().map(s -> s + "❌").collect(Collectors.joining(" "));
        row.analysisNote = buildNote(score, matched, missing);
        return row;
    }

    private static String buildNote(int score, Set<String> matched, Set<String> missing) {
        if (score >= 80) {
            return "Strong alignment: " + String.join(", ", matched) + ". Keep evidence of these skills visible in CV.";
        }
        if (score >= 40) {
            return "Partial match. Strengthen: " + String.join(", ", missing) + " before applying.";
        }
        return "Large gap vs requirements. Consider coursework or projects in: " + String.join(", ", missing) + ".";
    }

    private static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashSet<>();
        }
        String[] parts = text.toLowerCase(Locale.ROOT).split("[,，\\s;；]+");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.length() > 1)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
