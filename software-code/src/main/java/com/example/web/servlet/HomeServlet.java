package com.example.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Forwards to {@code /WEB-INF/jsp/home.jsp}.
 */
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("pageTitle", "Home");
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
