package com.example.web.dto;

/**
 * JSON body for {@code POST /api/mo/applications/{id}/decision}.
 */
public class DecisionRequest {

    /** PENDING, ACCEPTED, or REJECTED (aliases ACCEPT/REJECT also accepted). */
    public String status;

    /** When set, true = ACCEPTED and false = REJECTED (used by MO review UI). */
    public Boolean accept;
}
