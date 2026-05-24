package com.example.web.model;

/**
 * A TA application for a specific job posting.
 */
public class ApplicationRecord {

    public Long id;
    public Long jobId;
    public Long applicantId;
    /** CV file id chosen for this application; null on legacy rows. */
    public Long cvId;
    /** {@link com.example.web.ApplicationStatuses PENDING}, ACCEPTED, or REJECTED. */
    public String status;
    /** ISO-8601 timestamp when the application was submitted. */
    public String appliedAt;
}
