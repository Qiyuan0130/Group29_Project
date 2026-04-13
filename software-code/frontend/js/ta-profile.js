/**
 * TA dashboard: load current user profile into the existing Profile panel and save via PUT /api/profile.
 */
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    var panel = document.getElementById("ta-panel-profile");
    if (!panel) return;

    var grid = panel.querySelector(".profile-grid");
    if (!grid) return;

    var cards = grid.querySelectorAll(".profile-card");
    var displayCard = cards.length ? cards[0] : null;
    var editCard = cards.length > 1 ? cards[1] : null;

    var dds = displayCard ? displayCard.querySelectorAll(".kv-list dd") : [];
    var form = editCard ? editCard.querySelector("form") : null;
    var ctrls = editCard ? editCard.querySelectorAll(".form-control") : [];

    function applyToView(user) {
      if (!user) return;
      var vals = [
        user.name || "—",
        user.qmNumber || "—",
        user.major || "—",
        user.educationBackground || user.major || "—",
        user.technicalAbility || "—",
        user.contact || "—",
      ];
      for (var i = 0; i < dds.length && i < vals.length; i++) {
        dds[i].textContent = vals[i];
      }
    }

    function applyToForm(user) {
      if (!user || !ctrls.length) return;
      ctrls[0].value = user.name != null ? user.name : "";
      if (ctrls[1]) ctrls[1].value = user.qmNumber != null ? user.qmNumber : "";
      if (ctrls[2]) ctrls[2].value = user.major != null ? user.major : "";
      if (ctrls[3]) ctrls[3].value = user.educationBackground != null ? user.educationBackground : (user.major != null ? user.major : "");
      if (ctrls[4]) ctrls[4].value = user.technicalAbility != null ? user.technicalAbility : "";
      if (ctrls[5]) ctrls[5].value = user.contact != null ? user.contact : "";
    }

    function isProfileCompleteForJobs(user) {
      return (
        window.taRecruitment &&
        typeof window.taRecruitment.isTaProfileCompleteForJobs === "function" &&
        window.taRecruitment.isTaProfileCompleteForJobs(user)
      );
    }

    function updateTaJobsTabLock(user) {
      var nav = document.querySelector('.ta-nav-link[data-tab="jobs"]');
      if (!nav) return;
      var ok = isProfileCompleteForJobs(user);
      nav.classList.toggle("ta-nav-link--locked", !ok);
      nav.setAttribute("aria-disabled", ok ? "false" : "true");
      nav.title = ok ? "" : window.taRecruitment.taProfileIncompleteToast;
    }

    function syncTaDashboardJobsHash(user) {
      if (!document.getElementById("ta-panel-jobs")) return;
      if (location.hash !== "#jobs") return;
      if (isProfileCompleteForJobs(user)) {
        var jobsTab = document.querySelector('.ta-nav-link[data-tab="jobs"]');
        if (jobsTab) jobsTab.click();
      } else {
        if (history.replaceState) {
          history.replaceState(null, "", "#profile");
        } else {
          location.hash = "#profile";
        }
        var profileTab = document.querySelector('.ta-nav-link[data-tab="profile"]');
        if (profileTab) profileTab.click();
        if (window.taRecruitment && window.taRecruitment.showToast) {
          window.taRecruitment.showToast(window.taRecruitment.taProfileIncompleteToast);
        }
      }
    }

    window.taApi
      .me()
      .then(function (user) {
        applyToView(user);
        applyToForm(user);
        updateTaJobsTabLock(user);
        syncTaDashboardJobsHash(user);
      })
      .catch(function (err) {
        if (window.taRecruitment && window.taRecruitment.showToast) {
          window.taRecruitment.showToast("Could not load profile: " + (err && err.message ? err.message : "error"));
        }
      });

    if (form) {
      form.addEventListener("submit", function (e) {
        e.preventDefault();
        var body = {
          name: ctrls[0] ? ctrls[0].value : "",
          qmNumber: ctrls[1] ? ctrls[1].value : "",
          major: ctrls[2] ? ctrls[2].value : "",
          educationBackground: ctrls[3] ? ctrls[3].value : "",
          technicalAbility: ctrls[4] ? ctrls[4].value : "",
          contact: ctrls[5] ? ctrls[5].value : "",
        };
        window.taApi
          .profileUpdate(body)
          .then(function (user) {
            applyToView(user);
            applyToForm(user);
            updateTaJobsTabLock(user);
            window.taRecruitment.showToast("Profile saved");
          })
          .catch(function (err) {
            window.taRecruitment.showToast("Save failed: " + (err && err.message ? err.message : "error"));
          });
      });
    }
  });
})();
