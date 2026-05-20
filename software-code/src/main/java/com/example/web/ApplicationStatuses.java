package com.example.web;

public final class ApplicationStatuses {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";

    /** Maps API/UI values (incl. ACCEPT/REJECT aliases) to a stored status, or null if invalid. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.trim().toUpperCase()) {
            case "PENDING" -> PENDING;
            case "ACCEPT", "ACCEPTED", "ACCEPTED_BY_MO" -> ACCEPTED;
            case "REJECT", "REJECTED", "REJECTED_BY_MO" -> REJECTED;
            default -> null;
        };
    }

    private ApplicationStatuses() {
    }
}
