package com.example.web;

/**
 * Canonical application status constants for TA job applications.
 */
public final class ApplicationStatuses {

    /** Awaiting MO review. */
    public static final String PENDING = "PENDING";
    /** MO accepted the application. */
    public static final String ACCEPTED = "ACCEPTED";
    /** MO rejected the application. */
    public static final String REJECTED = "REJECTED";

    /**
     * Maps API/UI values (including ACCEPT/REJECT aliases) to a stored status.
     *
     * @param raw status string from client
     * @return normalised status, or {@code null} if invalid
     */
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
