package com.example.web.model;

/**
 * Metadata for an uploaded PDF CV file.
 */
public class CvRecord {

    public Long id;
    public Long userId;
    /** Original filename from the client upload. */
    public String originalName;
    /** Server-side stored filename under the uploads directory. */
    public String storedName;
    public String uploadedAt;
    public Long sizeBytes;
}
