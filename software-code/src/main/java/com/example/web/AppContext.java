package com.example.web;

import com.example.web.repo.ApplicationRepository;
import com.example.web.repo.CvRepository;
import com.example.web.repo.JobRepository;
import com.example.web.repo.UserRepository;

import jakarta.servlet.ServletContext;

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
