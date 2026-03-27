package com.example.web.repo;

import com.example.web.model.Job;
import com.example.web.model.JobDatabase;
import com.example.web.util.JsonFileStore;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JobRepository {

    private static final String FILE = "jobs.json";
    private final ServletContext ctx;

    public JobRepository(ServletContext ctx) {
        this.ctx = ctx;
    }

    public synchronized JobDatabase load() throws IOException {
        JobDatabase db = JsonFileStore.read(ctx, FILE, JobDatabase.class);
        if (db == null) {
            db = new JobDatabase();
        }
        if (db.jobs == null) {
            db.jobs = new ArrayList<>();
        }
        return db;
    }

    public synchronized void save(JobDatabase db) throws IOException {
        JsonFileStore.write(ctx, FILE, db);
    }

    public synchronized void ensureSeed(UserRepository users) throws IOException {
        JobDatabase db = load();
        if (!db.jobs.isEmpty()) {
            return;
        }
        Optional<com.example.web.model.User> mo = users.findByUsername("mo1");
        long orgId = mo.map(u -> u.id).orElse(4L);
        long jid = db.nextJobId++;
        db.jobs.add(job(jid++, "TA for EBU6304", "Software Engineering",
                "Basic Java, teamwork, communication skills", "", "4 hrs/week", "2026-04-10", orgId));
        db.jobs.add(job(jid++, "Exam Invigilation Assistant", "School-wide",
                "Punctuality, responsibility", "", "As scheduled", "2026-04-15", orgId));
        db.jobs.add(job(jid++, "TA for EBU5002", "Computer Networks",
                "Network stack, troubleshooting, communication", "", "3 hrs/week", "2026-04-20", orgId));
        db.nextJobId = jid;
        save(db);
    }

    private static Job job(long id, String title, String module, String req, String reqNote, String hours, String deadline, long org) {
        Job j = new Job();
        j.id = id;
        j.title = title;
        j.module = module;
        j.requirements = req;
        j.requirementsTags = new ArrayList<>();
        j.requirementsNote = reqNote;
        j.workingHours = hours;
        j.deadline = deadline;
        j.organizerId = org;
        return j;
    }

    public List<Job> findAll() throws IOException {
        return new ArrayList<>(load().jobs);
    }

    public Optional<Job> findById(long id) throws IOException {
        for (Job j : load().jobs) {
            if (j.id != null && j.id == id) {
                return Optional.of(j);
            }
        }
        return Optional.empty();
    }

    public synchronized Job create(Job job) throws IOException {
        JobDatabase db = load();
        job.id = db.nextJobId++;
        db.jobs.add(job);
        save(db);
        return job;
    }

    public synchronized Job updateByOrganizer(long jobId, long organizerId, Job patch) throws IOException {
        JobDatabase db = load();
        for (Job j : db.jobs) {
            if (j.id == null || j.id != jobId) {
                continue;
            }
            if (j.organizerId == null || j.organizerId != organizerId) {
                throw new SecurityException("Not your job");
            }
            if (patch.title != null) {
                j.title = patch.title;
            }
            if (patch.module != null) {
                j.module = patch.module;
            }
            if (patch.requirements != null) {
                j.requirements = patch.requirements;
            }
            if (patch.requirementsTags != null) {
                j.requirementsTags = new ArrayList<>(patch.requirementsTags);
            }
            if (patch.requirementsNote != null) {
                j.requirementsNote = patch.requirementsNote;
            }
            if (patch.workingHours != null) {
                j.workingHours = patch.workingHours;
            }
            if (patch.deadline != null) {
                j.deadline = patch.deadline;
            }
            save(db);
            return j;
        }
        throw new IllegalArgumentException("Job not found");
    }
}
