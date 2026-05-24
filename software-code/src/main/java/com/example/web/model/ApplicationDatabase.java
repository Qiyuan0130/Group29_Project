package com.example.web.model;

import java.util.ArrayList;
import java.util.List;

/** Root JSON document for {@code applications.json}. */
public class ApplicationDatabase {

    public long nextApplicationId = 1;
    public List<ApplicationRecord> applications = new ArrayList<>();
}
