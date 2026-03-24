package com.example.web.repo;

import com.example.web.Roles;
import com.example.web.model.User;
import com.example.web.model.UserDatabase;
import com.example.web.util.JsonFileStore;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public final class UserRepository {

    private static final String FILE = "users.json";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,10}$");
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

    public Optional<User> findByName(String name) throws IOException {
        if (name == null) {
            return Optional.empty();
        }
        String n = name.trim();
        for (User x : load().users) {
            if (x.name != null && n.equalsIgnoreCase(x.name.trim())) {
                return Optional.of(x);
            }
            // 兼容历史数据：部分账号把姓名存进了 username 字段
            if (x.username != null && n.equalsIgnoreCase(x.username.trim())) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws IOException {
        if (email == null) {
            return Optional.empty();
        }
        String e = email.trim();
        for (User x : load().users) {
            if (x.contact != null && e.equalsIgnoreCase(x.contact.trim())) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByLogin(String login) throws IOException {
        if (login == null) {
            return Optional.empty();
        }
        String key = login.trim();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> byEmail = findByEmail(key);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return findByName(key);
    }

    public Optional<User> findById(long id) throws IOException {
        for (User x : load().users) {
            if (x.id != null && x.id == id) {
                return Optional.of(x);
            }
        }
        return Optional.empty();
    }

    public synchronized User registerTa(String name, String email, String password) throws IOException {
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        String rawPassword = password == null ? "" : password;
        if (!NAME_PATTERN.matcher(cleanName).matches()) {
            throw new IllegalArgumentException("姓名只能包含字母和数字");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("密码需为6-10位，且必须同时包含字母和数字");
        }
        if (findByName(cleanName).isPresent()) {
            throw new IllegalArgumentException("姓名已被注册");
        }
        if (findByEmail(cleanEmail).isPresent()) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        UserDatabase db = load();
        User u = new User();
        u.id = db.nextUserId++;
        u.username = cleanName;
        u.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
        u.role = Roles.TA;
        u.qmNumber = "";
        u.name = cleanName;
        u.major = "";
        u.technicalAbility = "";
        u.contact = cleanEmail;
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
