package com.example.web.repo;

import com.example.web.Roles;
import com.example.web.model.User;
import com.example.web.model.UserDatabase;
import com.example.web.util.JsonFileStore;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.util.Optional;

public final class UserRepository {

    private static final String FILE = "users.json";
    private final ServletContext ctx;

    public UserRepository(ServletContext ctx) {
        this.ctx = ctx;
    }

    public synchronized UserDatabase load() throws IOException {
        UserDatabase db = JsonFileStore.read(ctx, FILE, UserDatabase.class);
        if (db == null) {
            db = new UserDatabase();
        }
        if (db.users == null) {
            db.users = new java.util.ArrayList<>();
        }
        return db;
    }

    public synchronized void save(UserDatabase db) throws IOException {
        JsonFileStore.write(ctx, FILE, db);
    }

    public synchronized void ensureSeed() throws IOException {
        UserDatabase db = load();
        if (!db.users.isEmpty()) {
            return;
        }
        String hash = BCrypt.hashpw("password", BCrypt.gensalt(10));
        long id = 1;
        db.users.add(seedUser(id++, "alice", hash, Roles.TA, "QM230001", "Alice", "Software Engineering",
                "Java, Git, teamwork, communication", "alice@bupt.edu"));
        db.users.add(seedUser(id++, "bob", hash, Roles.TA, "QM230002", "Bob", "Computer Networks",
                "Java, routing basics", "bob@bupt.edu"));
        db.users.add(seedUser(id++, "charlie", hash, Roles.TA, "QM230003", "Charlie", "Software Engineering",
                "Python", "charlie@bupt.edu"));
        db.users.add(seedUser(id++, "mo1", hash, Roles.MO, "QM200001", "Dr. MO", "School",
                "Module organisation", "mo@bupt.edu"));
        db.users.add(seedUser(id++, "admin1", hash, Roles.ADMIN, "QM100001", "Admin User", "—",
                "—", "admin@bupt.edu"));
        db.nextUserId = id;
        save(db);
    }

    private static User seedUser(long id, String username, String hash, String role, String qm, String name,
                                 String major, String tech, String contact) {
        User u = new User();
        u.id = id;
        u.username = username;
        u.setPasswordHash(hash);
        u.role = role;
        u.qmNumber = qm;
        u.name = name;
        u.major = major;
        u.technicalAbility = tech;
        u.contact = contact;
        return u;
    }

    public Optional<User> findByUsername(String username) throws IOException {
        if (username == null) {
            return Optional.empty();
        }
        String u = username.trim();
        for (User x : load().users) {
            if (u.equalsIgnoreCase(x.username)) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(long id) throws IOException {
        for (User x : load().users) {
            if (x.id != null && x.id == id) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    public synchronized User register(String username, String password, String role, String qmNumber) throws IOException {
        if (findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        UserDatabase db = load();
        User u = new User();
        u.id = db.nextUserId++;
        u.username = username.trim();
        u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        u.role = role;
        u.qmNumber = qmNumber == null ? "" : qmNumber.trim();
        u.name = username.trim();
        u.major = "";
        u.technicalAbility = "";
        u.contact = "";
        db.users.add(u);
        save(db);
        return u;
    }

    public boolean verifyPassword(User user, String plain) {
        return user.getPasswordHash() != null && BCrypt.checkpw(plain, user.getPasswordHash());
    }

    public synchronized void updateProfile(User updated) throws IOException {
        UserDatabase db = load();
        for (int i = 0; i < db.users.size(); i++) {
            if (db.users.get(i).id.equals(updated.id)) {
                User u = db.users.get(i);
                u.name = updated.name;
                u.qmNumber = updated.qmNumber;
                u.major = updated.major;
                u.technicalAbility = updated.technicalAbility;
                u.contact = updated.contact;
                save(db);
                return;
            }
        }
        throw new IllegalArgumentException("User not found");
    }
}
