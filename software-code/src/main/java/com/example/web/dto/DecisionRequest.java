package com.example.web.dto;

public class DecisionRequest {

    /** PENDING, ACCEPTED, or REJECTED (aliases ACCEPT/REJECT also accepted). */
    public String status;

    /** Legacy: true = ACCEPTED, false = REJECTED (ignored when {@code status} is set). */
    public Boolean accept;
}
