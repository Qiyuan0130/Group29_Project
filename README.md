<div align="center">

# 📘 Group29_Project

**📚 BUPT International School Teaching Assistant Recruitment System**  
*🔄 Developed using Agile methods for EBU6304 Software Engineering Group Project.*

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

</div>

## 👥 Group Name-List

- 🏷️ GitHub Account:QMID(Lead/Member)
- qrsikno2:190898878(Support TA)
- ACCOUNT for Qiyuan0130:231220149(L)
- ACCOUNT for pinkpiggyVv:231222006(M)
- ACCOUNT for C3H604:231220541(M)
- ACCOUNT for twila-suyuluo:231221630(M)
- ACCOUNT for lindbs1:231220644(M)
- ACCOUNT for zyxy7:231222800(M)

---


# BUPT TA Recruitment System — Setup, Configuration & Run Guide

> **Document type:** Installation & operations guide  
> **Course:** EBU6304 Software Engineering · BUPT International School · Group 29  
> **Audience:** Developers, reviewers, and anyone deploying the application locally

This document explains how to set up, configure, and run the **BUPT International School TA Recruitment System** (Group 29, EBU6304). It walks through prerequisites, project layout, build and deploy steps, optional AI configuration, and common troubleshooting—so you can go from a fresh checkout to a working Tomcat deployment without guessing paths or scripts.

Application code is in the `software-code/` folder. Follow the sections below in order for a first-time setup, or jump via the table of contents if you only need configuration or troubleshooting.

---

## Table of Contents

