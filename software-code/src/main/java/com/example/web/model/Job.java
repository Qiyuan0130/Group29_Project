package com.example.web.model;

/**
 * Job posting created by a Module Organiser.
 */
public class Job {

    public Long id;
    public String title;
    public String courseName;
    public String requirements;
    public java.util.List<String> requirementsTags;
    public String requirementsNote;
    public String workingHours;
    /** Application deadline (ISO date string). */
    public String deadline;
    /** User id of the MO who posted this job. */
    public Long organizerId;
}
