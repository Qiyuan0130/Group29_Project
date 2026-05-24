package com.example.web.dto;

/**
 * JSON body for {@code POST /api/ai/match-mo}.
 */
public class MoAiRequest {

    /** MO-owned job id whose applicants should be matched. */
    public Long jobId;
}
