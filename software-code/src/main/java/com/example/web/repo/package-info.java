/**
 * JSON-backed persistence repositories.
 *
 * <p>Each repository reads and writes a single JSON file in the configured data directory
 * (see {@link com.example.web.util.JsonPaths}). Repositories are thread-safe via
 * {@code synchronized} methods.</p>
 */
package com.example.web.repo;
