package com.example.web.dto;

/**
 * JSON body for {@code POST /api/auth/register}.
 */
public class RegisterRequest {

    /** Display name (letters and digits). */
    public String name;
    /** Unique email; also stored as contact. */
    public String email;
    /** Password (6–10 chars, letters and digits). */
    public String password;
    /** One of TA, MO, ADMIN. */
    public String role;
    /** Required when {@code role} is MO. */
    public String moKey;
    /** Required when {@code role} is ADMIN. */
    public String adminKey;
}
