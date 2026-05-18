package com.example.web.service;

import com.example.web.ApplicationStatuses;
import com.example.web.model.ApplicationRecord;
import com.example.web.model.Job;
import com.example.web.model.User;
import com.example.web.repo.ApplicationRepository;
import com.example.web.repo.JobRepository;
import com.example.web.repo.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin-only TA workload aggregation (accepted assignments = confirmed load).
 */
public final class WorkloadService {

    private WorkloadService() {
    }

    public static Map<String, Object> buildReport(UserRepository ur, JobRepository jr, ApplicationRepository ar)
            throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        int totalAccepted = 0;
        int taWithNoAssignment = 0;
        String busiestTaName = "";
        int busiestCount = -1;

        for (User ta : ur.listTaUsers()) {
            Map<String, Object> row = buildTaRow(ta, jr, ar);
            rows.add(row);

            int accepted = ((Number) row.get("acceptedJobCount")).intValue();
            totalAccepted += accepted;
            if (accepted == 0) {
                taWithNoAssignment++;
            }
            if (accepted > busiestCount) {
                busiestCount = accepted;
                busiestTaName = String.valueOf(row.get("taName"));
            }
        }

        rows.sort((a, b) -> Integer.compare(
                ((Number) b.get("acceptedJobCount")).intValue(),
                ((Number) a.get("acceptedJobCount")).intValue()));

        int totalTa = rows.size();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTa", totalTa);
        summary.put("totalAcceptedAssignments", totalAccepted);
        summary.put("avgAcceptedPerTa", totalTa == 0 ? 0.0 : round1((double) totalAccepted / totalTa));
        summary.put("taWithNoAssignment", taWithNoAssignment);
        summary.put("busiestTaName", busiestCount < 0 ? "" : busiestTaName);
        summary.put("busiestTaAcceptedCount", Math.max(busiestCount, 0));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("rows", rows);
        return report;
    }

    private static Map<String, Object> buildTaRow(User ta, JobRepository jr, ApplicationRepository ar)
            throws IOException {
        List<String> titles = new ArrayList<>();
        Set<String> courses = new LinkedHashSet<>();
        List<String> hoursParts = new ArrayList<>();
        int pending = 0;

        for (ApplicationRecord a : ar.findByApplicant(ta.id)) {
            if (ApplicationStatuses.PENDING.equals(a.status)) {
                pending++;
                continue;
            }
            if (!ApplicationStatuses.ACCEPTED.equals(a.status)) {
                continue;
            }
            Job j = jr.findById(a.jobId).orElse(null);
            if (j == null) {
                continue;
            }
            if (j.title != null && !j.title.isBlank()) {
                titles.add(j.title.trim());
            }
            if (j.courseName != null && !j.courseName.isBlank()) {
                courses.add(j.courseName.trim());
            }
            if (j.workingHours != null && !j.workingHours.isBlank()) {
                hoursParts.add(j.workingHours.trim());
            }
        }

        int accepted = titles.size();
        String positionsText = titles.isEmpty() ? "None" : String.join(", ", titles);
        String coursesText = courses.isEmpty() ? "—" : String.join(", ", courses);
        String hoursText = hoursParts.isEmpty() ? "—" : String.join("; ", hoursParts);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taId", ta.id);
        row.put("taName", displayName(ta));
        row.put("username", safe(ta.username));
        row.put("qmNumber", safe(ta.qmNumber));
        row.put("acceptedJobCount", accepted);
        row.put("pendingApplicationCount", pending);
        row.put("assignedPositions", positionsText);
        row.put("courses", coursesText);
        row.put("weeklyWorkloadSummary", hoursText);
        row.put("recommendation", recommendationFor(accepted));
        return row;
    }

    static String recommendationFor(int acceptedCount) {
        if (acceptedCount == 0) {
            return "Can assign new positions";
        }
        if (acceptedCount <= 2) {
            return "Can take 1 more job";
        }
        return "Avoid assigning new jobs (high load)";
    }

    private static String displayName(User ta) {
        if (ta.name != null && !ta.name.isBlank()) {
            return ta.name.trim();
        }
        if (ta.username != null && !ta.username.isBlank()) {
            return ta.username.trim();
        }
        return "TA#" + ta.id;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public static String toCsv(Map<String, Object> report) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) report.get("rows");
        StringBuilder sb = new StringBuilder();
        sb.append("TA Name,Login,Accepted Jobs,Pending Apps,Courses,Assigned Positions,Weekly Workload,Recommendation\n");
        if (rows == null) {
            return sb.toString();
        }
        for (Map<String, Object> r : rows) {
            sb.append(escapeCsv(String.valueOf(r.get("taName")))).append(',')
                    .append(escapeCsv(String.valueOf(r.get("username")))).append(',')
                    .append(r.get("acceptedJobCount")).append(',')
                    .append(r.get("pendingApplicationCount")).append(',')
                    .append(escapeCsv(String.valueOf(r.get("courses")))).append(',')
                    .append(escapeCsv(String.valueOf(r.get("assignedPositions")))).append(',')
                    .append(escapeCsv(String.valueOf(r.get("weeklyWorkloadSummary")))).append(',')
                    .append(escapeCsv(String.valueOf(r.get("recommendation")))).append('\n');
        }
        return sb.toString();
    }

    private static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
