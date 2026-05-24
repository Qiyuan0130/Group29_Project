package com.example.web;

import com.example.web.repo.ApplicationRepository;
import com.example.web.repo.CvRepository;
import com.example.web.repo.JobRepository;
import com.example.web.repo.UserRepository;

import jakarta.servlet.ServletContext;

/**
 * Application-wide singleton holding JSON-backed repositories.
 *
 * <p>Stored as a {@link ServletContext} attribute so all servlets and filters share
 * the same repository instances.</p>
 */
public final class AppContext {

    public final UserRepository users;
    public final JobRepository jobs;
    public final ApplicationRepository applications;
    public final CvRepository cvs;

    private AppContext(ServletContext ctx) {
        users = new UserRepository(ctx);
        jobs = new JobRepository(ctx);
        applications = new ApplicationRepository(ctx);
        cvs = new CvRepository(ctx);
    }

    /**
     * Returns the shared application context, creating it on first access.
     *
     * @param ctx servlet context
     * @return singleton {@code AppContext}
     */
    public static AppContext get(ServletContext ctx) {
        synchronized (ctx) {
            AppContext a = (AppContext) ctx.getAttribute("appContext");
            if (a == null) {
                a = new AppContext(ctx);
                ctx.setAttribute("appContext", a);
            }
            return a;
        }
    }
}
