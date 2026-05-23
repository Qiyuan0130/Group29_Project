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

## 📅 Meeting Record

| Meeting | Focus | Output |
|---------|-------|--------|
| Meeting 1 | Discussed overall project requirements, analyzed core tasks, and clarified module ownership. | Initial task breakdown and role assignment draft. |
| Meeting 2 | Reviewed completed work in the previous phase and identified items needing adjustment. | Progress check with improvement items. |
| Meeting 3 | Analyzed second-phase priorities and reconfirmed responsibilities based on project progress. | Finalized task allocation for the next stage. |
| Meeting 4 | Checked this week’s completed tasks and discussed the work to be improved and finished next week. | Confirmed current progress and assigned next week’s follow-up tasks. |
| Meeting 5 | Validated Phase 1 outcomes and planned Phase 2 scope. | Phase 1 sign-off and Phase 2 work allocation. |
| Meeting 6 | Reviewed each member’s tasks; aligned on GitHub workflow to reduce merge conflicts; confirmed final assignments. | Accepted individual deliverables, shared branching/PR conventions, and locked final responsibilities. |
| Meeting 7 | Clarified mid-term acceptance criteria; discussed code improvements and expanded automated tests; planned the final release. | Mid-term checklist agreed; improvement and testing actions defined; release plan for the final project. |

### ✅ Current task allocation

| 👤 Member | Iteration 1 | Iteration 2 |
|--------|--------|--------|
| Jingyi Wang | Through JUnit testing, a test file was written to verify the validity of registration inputs. | Completed the implementation of the key/secret system for registration; MOs can filter published recruitments, view TA applications for corresponding activities, as well as approve and reject them. |
| Zixin Xiong | Determine the verification criteria for the three key pieces of information for registration and write them into the code. | Responsible for the resume upload section, users can upload multiple versions of PDF resumes; the "Jobs" function can only be unlocked after the user has completed filling in the profile; implement the verification that at least one resume must be uploaded for the user to apply for a job. |
| Xinyi Li | Refined the registration and login information. Users can register using their name, email, password, and a unique MO key. | Create a job posting page for MOs to set the job title, technical ability, weekly working hours, and deadline. Develop a job list where MOs can view and edit their posted information. |
| Mengdi Yang | JUnit tests are run to ensure that the data are correctly sent to the JSON files used to store user data. | Completed the TA profile part, which now can add TA's information for the MOs and TA themselves to read. After registration, TA's name and email will be automatically shown in the profile that's ready for completion.  |
| Songqi Zhang | JUnit tests are run to ensure that the paths used for JSON data files are set up correctly. | Built the TA-side job list for viewing MO-published jobs; fetched and displayed job details; added multi-keyword tag filtering; added JsonPathsTest. |
| Yangxinyue Zhou | Responsible for designing the full project prototype, defining page layouts, user flows and interaction logic, and guiding the front-end implementation based on this prototype. | Subsequent front-end maintenance |

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
