/**
 * Admin TA Profiles — columns: Login, Name, Major, Technical Ability, Contact (no QM Number).
 */
(function () {
  "use strict";

  function loadAdminTaProfiles() {
    var tbody = document.getElementById("admin-ta-profiles-body");
    if (!tbody || !window.taApi || !window.taApi.taProfiles) return;

    window.taApi
      .taProfiles()
      .then(function (res) {
        var list = res && res.profiles ? res.profiles : [];
        tbody.innerHTML = "";
        if (!list.length) {
          tbody.innerHTML = '<tr><td colspan="5">No TA profiles yet.</td></tr>';
          return;
        }
        list.forEach(function (p) {
          var tr = document.createElement("tr");
          [p.username, p.name, p.major, p.technicalAbility, p.contact].forEach(function (val) {
            var td = document.createElement("td");
            td.textContent = val == null || String(val).trim() === "" ? "—" : String(val);
            tr.appendChild(td);
          });
          tbody.appendChild(tr);
        });
      })
      .catch(function (err) {
        tbody.innerHTML =
          '<tr><td colspan="5">Failed to load: ' +
          (err && err.message ? err.message : "error") +
          "</td></tr>";
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    loadAdminTaProfiles();
    document.querySelectorAll('[data-admin-tab="ta-profiles"]').forEach(function (btn) {
      btn.addEventListener("click", loadAdminTaProfiles);
    });
  });
})();
