package com.example.web;

import com.example.web.util.JsonPaths;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.IOException;

public class BootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        AppContext app = AppContext.get(ctx);
        try {
            app.users.ensureSeed();
            app.jobs.ensureSeed(app.users);
            app.applications.ensureSeed(app.users);
        } catch (IOException e) {
            throw new IllegalStateException("初始化 JSON 数据失败", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JsonPaths.clearSettingsCache();
    }
}
