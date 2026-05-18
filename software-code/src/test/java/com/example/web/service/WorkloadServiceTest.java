package com.example.web.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkloadServiceTest {

    @Test
    void recommendationFor_zeroAccepted() {
        assertEquals("Can assign new positions", WorkloadService.recommendationFor(0));
    }

    @Test
    void recommendationFor_lowLoad() {
        assertEquals("Can take 1 more job", WorkloadService.recommendationFor(1));
        assertEquals("Can take 1 more job", WorkloadService.recommendationFor(2));
    }

    @Test
    void recommendationFor_highLoad() {
        assertEquals("Avoid assigning new jobs (high load)", WorkloadService.recommendationFor(3));
        assertEquals("Avoid assigning new jobs (high load)", WorkloadService.recommendationFor(5));
    }
}
