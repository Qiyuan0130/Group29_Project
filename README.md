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

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
## 1. What this software is

A lightweight **Java Servlet / JSP** web application for recruiting Teaching Assistants (TAs):

| Role | Main capabilities |
|------|-------------------|
| **TA** | Profile, CV upload (PDF), browse/apply for jobs, check application status |
| **MO** | Post and edit jobs, review applications (approve/reject), AI skill matching, AI workload balance |
| **ADMIN** | TA workload statistics (CSV export), AI workload view, browse TA profiles |

- **No database** — data stored in **JSON** files and PDF uploads.
- Deployed as **`java-web-json.war`** on **Apache Tomcat 10.1.x**.
- Browser context path: **`/java-web-json/`**

---

## 2. Prerequisites

| Software | Requirement |
|----------|-------------|
| **JDK** | 17+ (project `maven.compiler.release=17`; JDK 23 tested) |
| **Apache Tomcat** | 10.1.x (Jakarta Servlet 5 / EE 9+) |
| **Maven** | 3.9+ (optional — see build without Maven below) |
| **OS** | Windows recommended (PowerShell scripts); manual steps work on macOS/Linux |

---

## 3. Project structure

```
Group29_Project/
├── SETUP_README.md          ← this file (setup / run)
├── README.md                ← group info & meeting records (do not replace)
├── docs/
│   └── User_Manual.md       ← user manual + screenshot checklist
└── software-code/
    ├── frontend/            ← HTML / CSS / JavaScript
    ├── src/main/java/       ← Servlets, services, repositories
    ├── src/test/java/       ← JUnit 5 tests
    ├── data/                ← users.json, jobs.json, applications.json, cvs.json
    ├── uploads/cv/          ← uploaded PDF resumes
    ├── pom.xml
    ├── build-no-maven.ps1   ← build WAR without Maven
    ├── deploy-tomcat.ps1    ← copy WAR to Tomcat webapps
    └── run-tests.ps1        ← run tests (requires Maven)
```

---

## 4. Build the WAR

Open PowerShell in `software-code/`.

### Option A — Without Maven

```powershell
cd software-code
.\build-no-maven.ps1
```

**Output:** `software-code/build/java-web-json.war`

### Option B — With Maven

```powershell
cd software-code
mvn clean package -DskipTests
```

**Output:** `software-code/target/java-web-json.war`

After building, confirm the WAR **modification time** is today. Do not copy an old WAR from weeks ago.

---

## 5. Deploy to Tomcat

Example Tomcat path (adjust to your machine):

`D:\Tomcat\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49`

### Steps

1. **Stop** Tomcat: `bin\shutdown.bat`
2. **Remove** old deployment:
   - `webapps\java-web-json.war`
   - `webapps\java-web-json\` (exploded folder)
3. **Copy** the newly built WAR to `webapps\java-web-json.war`
4. **Start** Tomcat: `bin\startup.bat`

### PowerShell copy example

```powershell
$tomcat = "D:\Tomcat\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
$war    = "D:\MSD\Group29_Project\software-code\build\java-web-json.war"

Remove-Item "$tomcat\webapps\java-web-json" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$tomcat\webapps\java-web-json.war" -Force -ErrorAction SilentlyContinue
Copy-Item $war "$tomcat\webapps\java-web-json.war"
```

### Deploy script (optional)

```powershell
cd software-code
$env:CATALINA_HOME = "D:\Tomcat\apache-tomcat-10.1.49-windows-x64\apache-tomcat-10.1.49"
.\deploy-tomcat.ps1          # uses build\java-web-json.war
# .\deploy-tomcat.ps1 -Maven # uses target\java-web-json.war
```

Then restart Tomcat.

---

## 6. Access URLs

Replace `localhost:8080` if your Tomcat uses another host/port.

| Page | URL |
|------|-----|
| Home | http://localhost:8080/java-web-json/ |
| Login | http://localhost:8080/java-web-json/login.html |
| Register | http://localhost:8080/java-web-json/register.html |
| TA dashboard | http://localhost:8080/java-web-json/ta/dashboard.html |
| MO dashboard | http://localhost:8080/java-web-json/mo/dashboard.html |
| Admin dashboard | http://localhost:8080/java-web-json/admin/dashboard.html |

---

## 7. Configuration

### 7.1 Data and upload directories

At build time, `WEB-INF/app-settings.properties` is generated with paths to:

- `software-code/data/` — JSON data files
- `software-code/uploads/cv/` — TA resume PDFs

If you move the project folder, **rebuild and redeploy** the WAR.

### 7.2 AI / LLM (optional)

MO **AI Matching** and **AI Workload Balance** need an LLM API when enabled.

1. Copy  
   `src/main/webapp/WEB-INF/app-settings.local.properties.example`  
   to `app-settings.local.properties` (same folder).
2. Set `llm.baseUrl`, `llm.apiKey`, `llm.model`.
3. Rebuild and redeploy.

Environment variables `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL` override file values.  
**Do not commit real API keys.**


---

## 8. Demo accounts (seed data in `data/users.json`)

Log in with **username** or **email** and password:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin1234` |
| MO | `dr_smith` | `123456a` |
| MO | `dr_chen` | `123456a` |
| TA | `alice` | `123456a` |
| TA | `bob` | `123456a` |

**Password rules:** 6–10 characters, must include **letters and numbers**.
**TA workflow:** Profile → CV upload → Jobs → apply → Status.

---

## 9. Run automated tests

Requires Maven on PATH:

```powershell
cd software-code
.\run-tests.ps1
```

Or: `mvn test`

---

## 10. Troubleshooting

| Problem | Solution |
|---------|----------|
| `startup.bat` window closes immediately | Port **8080** or **8005** in use — run `shutdown.bat` first, or `netstat -ano \| findstr :8080` then `taskkill /PID <pid> /F` |
| Old UI after deploy | Delete `webapps\java-web-json\`, copy new WAR, restart Tomcat, browser **Ctrl+F5** |
| TA **Jobs** tab locked | Complete Profile and save; upload at least one CV |
| Cannot apply for a job | Profile complete + at least one CV |
| `mvn` not found | Use `.\build-no-maven.ps1` or install Maven |
| Server errors | Check `Tomcat\logs\catalina.<date>.log` |

---

## 11. Technology stack

- **Backend:** Java 17, Jakarta Servlet 5, Gson, jBCrypt  
- **Frontend:** HTML, CSS, JavaScript (packaged in WAR)  
- **Storage:** JSON + PDF files (no SQL)  
- **Server:** Apache Tomcat 10.1.x
