package com.example.web.dto;

/**
 * JSON body for {@code POST /api/auth/login}.
 */
public class LoginRequest {

    /** Registered email address (login identifier). */
    public String email;
    /** Account password. */
    public String password;
}
