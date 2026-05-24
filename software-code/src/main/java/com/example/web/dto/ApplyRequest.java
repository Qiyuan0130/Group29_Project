package com.example.web.dto;

/**
 * JSON body for {@code POST /api/applications}.
 */
public class ApplyRequest {

    /** Target job id. */
    public Long jobId;
    /** Id of an uploaded PDF CV owned by the applicant. */
    public Long cvId;
}