| # | Section | What you will find |
|---|---------|-------------------|
| 1 | [Overview](#overview) | System purpose, roles, and technology stack |
| 2 | [Prerequisites](#prerequisites) | Required software versions and install hints |
| 3 | [Project Structure](#project-structure) | Repository layout and runtime data locations |
| 4 | [Quick Start](#quick-start) | Minimal commands to build, deploy, and open the app |
| 5 | [Configuration](#configuration) | Data paths, LLM, registration keys, passwords |
| 6 | [Build the Application](#build-the-application) | Maven and no-Maven build options |
| 7 | [Deploy & Run on Tomcat](#deploy--run-on-tomcat) | Script-based and manual deployment |
| 8 | [Access the Application](#access-the-application) | URLs for pages and REST API |
| 9 | [Demo Accounts](#demo-accounts) | Pre-seeded login credentials for testing |
| 10 | [Run Unit Tests](#run-unit-tests) | How to execute the test suite |
| 11 | [Troubleshooting](#troubleshooting) | Frequent issues and fixes |

---

## Overview

The system supports the full TA recruitment workflow: module organisers publish positions, teaching assistants apply with profiles and CVs, and administrators review workload and optional AI-assisted analysis. The table below summarises the main characteristics at a glance.

| Item | Detail |
|------|--------|
| **Purpose** | TA recruitment: job posting (MO), applications (TA), workload & AI analysis (Admin) |
| **Roles** | TA, MO (Module Organiser), ADMIN |
| **Backend** | Java 17, Jakarta Servlet, JSON file storage (no database) |
| **Frontend** | HTML / CSS / JavaScript |
| **Server** | Apache Tomcat 10.1+ |
| **Context path** | `/java-web-json` |

**Note:** There is no separate database server; persistence is via JSON files under `software-code/data/`, which simplifies local development and demo deployments.

---

## Prerequisites

Before building or deploying, ensure your machine meets the requirements below. Missing or incompatible versions (especially Tomcat 9 vs 10) are a common source of deployment failures.

| Requirement | Version / Notes |
|-------------|-----------------|
| **JDK** | 17+ (`java -version`, `javac -version`) |
| **Apache Tomcat** | 10.1+ (Jakarta EE; **not** Tomcat 9 or earlier) |
| **Maven** | 3.9+ (optional — see no-Maven build below) |
| **PowerShell** | Windows — for build/deploy scripts |
| **Network** | Required once to download dependencies |

### Install examples (Windows)

The following commands use `winget` to install a JDK and Maven quickly. Adjust if you prefer manual installers or a different package manager.

```powershell
winget install Microsoft.OpenJDK.17
winget install Apache.Maven
```

**Tomcat 10.1 download:** [https://tomcat.apache.org/download-10.cgi](https://tomcat.apache.org/download-10.cgi)

After installing Tomcat, set `CATALINA_HOME` to your Tomcat root directory (see [Quick Start](#quick-start) and [Deploy & Run on Tomcat](#deploy--run-on-tomcat)).

---

## Project Structure

The repository is organised so that documentation sits at the group root and all runnable application assets live under `software-code/`. The tree below shows the main folders and scripts you will use during setup.

```
Group29_Project/
├── Software_Setup_README.md     ← this file
├── README.md                    ← group project info (unchanged)
└── software-code/
    ├── frontend/                # HTML / CSS / JS pages
    ├── src/main/java/           # Java backend
    ├── src/main/webapp/WEB-INF/
    │   ├── web.xml
    │   └── app-settings.local.properties.example
    ├── data/                    # JSON data (users, jobs, applications, CVs)
    ├── uploads/cv/              # Uploaded PDF résumés
    ├── build-no-maven.ps1       # Build WAR without Maven
    ├── deploy-tomcat.ps1        # Deploy WAR to Tomcat
    ├── run-tests.ps1            # Run JUnit tests
    └── pom.xml                  # Maven build (alternative)
```

### Runtime data locations

At runtime the app reads/writes:

- `software-code/data/*.json` — accounts, jobs, applications, CV metadata
- `software-code/uploads/cv/*.pdf` — TA résumés (max 5 MB each)

Paths are set in `WEB-INF/app-settings.properties`, generated automatically when you build. Keeping data outside the exploded WAR avoids losing files when you redeploy (see [Configuration](#configuration)).

---

## Quick Start

Use this section for the shortest path from clone to browser. Replace the Tomcat path with your own installation directory.

```powershell
# 1. Go to the application folder
cd software-code

# 2. Build the WAR file
.\build-no-maven.ps1

# 3. Point to your Tomcat installation (change the path if needed)
$env:CATALINA_HOME = "D:\Tomcat\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"

# 4. Deploy the WAR
.\deploy-tomcat.ps1

# 5. Start Tomcat, then open in browser:
#    http://localhost:8080/java-web-json/
```

> **After deploying:** Restart Tomcat and press **Ctrl+F5** in the browser to avoid cached pages showing an older UI.

For detailed build options, LLM setup, and manual deploy steps, see the following sections.

---

## Configuration

This section covers where data is stored, how to enable optional LLM features, registration keys for new accounts, and password rules enforced by the application.

### Data and upload directories

Each build writes `WEB-INF/app-settings.properties` inside the WAR with absolute paths to:

- `software-code/data`
- `software-code/uploads/cv`

Data is stored **outside** the exploded WAR so redeploying does not delete your JSON files or PDFs.

If you move the project folder, **rebuild the WAR** so paths stay correct.

### LLM / AI features (optional)

Used by **Admin** (workload balance) and **MO** (AI application matching). These features call an external LLM API when configured; otherwise the app uses fallbacks as noted below.

**Step 1 — Copy the example config:**

```powershell
cd software-code
copy src\main\webapp\WEB-INF\app-settings.local.properties.example `
     src\main\webapp\WEB-INF\app-settings.local.properties
```

**Step 2 — Edit** `src/main/webapp/WEB-INF/app-settings.local.properties`:

```properties
llm.baseUrl=https://api.chatanywhere.tech/v1
llm.apiKey=your-api-key-here
llm.model=gpt-5.1
```

**Step 3 —** Rebuild and redeploy the WAR.

**Environment variables** (override file settings):

| Variable | Description |
|----------|-------------|
| `LLM_BASE_URL` | API base URL |
| `LLM_API_KEY` | API key |
| `LLM_MODEL` | Model name |

Without LLM config, MO matching falls back to rule-based logic; Admin LLM analysis will report a configuration error.

### Registration keys

New MO and Admin accounts require a registration key at sign-up. TA registration does not use a key.

| Role | Registration key |
|------|------------------|
| MO | `qwert1234` |
| Admin | `Group29admin` |
| TA | No key required |

### Password rules

All user passwords must satisfy:

- 6–10 characters
- Must include both letters and numbers
- Letters and digits only (no special symbols)

---

## Build the Application

You can build the deployable WAR either with the included PowerShell script (no Maven required) or with Maven. Both produce a WAR named `java-web-json.war` suitable for Tomcat 10.1+.

### Option A — Without Maven (recommended on Windows)

```powershell
cd software-code
.\build-no-maven.ps1
```

**Output:** `software-code/build/java-web-json.war`

The script:

1. Copies `frontend/` into the WAR
2. Compiles Java sources (downloads JARs if needed)
3. Generates `app-settings.properties` with correct data paths
4. Includes `app-settings.local.properties` if it exists

### Option B — With Maven

```powershell
cd software-code
mvn clean package -DskipTests
```

**Output:** `software-code/target/java-web-json.war`

If Maven is bundled in the repo:

```powershell
..\.tools\apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests
```

---

## Deploy & Run on Tomcat

Deployment copies the built WAR into Tomcat’s `webapps/` directory. Tomcat expands it on startup; the application is then available at the context path `/java-web-json`.

### Using the deploy script

```powershell
cd software-code
$env:CATALINA_HOME = "C:\path\to\apache-tomcat-10.1.49"

# Default: builds with build-no-maven.ps1, then copies WAR
.\deploy-tomcat.ps1

# Or deploy a Maven-built WAR:
.\deploy-tomcat.ps1 -Maven
```

### Manual deploy

1. Copy `java-web-json.war` to `<TOMCAT>/webapps/`
2. Start Tomcat:

   ```powershell
   & "$env:CATALINA_HOME\bin\startup.bat"
   ```

3. Wait for Tomcat to expand the WAR, then open the URLs in [Access the Application](#access-the-application).

### VS Code / Cursor Tomcat extension

1. Run `.\build-no-maven.ps1` (or VS Code task **Build WAR**)
2. Publish `build/java-web-json.war` from the Tomcat extension
3. Restart the server

---

## Access the Application

Once Tomcat is running and the WAR has expanded, use the base URL below (default HTTP port **8080**). Always access pages through Tomcat—not by opening HTML files directly from disk—or API calls and login will fail.

**Base URL:**

```
http://localhost:8080/java-web-json/
```

| Page | URL |
|------|-----|
| Home | `/java-web-json/index.html` |
| Register | `/java-web-json/register.html` |
| Login | `/java-web-json/login.html` |
| TA dashboard | `/java-web-json/ta/dashboard.html` |
| MO dashboard | `/java-web-json/mo/dashboard.html` |
| Admin dashboard | `/java-web-json/admin/dashboard.html` |

**REST API base:** `/java-web-json/api/`

---

## Demo Accounts

Pre-seeded in `software-code/data/users.json`. Use the **email** as login username. These accounts are intended for local testing and demonstrations only; change or remove them in production-like environments.

| Role | Login (email) | Password | Name |
|------|---------------|----------|------|
| MO | `j.smith@bupt.edu` | `123456a` | Dr. James Smith |
| MO | `w.chen@bupt.edu` | `123456a` | Dr. Wei Chen |
| Admin | `admin@bupt.edu` | `admin1234` | System Administrator |
| TA | `alice.wang@bupt.edu` | `123456a` | Alice Wang |
| TA | `bob.li@bupt.edu` | `123456a` | Bob Li |
| TA | `charlie.zhang@bupt.edu` | `123456a` | Charlie Zhang |
| TA | `diana.liu@bupt.edu` | `123456a` | Diana Liu |
| TA | `eve.chen@bupt.edu` | `123456a` | Eve Chen |

---

## Run Unit Tests

Unit tests require Maven on your PATH. From the `software-code` directory, use the project script or invoke Maven directly.

```powershell
cd software-code
.\run-tests.ps1 test
```

Or:

```powershell
mvn test
```

---

## Troubleshooting

If something does not work as expected, check the table below before changing code. Most issues relate to Tomcat version, context path, cache, or incomplete TA profile/CV setup.

| Problem | Solution |
|---------|----------|
| **404 or blank page** | Check Tomcat is running and WAR is in `webapps/`. Context path must be `/java-web-json`. |
| **Login / network error** | Open via Tomcat URL, not by double-clicking HTML files. |
| **Old UI after deploy** | Delete `webapps/java-web-json/`, redeploy WAR, restart Tomcat, **Ctrl+F5**. |
| **TA cannot open Jobs** | Complete all five Profile fields, Save, upload at least one PDF on CV Upload. |
| **Cannot apply for a job** | Complete profile/CV, or job deadline may have passed. |
| **LLM analysis failed** | Set API key in `app-settings.local.properties` and rebuild/redeploy. |
| **Data not saved** | Rebuild WAR so `app-settings.properties` points to the correct `data/` folder. |
| **`javac` / `mvn` not found** | Install JDK 17 and/or Maven; reopen terminal. |

---

## Related Documents

- **Group info & meeting records:** [README.md](README.md)
- **User manuals:** `docs/User_Manual_Group_29_English.docx`, `docs/User_Manual_Group_29_Chinese.docx` (if present in your workspace)

---

*Group 29 · EBU6304 Software Engineering · BUPT International School*
