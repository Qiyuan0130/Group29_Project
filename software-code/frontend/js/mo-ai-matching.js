/**
 * MO AI matching: pick a posted job, load applicants, run /api/ai/match-mo.
 */
(function () {
  "use strict";

  var jobSelect = document.getElementById("mo-ai-job-select");
  var tbody = document.getElementById("mo-ai-applicants-body");
  var runBtn = document.getElementById("mo-ai-run-analysis");
  var jobHint = document.getElementById("mo-ai-job-hint");
  if (!jobSelect || !tbody || !runBtn) return;

  var currentJobId = null;
  var currentJob = null;
  var moOwnJobs = [];

  function escHtml(s) {
    return String(s == null ? "" : s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function pendingCell() {
    return '<span class="ai-pending">—</span>';
  }

  function pendingNote() {
    return '<span class="ai-pending">Pending analysis</span>';
  }

  function resetRunButton() {
    runBtn.disabled = !currentJobId;
    runBtn.textContent = "Run AI Analysis";
  }

  function renderEmpty(message) {
    tbody.innerHTML =
      '<tr><td colspan="5"><span class="ai-pending">' + escHtml(message) + "</span></td></tr>";
    runBtn.disabled = true;
  }

  function updateJobHint(appCount) {
    if (!jobHint) return;
    var parts = [];
    if (currentJob) {
      var label = currentJob.title || "Job #" + currentJob.id;
      if (currentJob.courseName) label += " · " + currentJob.courseName;
      parts.push(label);
    }
    if (typeof appCount === "number") {
      parts.push(appCount + (appCount === 1 ? " applicant" : " applicants"));
    }
    jobHint.textContent = parts.join(" — ");
  }

  function renderApplicants(apps) {
    if (!apps.length) {
      renderEmpty("No applications for this job yet.");
      updateJobHint(0);
      return;
    }
    updateJobHint(apps.length);
    tbody.innerHTML = apps
      .map(function (a) {
        var name = a.applicantName || "Applicant";
        var major = a.major ? String(a.major) : "";
        return (
          '<tr class="ai-matching-row" data-applicant-id="' +
          escHtml(a.applicantId) +
          '">' +
          "<td><strong>" +
          escHtml(name) +
          "</strong>" +
          (major
            ? '<br /><span class="text-muted" style="font-size:0.85rem">' + escHtml(major) + "</span>"
            : "") +
          "</td>" +
          '<td class="js-ai-cell-score">' +
          pendingCell() +
          "</td>" +
          '<td class="js-ai-cell-matched">' +
          pendingCell() +
          "</td>" +
          '<td class="js-ai-cell-missing">' +
          pendingCell() +
          "</td>" +
          '<td class="js-ai-cell-note">' +
          pendingNote() +
          "</td>" +
          "</tr>"
        );
      })
      .join("");
    resetRunButton();
  }

  function fillAnalysisRow(tr, row) {
    if (!row) return;
    var elScore = tr.querySelector(".js-ai-cell-score");
    var elMatched = tr.querySelector(".js-ai-cell-matched");
    var elMissing = tr.querySelector(".js-ai-cell-missing");
    var elNote = tr.querySelector(".js-ai-cell-note");
    var score = row.matchScore || "—";
    var matched = row.matchedSkills || "—";
    var missing = row.missingSkills || "—";
    var note = row.analysisNote || "";

    if (elScore) elScore.innerHTML = '<span class="score">' + escHtml(score) + "</span>";
    if (elMatched) {
      if (matched === "—") elMatched.textContent = "—";
      else elMatched.innerHTML = '<span class="skills-ok">' + escHtml(matched) + "</span>";
    }
    if (elMissing) {
      if (missing === "—") elMissing.textContent = "—";
      else elMissing.innerHTML = '<span class="skills-bad">' + escHtml(missing) + "</span>";
    }
    if (elNote) elNote.textContent = note;
    tr.classList.add("is-analyzed");
  }

  function applyMatchResults(rows) {
    var byKey = {};
    (rows || []).forEach(function (r) {
      if (r && r.idKey) byKey[r.idKey] = r;
    });
    tbody.querySelectorAll(".ai-matching-row").forEach(function (tr) {
      var aid = tr.getAttribute("data-applicant-id");
      fillAnalysisRow(tr, byKey["user-" + aid]);
    });
  }

  function loadApplicationsForJob(jobId) {
    if (!jobId) {
      currentJobId = null;
      renderEmpty("Select a job to load applicants.");
      if (jobHint) jobHint.textContent = "";
      return;
    }
    currentJobId = jobId;
    renderEmpty("Loading applicants…");
    runBtn.disabled = true;
    window.taApi
      .moJobApplications(jobId)
      .then(function (data) {
        currentJob = data && data.job ? data.job : null;
        renderApplicants((data && data.applications) || []);
      })
      .catch(function (err) {
        currentJobId = null;
        renderEmpty("Failed to load applicants.");
        if (jobHint) jobHint.textContent = "";
        alert(err.message || "Failed to load applications.");
      });
  }

  function loadMoJobs() {
    window.taApi
      .jobsList()
      .then(function (jobs) {
        return window.taApi.me().then(function (me) {
          var myId = me && me.id != null ? String(me.id) : "";
          moOwnJobs = (Array.isArray(jobs) ? jobs : []).filter(function (j) {
            return String(j && j.organizerId != null ? j.organizerId : "") === myId;
          });
          if (!moOwnJobs.length) {
            jobSelect.innerHTML = '<option value="">No posted jobs</option>';
            loadApplicationsForJob(null);
            return;
          }
          jobSelect.innerHTML = moOwnJobs
            .map(function (j) {
              return (
                '<option value="' +
                escHtml(j.id) +
                '">' +
                escHtml(j.title || "Job #" + j.id) +
                "</option>"
              );
            })
            .join("");
          var params = new URLSearchParams(location.search);
          var wanted = params.get("jobId");
          if (wanted && moOwnJobs.some(function (j) { return String(j.id) === String(wanted); })) {
            jobSelect.value = wanted;
          }
          loadApplicationsForJob(jobSelect.value);
        });
      })
      .catch(function (err) {
        jobSelect.innerHTML = '<option value="">Load failed</option>';
        alert(err.message || "Failed to load jobs.");
      });
  }

  jobSelect.addEventListener("change", function () {
    loadApplicationsForJob(jobSelect.value || null);
  });

  runBtn.addEventListener("click", function () {
    if (!currentJobId) return;
    var rows = tbody.querySelectorAll(".ai-matching-row");
    if (!rows.length) return;
    runBtn.disabled = true;
    runBtn.textContent = "Analyzing…";
    window.taApi
      .aiMatchMo(currentJobId)
      .then(function (data) {
        applyMatchResults((data && data.rows) || []);
        runBtn.textContent = "Analysis complete";
      })
      .catch(function (err) {
        resetRunButton();
        alert(err.message || "AI analysis failed.");
      });
  });

  loadMoJobs();
})();
