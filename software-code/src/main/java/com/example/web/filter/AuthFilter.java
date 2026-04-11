package com.example.web.filter;

import com.example.web.util.HttpJson;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Protects {@code /api/*} except login, register, and logout.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublic(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object uid = session != null ? session.getAttribute("USER_ID") : null;
        if (uid == null) {
            HttpJson.error(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isPublic(String path, String method) {
        if ("/api/auth/login".equals(path) && "POST".equals(method)) {
            return true;
        }
        if ("/api/auth/register".equals(path) && "POST".equals(method)) {
            return true;
        }
        if ("/api/auth/logout".equals(path) && "POST".equals(method)) {
            return true;
        }
        return false;
    }
}
