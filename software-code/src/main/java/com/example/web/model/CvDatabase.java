package com.example.web.model;

import java.util.ArrayList;
import java.util.List;

/** Root JSON document for {@code cvs.json}. */
public class CvDatabase {

    public long nextCvId = 1;
    public List<CvRecord> cvs = new ArrayList<>();
}
