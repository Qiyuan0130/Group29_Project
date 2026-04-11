package com.example.web.util;

import java.util.UUID;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Helpers to generate auth tokens for registration and login flows.
 */
public final class AuthTokenUtil {
    
    private AuthTokenUtil() {
    }
    
    /**
     * Generates a unique token (e.g. after registration).
     * @return URL-safe Base64 token string
     */
    public static String generateAuthToken() {
        UUID uuid = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        String combined = uuid.toString() + timestamp;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            // Fallback if SHA-256 is unavailable
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes());
        }
    }
    
    /**
     * Generates a session key (same format as auth token).
     */
    public static String generateSessionKey() {
        return generateAuthToken();
    }
    
    /**
     * Basic format check for a token string.
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // Typical Base64url SHA-256 digest length ~43 chars
        return token.length() > 20 && token.length() < 100;
    }
}
