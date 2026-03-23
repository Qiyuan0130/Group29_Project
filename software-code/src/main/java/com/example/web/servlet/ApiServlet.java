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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
public class ApiServlet extends HttpServlet {

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
            Optional<User> u = ur.findByUsername(body.username);
            if (u.isEmpty() || !ur.verifyPassword(u.get(), body.password)) {
                HttpJson.error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
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
            if (body.username == null || body.password == null || body.role == null) {
                throw new IllegalArgumentException("username, password, role required");
            }
            if (!Roles.TA.equals(body.role) && !Roles.MO.equals(body.role) && !Roles.ADMIN.equals(body.role)) {
                throw new IllegalArgumentException("Invalid role");
            }
            User created = ur.register(body.username, body.password, body.role, body.qmNumber);
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_ID", created.id);
            Map<String, Object> out = new HashMap<>();
            out.put("user", UserPublic.from(created));
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
        if ("/api/jobs".equals(path) && "GET".equals(method)) {
            requireUser(ur, req);
            HttpJson.write(resp, 200, jr.findAll());
            return;
        }
        if ("/api/jobs".equals(path) && "POST".equals(method)) {
            User u = requireUser(ur, req);
            if (!Roles.MO.equals(u.role)) {
                throw new SecurityException("MO only");
            }
            Job job = HttpJson.readBody(req, Job.class);
            job.organizerId = u.id;
            jr.create(job);
            HttpJson.write(resp, 200, job);
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
        String submitted = part.getSubmittedFileName();
        String original = submitted != null ? submitted : "upload.bin";
        String lower = original.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".doc") && !lower.endsWith(".docx")) {
            throw new IllegalArgumentException("Only PDF or DOC/DOCX allowed");
        }
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String stored = UUID.randomUUID() + ext;
        Path dir = JsonPaths.uploadsCvDirectory(getServletContext());
        Files.createDirectories(dir);
        Path target = dir.resolve(stored);
        part.write(target.toAbsolutePath().toString());
        CvRecord rec = app.cvs.add(u.id, original, stored, part.getSize());
        HttpJson.write(resp, 200, rec);
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
}
