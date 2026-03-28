package com.example.web.servlet;

import com.example.web.ApplicationStatuses;
import com.example.web.AppContext;
import com.example.web.Roles;
import com.example.web.dto.ApplyRequest;
import com.example.web.dto.DecisionRequest;
import com.example.web.dto.LoginRequest;
import com.example.web.dto.MatchResultRow;
import com.example.web.dto.MoAiRequest;
import com.example.web.dto.ProfileUpdateRequest;
import com.example.web.dto.RegisterRequest;
import com.example.web.dto.UserPublic;
import com.example.web.model.ApplicationRecord;
import com.example.web.model.CvRecord;
import com.example.web.model.Job;
import com.example.web.model.User;
import com.example.web.repo.ApplicationRepository;
import com.example.web.repo.CvRepository;
import com.example.web.repo.JobRepository;
import com.example.web.repo.UserRepository;
import com.example.web.service.AiMatchingService;
import com.example.web.util.HttpJson;
import com.example.web.util.JsonPaths;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
public class ApiServlet extends HttpServlet {
    private static final long MAX_CV_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final String MO_REGISTER_KEY = "qwert1234";
    private static final int MAX_REQUIREMENT_TAGS = 6;
    private static final int MAX_REQUIREMENT_TAG_LENGTH = 10;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AppContext app = AppContext.get(getServletContext());
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();
        try {
            if (path.startsWith("/api/cv/upload") && "POST".equals(method)
                    && req.getContentType() != null
                    && req.getContentType().toLowerCase().startsWith("multipart/")) {
                handleCvUpload(req, resp, app);
                return;
            }
            if (!"GET".equals(method) && req.getContentType() != null
                    && req.getContentType().toLowerCase().startsWith("multipart/")) {
                HttpJson.error(resp, 400, "Use multipart only for /api/cv/upload");
                return;
            }
            route(path, method, req, resp, app);
        } catch (IllegalArgumentException e) {
            HttpJson.error(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            HttpJson.error(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpJson.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void route(String path, String method, HttpServletRequest req, HttpServletResponse resp, AppContext app)
            throws Exception {
        UserRepository ur = app.users;
        JobRepository jr = app.jobs;
        ApplicationRepository ar = app.applications;
        CvRepository cr = app.cvs;

        if ("/api/auth/login".equals(path) && "POST".equals(method)) {
            LoginRequest body = HttpJson.readBody(req, LoginRequest.class);
            Optional<User> u = ur.findByLogin(body.login);
            if (u.isEmpty() || !ur.verifyPassword(u.get(), body.password)) {
                HttpJson.error(resp, HttpServletResponse.SC_UNAUTHORIZED, "姓名/邮箱或密码错误");
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_ID", u.get().id);
            Map<String, Object> out = new HashMap<>();
            out.put("user", UserPublic.from(u.get()));
            HttpJson.write(resp, 200, out);
            return;
        }
        if ("/api/auth/register".equals(path) && "POST".equals(method)) {
            RegisterRequest body = HttpJson.readBody(req, RegisterRequest.class);
            if (body.name == null || body.email == null || body.password == null || body.role == null) {
                throw new IllegalArgumentException("name, email, password, role required");
            }
            String role = body.role.trim().toUpperCase();
            if (Roles.MO.equals(role)) {
                String moKey = body.moKey == null ? "" : body.moKey.trim();
                if (!MO_REGISTER_KEY.equals(moKey)) {
                    throw new IllegalArgumentException("Invalid key. Registration not allowed.");
                }
            }
            User created = ur.register(body.name, body.email, body.password, body.role);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("message", "注册成功，请登录");
            out.put("role", created.role);
            HttpJson.write(resp, 200, out);
            return;
        }
        if ("/api/auth/logout".equals(path) && "POST".equals(method)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            HttpJson.write(resp, 200, Map.of("ok", true));
            return;
        }
        if ("/api/auth/me".equals(path) && "GET".equals(method)) {
            User u = requireUser(ur, req);
            HttpJson.write(resp, 200, UserPublic.from(u));
            return;
        }
        if ("/api/profile".equals(path) && "GET".equals(method)) {
            User u = requireUser(ur, req);
            HttpJson.write(resp, 200, UserPublic.from(u));
            return;
        }
        if ("/api/profile".equals(path) && "PUT".equals(method)) {
            User u = requireUser(ur, req);
            ProfileUpdateRequest body = HttpJson.readBody(req, ProfileUpdateRequest.class);
            u.name = body.name != null ? body.name : u.name;
            u.qmNumber = body.qmNumber != null ? body.qmNumber : u.qmNumber;
            u.major = body.major != null ? body.major : u.major;
            u.technicalAbility = body.technicalAbility != null ? body.technicalAbility : u.technicalAbility;
            u.contact = body.contact != null ? body.contact : u.contact;
            ur.updateProfile(u);
            HttpJson.write(resp, 200, UserPublic.from(ur.findById(u.id).orElse(u)));
            return;
        }
        if ("/api/ta-profiles".equals(path) && "GET".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.ADMIN.equals(u.role) && !Roles.MO.equals(u.role)) {
                throw new SecurityException("Admin or MO only");
            }
            List<UserPublic> profiles = new ArrayList<>();
            for (User ta : ur.listTaUsers()) {
                profiles.add(UserPublic.from(ta));
            }
            HttpJson.write(resp, 200, Map.of("profiles", profiles));
            return;
        }
        if ("/api/jobs".equals(path) && "GET".equals(method)) {
            requireUser(ur, req);
            List<Map<String, Object>> jobRows = new ArrayList<>();
            for (Job j : jr.findAll()) {
                jobRows.add(jobToPublicMap(j, ur));
            }
            HttpJson.write(resp, 200, jobRows);
            return;
        }
        if ("/api/jobs".equals(path) && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            Job job = HttpJson.readBody(req, Job.class);
            validateJobRequiredFields(job);
            validateRequirementTags(job);
            job.organizerId = u.id;
            jr.create(job);
            HttpJson.write(resp, 200, job);
            return;
        }
        if (path.matches("/api/jobs/\\d+") && "PUT".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            long jobId = Long.parseLong(path.replaceFirst("/api/jobs/(\\d+)", "$1"));
            Job patch = HttpJson.readBody(req, Job.class);
            if (patch.title != null && patch.title.trim().isEmpty()) {
                throw new IllegalArgumentException("title cannot be empty");
            }
            validateJobRequiredFields(patch);
            validateRequirementTags(patch);
            Job updated = jr.updateByOrganizer(jobId, u.id, patch);
            HttpJson.write(resp, 200, updated);
            return;
        }
        if ("/api/applications/me".equals(path) && "GET".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.TA.equals(u.role)) {
                throw new SecurityException("TA only");
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            for (ApplicationRecord a : ar.findByApplicant(u.id)) {
                Optional<Job> j = jr.findById(a.jobId);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("applicationId", a.id);
                row.put("jobId", a.jobId);
                row.put("jobTitle", j.map(x -> x.title).orElse(""));
                row.put("module", j.map(x -> x.module).orElse(""));
                row.put("appliedAt", a.appliedAt);
                row.put("status", a.status);
                rows.add(row);
            }
            HttpJson.write(resp, 200, Map.of("applications", rows));
            return;
        }
        if ("/api/applications".equals(path) && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.TA.equals(u.role)) {
                throw new SecurityException("TA only");
            }
            ApplyRequest body = HttpJson.readBody(req, ApplyRequest.class);
            if (body.jobId == null) {
                throw new IllegalArgumentException("jobId required");
            }
            ApplicationRecord created = ar.apply(body.jobId, u.id);
            HttpJson.write(resp, 200, created);
            return;
        }

        if (path.matches("/api/mo/jobs/\\d+/applications") && "GET".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            long jobId = Long.parseLong(path.replaceFirst("/api/mo/jobs/(\\d+)/applications", "$1"));
            Job job = jr.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found"));
            if (!u.id.equals(job.organizerId)) {
                throw new SecurityException("Not your job");
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            for (ApplicationRecord a : ar.findByJob(jobId)) {
                User applicant = ur.findById(a.applicantId).orElseThrow();
                Optional<CvRecord> cv = cr.findByUser(applicant.id).stream().findFirst();
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("applicationId", a.id);
                row.put("applicantName", applicant.name);
                row.put("qmNumber", applicant.qmNumber);
                row.put("status", a.status);
                row.put("cvFileName", cv.map(c -> c.originalName).orElse(""));
                rows.add(row);
            }
            HttpJson.write(resp, 200, Map.of("job", job, "applications", rows));
            return;
        }

        if (path.matches("/api/mo/applications/\\d+/decision") && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            long appId = Long.parseLong(path.replaceFirst("/api/mo/applications/(\\d+)/decision", "$1"));
            DecisionRequest body = HttpJson.readBody(req, DecisionRequest.class);
            ApplicationRecord a = ar.findById(appId).orElseThrow(() -> new IllegalArgumentException("Application not found"));
            Job job = jr.findById(a.jobId).orElseThrow();
            if (!u.id.equals(job.organizerId)) {
                throw new SecurityException("Not your job");
            }
            boolean accept = Boolean.TRUE.equals(body.accept);
            ar.updateStatus(appId, accept ? ApplicationStatuses.ACCEPTED : ApplicationStatuses.REJECTED);
            HttpJson.write(resp, 200, Map.of("ok", true, "status", accept ? ApplicationStatuses.ACCEPTED : ApplicationStatuses.REJECTED));
            return;
        }

        if ("/api/cv/list".equals(path) && "GET".equals(method)) {
            User u = requireUser(ur, req);
            HttpJson.write(resp, 200, Map.of("files", cr.findByUser(u.id)));
            return;
        }

        if (path.matches("/api/cv/\\d+/view") && "GET".equals(method)) {
            User u = requireUser(ur, req);
            long cvId = Long.parseLong(path.replaceFirst("/api/cv/(\\d+)/view", "$1"));
            CvRecord c = cr.findById(cvId).orElseThrow(() -> new IllegalArgumentException("CV not found"));
            if (!u.id.equals(c.userId)) {
                throw new SecurityException("Not your file");
            }
            Path file = JsonPaths.uploadsCvDirectory(getServletContext()).resolve(c.storedName == null ? "" : c.storedName);
            if (!Files.exists(file)) {
                throw new IllegalArgumentException("CV file missing on server");
            }
            resp.setStatus(200);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + (c.originalName == null ? "cv.pdf" : c.originalName) + "\"");
            resp.setContentLengthLong(Files.size(file));
            try (InputStream in = Files.newInputStream(file)) {
                in.transferTo(resp.getOutputStream());
            }
            return;
        }

        if (path.matches("/api/cv/\\d+") && "DELETE".equals(method)) {
            User u = requireUser(ur, req);
            long cvId = Long.parseLong(path.replace("/api/cv/", ""));
            CvRecord c = cr.findById(cvId).orElseThrow(() -> new IllegalArgumentException("CV not found"));
            if (!u.id.equals(c.userId)) {
                throw new SecurityException("Not your file");
            }
            cr.delete(cvId, JsonPaths.uploadsCvDirectory(getServletContext()));
            HttpJson.write(resp, 200, Map.of("ok", true));
            return;
        }

        if ("/api/admin/workload".equals(path) && "GET".equals(method)) {
            User admin = requireUser(ur, req);
            if (!Roles.ADMIN.equals(admin.role)) {
                throw new SecurityException("Admin only");
            }
            HttpJson.write(resp, 200, Map.of("rows", buildWorkloadRows(ur, jr, ar)));
            return;
        }

        if ("/api/admin/workload.csv".equals(path) && "GET".equals(method)) {
            User admin = requireUser(ur, req);
            if (!Roles.ADMIN.equals(admin.role)) {
                throw new SecurityException("Admin only");
            }
            String csv = toCsv(buildWorkloadRows(ur, jr, ar));
            resp.setStatus(200);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/csv;charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"workload.csv\"");
            resp.getWriter().write(csv);
            return;
        }

        if ("/api/ai/match-ta".equals(path) && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.TA.equals(u.role)) {
                throw new SecurityException("TA only");
            }
            User fresh = ur.findById(u.id).orElse(u);
            List<MatchResultRow> rows = AiMatchingService.matchJobsForTa(fresh, jr.findAll());
            HttpJson.write(resp, 200, Map.of("rows", rows));
            return;
        }

        if ("/api/ai/match-mo".equals(path) && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            MoAiRequest body = HttpJson.readBody(req, MoAiRequest.class);
            if (body.jobId == null) {
                throw new IllegalArgumentException("jobId required");
            }
            Job job = jr.findById(body.jobId).orElseThrow(() -> new IllegalArgumentException("Job not found"));
            if (!u.id.equals(job.organizerId)) {
                throw new SecurityException("Not your job");
            }
            List<MatchResultRow> rows = new ArrayList<>();
            for (ApplicationRecord a : ar.findByJob(body.jobId)) {
                User applicant = ur.findById(a.applicantId).orElseThrow();
                rows.addAll(AiMatchingService.matchApplicantsForMo(applicant, job));
            }
            HttpJson.write(resp, 200, Map.of("rows", rows));
            return;
        }

        HttpJson.error(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown API: " + path);
    }

    private void handleCvUpload(HttpServletRequest req, HttpServletResponse resp, AppContext app) throws Exception {
        User u = requireUser(app.users, req);
        if (!Roles.TA.equals(u.role)) {
            throw new SecurityException("TA only");
        }
        Part part = req.getPart("file");
        if (part == null || part.getSize() == 0) {
            throw new IllegalArgumentException("file part required");
        }
        if (part.getSize() > MAX_CV_SIZE_BYTES) {
            throw new IllegalArgumentException("PDF too large. Max 5MB");
        }
        String submitted = part.getSubmittedFileName();
        String original = submitted != null ? submitted : "upload.bin";
        String lower = original.toLowerCase();
        String contentType = part.getContentType() == null ? "" : part.getContentType().toLowerCase();
        if (!lower.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF allowed");
        }
        if (!contentType.isEmpty() && !contentType.contains("pdf")) {
            throw new IllegalArgumentException("Only PDF allowed");
        }
        Path dir = JsonPaths.uploadsCvDirectory(getServletContext());
        Files.createDirectories(dir);
        String stored = generateStoredCvName(u.username, dir);
        Path target = dir.resolve(stored);
        part.write(target.toAbsolutePath().toString());
        CvRecord rec = app.cvs.add(u.id, original, stored, part.getSize());
        HttpJson.write(resp, 200, rec);
    }

    private static String generateStoredCvName(String username, Path dir) {
        String base = username == null ? "user" : username.trim();
        if (base.isEmpty()) {
            base = "user";
        }
        String safeBase = base.replaceAll("[^A-Za-z0-9_-]", "_");
        for (int i = 0; i < 100; i++) {
            int n = ThreadLocalRandom.current().nextInt(0, 10000);
            String candidate = safeBase + String.format("%04d", n) + ".pdf";
            if (!Files.exists(dir.resolve(candidate))) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate unique CV file name");
    }

    private static List<Map<String, Object>> buildWorkloadRows(UserRepository ur, JobRepository jr, ApplicationRepository ar)
            throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : ur.load().users) {
            if (!Roles.TA.equals(u.role)) {
                continue;
            }
            List<String> titles = new ArrayList<>();
            for (ApplicationRecord a : ar.findByApplicant(u.id)) {
                if (ApplicationStatuses.ACCEPTED.equals(a.status)) {
                    jr.findById(a.jobId).ifPresent(j -> titles.add(j.title));
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("taName", u.name);
            row.put("assignedPositions", titles.isEmpty() ? "None" : String.join(", ", titles));
            rows.add(row);
        }
        return rows;
    }

    private static String toCsv(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("TA Name,Assigned Positions\n");
        for (Map<String, Object> r : rows) {
            sb.append(escapeCsv(String.valueOf(r.get("taName"))))
                    .append(',')
                    .append(escapeCsv(String.valueOf(r.get("assignedPositions"))))
                    .append('\n');
        }
        return sb.toString();
    }

    private static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    private static Map<String, Object> jobToPublicMap(Job j, UserRepository ur) throws IOException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", j.id);
        m.put("title", j.title);
        m.put("module", j.module);
        m.put("requirements", j.requirements);
        m.put("requirementsTags", j.requirementsTags != null ? new ArrayList<>(j.requirementsTags) : new ArrayList<>());
        m.put("requirementsNote", j.requirementsNote);
        m.put("workingHours", j.workingHours);
        m.put("deadline", j.deadline);
        m.put("organizerId", j.organizerId);
        String moName = "";
        if (j.organizerId != null) {
            Optional<User> ou = ur.findById(j.organizerId);
            if (ou.isPresent()) {
                User mo = ou.get();
                if (mo.name != null && !mo.name.trim().isEmpty()) {
                    moName = mo.name.trim();
                } else if (mo.username != null && !mo.username.trim().isEmpty()) {
                    moName = mo.username.trim();
                }
            }
        }
        m.put("organizerName", moName.isEmpty() ? "Unknown" : moName);
        return m;
    }

    private static User requireUser(UserRepository ur, HttpServletRequest req) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new SecurityException("Not logged in");
        }
        Object uid = session.getAttribute("USER_ID");
        if (!(uid instanceof Long)) {
            throw new SecurityException("Not logged in");
        }
        return ur.findById((Long) uid).orElseThrow(() -> new SecurityException("User missing"));
    }

    private static void validateRequirementTags(Job job) {
        if (job == null || job.requirementsTags == null) {
            return;
        }
        if (job.requirementsTags.size() > MAX_REQUIREMENT_TAGS) {
            throw new IllegalArgumentException("You can add up to 6 tags.");
        }
        for (String t : job.requirementsTags) {
            String tag = t == null ? "" : t.trim();
            if (tag.isEmpty()) {
                throw new IllegalArgumentException("Tag cannot be empty.");
            }
            if (tag.length() > MAX_REQUIREMENT_TAG_LENGTH) {
                throw new IllegalArgumentException("Each tag must be at most 10 characters.");
            }
        }
    }

    private static void validateJobRequiredFields(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job payload required.");
        }
        String title = job.title == null ? "" : job.title.trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Job title is required.");
        }
        if (job.requirementsTags == null || job.requirementsTags.isEmpty()) {
            throw new IllegalArgumentException("At least one requirement tag is required.");
        }
        String hours = job.workingHours == null ? "" : job.workingHours.trim();
        if (hours.isEmpty()) {
            throw new IllegalArgumentException("Weekly working hours is required.");
        }
        if (!hours.matches("\\d+")) {
            throw new IllegalArgumentException("Weekly working hours must contain digits only.");
        }
        String deadline = job.deadline == null ? "" : job.deadline.trim();
        if (deadline.isEmpty()) {
            throw new IllegalArgumentException("Deadline is required.");
        }
    }
}
