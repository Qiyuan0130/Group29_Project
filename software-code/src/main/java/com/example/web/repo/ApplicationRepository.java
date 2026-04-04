package com.example.web.repo;

import com.example.web.ApplicationStatuses;
import com.example.web.model.ApplicationDatabase;
import com.example.web.model.ApplicationRecord;
import com.example.web.model.CvRecord;
import com.example.web.util.JsonFileStore;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ApplicationRepository {

    private static final String FILE = "applications.json";
    private final ServletContext ctx;

    public ApplicationRepository(ServletContext ctx) {
        this.ctx = ctx;
    }

    public synchronized ApplicationDatabase load() throws IOException {
        ApplicationDatabase db = JsonFileStore.read(ctx, FILE, ApplicationDatabase.class);
        if (db == null) {
            db = new ApplicationDatabase();
        }
        if (db.applications == null) {
            db.applications = new ArrayList<>();
        }
        return db;
    }

    public synchronized void save(ApplicationDatabase db) throws IOException {
        JsonFileStore.write(ctx, FILE, db);
    }

    public synchronized void ensureSeed(UserRepository users) throws IOException {
        ApplicationDatabase db = load();
        if (!db.applications.isEmpty()) {
            return;
        }
        Optional<com.example.web.model.User> alice = users.findByUsername("alice");
        Optional<com.example.web.model.User> bob = users.findByUsername("bob");
        long aid = db.nextApplicationId++;
        if (alice.isPresent()) {
            long aId = alice.get().id;
            db.applications.add(app(aid++, 1, aId, ApplicationStatuses.PENDING, "2026-04-03T10:00:00Z"));
            db.applications.add(app(aid++, 2, aId, ApplicationStatuses.ACCEPTED, "2026-04-01T09:00:00Z"));
        }
        if (bob.isPresent()) {
            db.applications.add(app(aid++, 1, bob.get().id, ApplicationStatuses.REJECTED, "2026-03-29T12:00:00Z"));
        }
        db.nextApplicationId = aid;
        save(db);
    }

    private static ApplicationRecord app(long id, long jobId, long applicantId, String status, String at) {
        ApplicationRecord r = new ApplicationRecord();
        r.id = id;
        r.jobId = jobId;
        r.applicantId = applicantId;
        r.status = status;
        r.appliedAt = at;
        return r;
    }

    public List<ApplicationRecord> findByApplicant(long applicantId) throws IOException {
        return load().applications.stream()
                .filter(a -> a.applicantId != null && a.applicantId == applicantId)
                .collect(Collectors.toList());
    }

    public List<ApplicationRecord> findByJob(long jobId) throws IOException {
        return load().applications.stream()
                .filter(a -> a.jobId != null && a.jobId == jobId)
                .collect(Collectors.toList());
    }

    public Optional<ApplicationRecord> findById(long id) throws IOException {
        for (ApplicationRecord a : load().applications) {
            if (a.id != null && a.id == id) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public synchronized ApplicationRecord apply(long jobId, long applicantId, long cvId, CvRepository cvs)
            throws IOException {
        CvRecord cv = cvs.findById(cvId).orElseThrow(() -> new IllegalArgumentException("CV not found"));
        if (cv.userId == null || cv.userId != applicantId) {
            throw new IllegalArgumentException("CV does not belong to you");
        }
        ApplicationDatabase db = load();
        for (ApplicationRecord r : db.applications) {
            if (r.jobId == jobId && r.applicantId == applicantId) {
                throw new IllegalArgumentException("Already applied for this job");
            }
        }
        ApplicationRecord r = new ApplicationRecord();
        r.id = db.nextApplicationId++;
        r.jobId = jobId;
        r.applicantId = applicantId;
        r.cvId = cvId;
        r.status = ApplicationStatuses.PENDING;
        r.appliedAt = Instant.now().toString();
        db.applications.add(r);
        save(db);
        return r;
    }

    public synchronized void updateStatus(long applicationId, String status) throws IOException {
        ApplicationDatabase db = load();
        for (ApplicationRecord r : db.applications) {
            if (r.id != null && r.id == applicationId) {
                r.status = status;
                save(db);
                return;
            }
        }
        throw new IllegalArgumentException("Application not found");
    }

    public List<ApplicationRecord> findAll() throws IOException {
        return new ArrayList<>(load().applications);
    }
}
