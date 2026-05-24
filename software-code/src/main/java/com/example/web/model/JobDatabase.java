package com.example.web.model;

import java.util.ArrayList;
import java.util.List;

/** Root JSON document for {@code jobs.json}. */
public class JobDatabase {

    public long nextJobId = 1;
    public List<Job> jobs = new ArrayList<>();
}
