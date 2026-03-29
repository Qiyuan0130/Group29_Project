/**
 * Admin / MO: load TA directory from GET /api/ta-profiles into a table body by id.
 */
(function () {
  "use strict";

  function rowWithCells(texts) {
    var tr = document.createElement("tr");
    texts.forEach(function (t) {
      var td = document.createElement("td");
      td.textContent = t == null ? "" : String(t);
      tr.appendChild(td);
    });
    return tr;
  }

  function loadInto(tbodyId) {
    var tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    window.taApi
      .taProfiles()
      .then(function (res) {
        var list = res && res.profiles ? res.profiles : [];
        tbody.innerHTML = "";
        if (!list.length) {
          var tr0 = document.createElement("tr");
          var td0 = document.createElement("td");
          td0.colSpan = 6;
          td0.textContent = "No TA profiles yet.";
          tr0.appendChild(td0);
          tbody.appendChild(tr0);
          return;
        }
        list.forEach(function (p) {
          tbody.appendChild(
            rowWithCells([
              p.username || "",
              p.name || "",
              p.qmNumber || "",
              p.major || "",
              p.technicalAbility || "",
              p.contact || "",
            ])
          );
        });
      })
      .catch(function (err) {
        tbody.innerHTML = "";
        var trE = document.createElement("tr");
        var tdE = document.createElement("td");
        tdE.colSpan = 6;
        tdE.textContent = "Failed to load: " + (err && err.message ? err.message : "error");
        trE.appendChild(tdE);
        tbody.appendChild(trE);
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    loadInto("admin-ta-profiles-body");
    loadInto("mo-ta-profiles-body");

    document.querySelectorAll('[data-admin-tab="ta-profiles"]').forEach(function (btn) {
      btn.addEventListener("click", function () {
        loadInto("admin-ta-profiles-body");
      });
    });
    document.querySelectorAll('[data-mo-section="ta-profiles"]').forEach(function (btn) {
      btn.addEventListener("click", function () {
        loadInto("mo-ta-profiles-body");
      });
    });
  });
})();
