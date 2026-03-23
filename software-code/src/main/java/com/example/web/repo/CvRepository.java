package com.example.web.repo;

import com.example.web.model.CvDatabase;
import com.example.web.model.CvRecord;
import com.example.web.util.JsonFileStore;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CvRepository {

    private static final String FILE = "cvs.json";
    private final ServletContext ctx;

    public CvRepository(ServletContext ctx) {
        this.ctx = ctx;
    }

    public synchronized CvDatabase load() throws IOException {
        CvDatabase db = JsonFileStore.read(ctx, FILE, CvDatabase.class);
        if (db == null) {
            db = new CvDatabase();
        }
        if (db.cvs == null) {
            db.cvs = new java.util.ArrayList<>();
        }
        return db;
    }

    public synchronized void save(CvDatabase db) throws IOException {
        JsonFileStore.write(ctx, FILE, db);
    }

    public List<CvRecord> findByUser(long userId) throws IOException {
        return load().cvs.stream()
                .filter(c -> c.userId != null && c.userId == userId)
                .collect(Collectors.toList());
    }

    public Optional<CvRecord> findById(long id) throws IOException {
        for (CvRecord c : load().cvs) {
            if (c.id != null && c.id == id) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized CvRecord add(long userId, String originalName, String storedName, long size) throws IOException {
        CvDatabase db = load();
        CvRecord c = new CvRecord();
        c.id = db.nextCvId++;
        c.userId = userId;
        c.originalName = originalName;
        c.storedName = storedName;
        c.uploadedAt = Instant.now().toString();
        c.sizeBytes = size;
        db.cvs.add(c);
        save(db);
        return c;
    }

    public synchronized void delete(long id, Path uploadsDir) throws IOException {
        CvDatabase db = load();
        CvRecord found = null;
        for (CvRecord c : db.cvs) {
            if (c.id != null && c.id == id) {
                found = c;
                break;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("CV not found");
        }
        db.cvs.removeIf(c -> c.id != null && c.id == id);
        save(db);
        if (found.storedName != null) {
            Path f = uploadsDir.resolve(found.storedName);
            Files.deleteIfExists(f);
        }
    }
}
