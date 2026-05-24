/**
 * Root package for the BUPT International School TA Recruitment web application.
 *
 * <p>The system supports three roles — {@link com.example.web.Roles#TA Teaching Assistant},
 * {@link com.example.web.Roles#MO Module Organiser}, and {@link com.example.web.Roles#ADMIN Admin}.
 * All persistent data is stored in JSON text files (no database).</p>
 *
 * <p>Startup seed data is loaded by {@link com.example.web.BootstrapListener}.
 * Shared repositories are accessed through {@link com.example.web.AppContext}.</p>
 *
 * @see com.example.web.servlet.ApiServlet
 */
package com.example.web;
