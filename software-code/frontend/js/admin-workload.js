/**
 * Admin: TA workload statistics + LLM workload balance.
 */
(function () {
  "use strict";

  function textOrDash(v) {
    if (v == null || String(v).trim() === "") return "—";
    return String(v);
  }

  function rowCells(texts) {
    var tr = document.createElement("tr");
    texts.forEach(function (t) {
      var td = document.createElement("td");
      td.textContent = t == null ? "" : String(t);
      tr.appendChild(td);
    });
    return tr;
  }

  function renderSummary(summaryEl, summary) {
    if (!summaryEl) return;
    if (!summary) {
      summaryEl.textContent = "No summary available.";
      return;
    }
    var parts = [
      "TAs: " + (summary.totalTa != null ? summary.totalTa : 0),
      "Accepted assignments: " + (summary.totalAcceptedAssignments != null ? summary.totalAcceptedAssignments : 0),
      "Avg per TA: " + (summary.avgAcceptedPerTa != null ? summary.avgAcceptedPerTa : 0),
      "Unassigned TAs: " + (summary.taWithNoAssignment != null ? summary.taWithNoAssignment : 0),
    ];
    if (summary.busiestTaName) {
      parts.push(
        "Busiest: " +
          summary.busiestTaName +
          " (" +
          (summary.busiestTaAcceptedCount != null ? summary.busiestTaAcceptedCount : 0) +
          " jobs)"
      );
    }
    summaryEl.textContent = parts.join(" · ");
  }

  function renderWorkloadTable(tbody, rows) {
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!rows || !rows.length) {
      var tr0 = document.createElement("tr");
      var td0 = document.createElement("td");
      td0.colSpan = 7;
      td0.textContent = "No TA accounts yet.";
      tr0.appendChild(td0);
      tbody.appendChild(tr0);
      return;
    }
    rows.forEach(function (r) {
      tbody.appendChild(
        rowCells([
          textOrDash(r.taName),
          textOrDash(r.username),
          r.acceptedJobCount != null ? r.acceptedJobCount : 0,
          r.pendingApplicationCount != null ? r.pendingApplicationCount : 0,
          r.weeklyHoursTotal != null ? r.weeklyHoursTotal + " h/wk" : "—",
          textOrDash(r.courses),
          textOrDash(r.assignedPositions),
        ])
      );
    });
  }

  function renderRuleBalanceTable(tbody, rows) {
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!rows || !rows.length) {
      var tr0 = document.createElement("tr");
      var td0 = document.createElement("td");
      td0.colSpan = 4;
      td0.textContent = "No TA accounts yet.";
      tr0.appendChild(td0);
      tbody.appendChild(tr0);
      return;
    }
    rows.forEach(function (r) {
      tbody.appendChild(
        rowCells([
          textOrDash(r.taName),
          r.weeklyHoursTotal != null ? r.weeklyHoursTotal + " h/wk" : "0 h/wk",
          r.acceptedJobCount != null ? r.acceptedJobCount : 0,
          textOrDash(r.assignedPositions),
        ])
      );
    });
  }

  function renderLlmBalanceTable(tbody, assessments) {
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!assessments || !assessments.length) {
      var tr0 = document.createElement("tr");
      var td0 = document.createElement("td");
      td0.colSpan = 5;
      td0.textContent = "No LLM assessments returned.";
      tr0.appendChild(td0);
      tbody.appendChild(tr0);
      return;
    }
    assessments.forEach(function (a) {
      var reasonable = a.reasonable === true ? "Yes" : a.reasonable === false ? "No" : "—";
      tbody.appendChild(
        rowCells([
          textOrDash(a.taName),
          a.weeklyHoursTotal != null ? a.weeklyHoursTotal + " h/wk" : "—",
          reasonable,
          textOrDash(a.reason),
          textOrDash(a.adjustment),
        ])
      );
    });
  }

  function loadWorkload() {
    var summaryEl = document.getElementById("admin-workload-summary");
    var workloadBody = document.getElementById("admin-workload-body");
    var ruleBalanceBody = document.getElementById("admin-balance-rule-body");
    if (!window.taApi || !window.taApi.adminWorkload) return;

    window.taApi
      .adminWorkload()
      .then(function (data) {
        renderSummary(summaryEl, data && data.summary ? data.summary : null);
        var rows = data && data.rows ? data.rows : [];
        renderWorkloadTable(workloadBody, rows);
        renderRuleBalanceTable(ruleBalanceBody, rows);
      })
      .catch(function (err) {
        var msg = err && err.message ? err.message : "error";
        if (summaryEl) summaryEl.textContent = "Failed to load: " + msg;
        [workloadBody, ruleBalanceBody].forEach(function (tbody) {
          if (!tbody) return;
          tbody.innerHTML = "";
          var trE = document.createElement("tr");
          var tdE = document.createElement("td");
          tdE.colSpan = 7;
          tdE.textContent = "Failed to load: " + msg;
          trE.appendChild(tdE);
          tbody.appendChild(trE);
        });
      });
  }

  function runLlmBalance() {
    var statusEl = document.getElementById("admin-llm-status");
    var teamEl = document.getElementById("admin-llm-team-advice");
    var tbody = document.getElementById("admin-balance-llm-body");
    var btn = document.getElementById("admin-llm-balance-btn");
    if (!window.taApi || !window.taApi.adminWorkloadLlmBalance) return;

    if (statusEl) statusEl.textContent = "Calling LLM… this may take up to a minute.";
    if (teamEl) teamEl.textContent = "";
    if (btn) btn.disabled = true;

    window.taApi
      .adminWorkloadLlmBalance()
      .then(function (data) {
        if (statusEl) {
          statusEl.textContent =
            "Model: " + (data.model || "—") + (data.source ? " · source: " + data.source : "");
        }
        if (teamEl) teamEl.textContent = data.teamAdjustment || "";
        renderLlmBalanceTable(tbody, data.assessments || []);
      })
      .catch(function (err) {
        if (statusEl) statusEl.textContent = "LLM failed: " + (err && err.message ? err.message : "error");
        renderLlmBalanceTable(tbody, []);
      })
      .finally(function () {
        if (btn) btn.disabled = false;
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var exportBtn = document.getElementById("admin-workload-export");
    if (exportBtn) {
      exportBtn.addEventListener("click", function () {
        if (!window.taApi || !window.taApi.adminWorkloadCsvUrl) return;
        window.location.href = window.taApi.adminWorkloadCsvUrl();
      });
    }

    var refreshBtn = document.getElementById("admin-workload-refresh");
    if (refreshBtn) refreshBtn.addEventListener("click", loadWorkload);

    var llmBtn = document.getElementById("admin-llm-balance-btn");
    if (llmBtn) llmBtn.addEventListener("click", runLlmBalance);

    loadWorkload();

    document.querySelectorAll('[data-admin-tab="workload"], [data-admin-tab="balance"]').forEach(function (btn) {
      btn.addEventListener("click", loadWorkload);
    });
  });
})();
