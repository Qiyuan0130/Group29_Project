/**
 * MO: LLM workload balance via POST /api/mo/workload/llm-balance
 */
(function () {
  "use strict";

  function textOrDash(v) {
    if (v == null || String(v).trim() === "") return "—";
    return String(v);
  }

  function renderTable(tbody, assessments) {
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!assessments || !assessments.length) {
      var tr0 = document.createElement("tr");
      var td0 = document.createElement("td");
      td0.colSpan = 5;
      td0.textContent = "No assessments returned.";
      tr0.appendChild(td0);
      tbody.appendChild(tr0);
      return;
    }
    assessments.forEach(function (a) {
      var tr = document.createElement("tr");
      var reasonable = a.reasonable === true ? "Yes" : a.reasonable === false ? "No" : "—";
      [a.taName, (a.weeklyHoursTotal != null ? a.weeklyHoursTotal + " h/wk" : "—"), reasonable, a.reason, a.adjustment].forEach(
        function (t) {
          var td = document.createElement("td");
          td.textContent = textOrDash(t);
          tr.appendChild(td);
        }
      );
      tbody.appendChild(tr);
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var btn = document.getElementById("mo-llm-balance-btn");
    if (!btn || !window.taApi || !window.taApi.moWorkloadLlmBalance) return;

    btn.addEventListener("click", function () {
      var statusEl = document.getElementById("mo-llm-status");
      var teamEl = document.getElementById("mo-llm-team-advice");
      var tbody = document.getElementById("mo-balance-llm-body");
      btn.disabled = true;
      if (statusEl) statusEl.textContent = "Calling LLM… please wait.";
      if (teamEl) teamEl.textContent = "";

      window.taApi
        .moWorkloadLlmBalance()
        .then(function (data) {
          if (statusEl) statusEl.textContent = "Model: " + (data.model || "—");
          if (teamEl) teamEl.textContent = data.teamAdjustment || "";
          renderTable(tbody, data.assessments || []);
        })
        .catch(function (err) {
          if (statusEl) statusEl.textContent = "Error: " + (err && err.message ? err.message : "failed");
          renderTable(tbody, []);
        })
        .finally(function () {
          btn.disabled = false;
        });
    });
  });
})();
