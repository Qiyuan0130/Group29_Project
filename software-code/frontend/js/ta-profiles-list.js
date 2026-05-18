/**
 * Admin: load TA directory from GET /api/ta-profiles (no QM Number column).
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

  function loadAdminTaProfiles() {
    var tbody = document.getElementById("admin-ta-profiles-body");
    if (!tbody) return;

    window.taApi
      .taProfiles()
      .then(function (res) {
        var list = res && res.profiles ? res.profiles : [];
        tbody.innerHTML = "";
        if (!list.length) {
          var tr0 = document.createElement("tr");
          var td0 = document.createElement("td");
          td0.colSpan = 5;
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
        tdE.colSpan = 5;
        tdE.textContent = "Failed to load: " + (err && err.message ? err.message : "error");
        trE.appendChild(tdE);
        tbody.appendChild(trE);
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    loadAdminTaProfiles();
    document.querySelectorAll('[data-admin-tab="ta-profiles"]').forEach(function (btn) {
      btn.addEventListener("click", loadAdminTaProfiles);
    });
  });
})();
