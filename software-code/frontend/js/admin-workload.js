/**
 * Admin: TA workload statistics and balance suggestions from GET /api/admin/workload.
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
      td0.colSpan = 8;
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
          textOrDash(r.qmNumber),
          r.acceptedJobCount != null ? r.acceptedJobCount : 0,
          r.pendingApplicationCount != null ? r.pendingApplicationCount : 0,
          textOrDash(r.courses),
          textOrDash(r.assignedPositions),
          textOrDash(r.weeklyWorkloadSummary),
        ])
      );
    });
  }

  function renderBalanceTable(tbody, rows) {
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!rows || !rows.length) {
      var tr0 = document.createElement("tr");
      var td0 = document.createElement("td");
      td0.colSpan = 3;
      td0.textContent = "No TA accounts yet.";
      tr0.appendChild(td0);
      tbody.appendChild(tr0);
      return;
    }
    rows.forEach(function (r) {
      tbody.appendChild(
        rowCells([
          textOrDash(r.taName),
          r.acceptedJobCount != null ? r.acceptedJobCount : 0,
          textOrDash(r.recommendation),
        ])
      );
    });
  }

  function load() {
    var summaryEl = document.getElementById("admin-workload-summary");
    var workloadBody = document.getElementById("admin-workload-body");
    var balanceBody = document.getElementById("admin-balance-body");
    if (!window.taApi || !window.taApi.adminWorkload) return;

    window.taApi
      .adminWorkload()
      .then(function (data) {
        renderSummary(summaryEl, data && data.summary ? data.summary : null);
        var rows = data && data.rows ? data.rows : [];
        renderWorkloadTable(workloadBody, rows);
        renderBalanceTable(balanceBody, rows);
      })
      .catch(function (err) {
        var msg = err && err.message ? err.message : "error";
        if (summaryEl) summaryEl.textContent = "Failed to load: " + msg;
        [workloadBody, balanceBody].forEach(function (tbody) {
          if (!tbody) return;
          tbody.innerHTML = "";
          var trE = document.createElement("tr");
          var tdE = document.createElement("td");
          tdE.colSpan = tbody.id === "admin-balance-body" ? 3 : 8;
          tdE.textContent = "Failed to load: " + msg;
          trE.appendChild(tdE);
          tbody.appendChild(trE);
        });
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
    if (refreshBtn) {
      refreshBtn.addEventListener("click", function () {
        load();
      });
    }

    load();

    document.querySelectorAll('[data-admin-tab="workload"], [data-admin-tab="balance"]').forEach(function (btn) {
      btn.addEventListener("click", function () {
        load();
      });
    });
  });
})();
