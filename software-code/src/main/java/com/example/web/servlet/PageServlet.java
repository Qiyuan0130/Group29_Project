package com.example.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Placeholder servlet: maps {@code /page/*} to JSP views.
 */
public class PageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "/default";
        }
        String slug = path.startsWith("/") ? path.substring(1) : path;
        req.setAttribute("pageTitle", slug);
        req.getRequestDispatcher("/WEB-INF/jsp/placeholder.jsp").forward(req, resp);
    }
}
