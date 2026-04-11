package com.example.web.model;

public class ApplicationRecord {

    public Long id;
    public Long jobId;
    public Long applicantId;
    /** CV file id chosen for this application; null on legacy rows. */
    public Long cvId;
    public String status;
    public String appliedAt;
}
