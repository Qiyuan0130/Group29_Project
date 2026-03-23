## Estimation Method (Story Points)

The team will use **Story Points** to estimate the relative effort required to implement each user story. Instead of measuring time directly, story points evaluate complexity, development effort, and uncertainty.

### Story Point Scale
The team will adopte a **Fibonacci sequence** scale for estimation:  
**1, 2, 3, 5, 8, 13**

* **1–2**: Very small tasks with minimal risk.
* **3–5**: Medium complexity tasks requiring standard logic.
* **8+**: Complex features requiring significant development effort or involving high uncertainty.

### Estimation Process
Story point estimation was performed during **Sprint Planning** through team consensus. The following factors will be considered for each assignment:

* **Technical Complexity**: Implementing logic for Java-based applications without using frameworks like Spring Boot.
* **Data Handling**: Managing input/output using simple text file formats (.txt, CSV, or JSON).
* **Testing Requirements**: Effort needed for functional and acceptance testing.
* **Dependencies**: Integration between user modules (TA, MO, and Admin).

### Example Story Point Estimation

| User Story | Description | Story Points |
| :--- | :--- | :--- |
| **Create applicant profile** |TA registers and stores personal details in text format. | 3 |
| **Upload CV** | Handle file I/O for CV submission. | 5 |
| **Browse job listings** | Display available TA jobs from data storage. | 3 |
| **Apply for job** | Submit application logic and link to MO view. | 5 |
| **MO posts job** | Create and publish job listing data. | 5 |
| **View application status** | Track application progress for TAs. | 3 |
| **Admin workload monitoring** | Calculate and view TA workload overview. | 8 |

This method allows the team to estimate the workload for each iteration and manage development progress effectively across the four planned sprints.
