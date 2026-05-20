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
      if (ctrls[1]) ctrls[1].value = user.major != null ? user.major : "";
      if (ctrls[2]) ctrls[2].value = user.educationBackground != null ? user.educationBackground : (user.major != null ? user.major : "");
      if (ctrls[3]) ctrls[3].value = user.technicalAbility != null ? user.technicalAbility : "";
      if (ctrls[4]) ctrls[4].value = user.contact != null ? user.contact : "";
    }

    function isProfileCompleteForJobs(user) {
      return (
        window.taRecruitment &&
        typeof window.taRecruitment.isTaProfileCompleteForJobs === "function" &&
        window.taRecruitment.isTaProfileCompleteForJobs(user)
      );
    }

    function preferredFallbackTab(user, hasCv) {
      if (!isProfileCompleteForJobs(user)) return "profile";
      if (!hasCv) return "cv";
      return "profile";
    }

    function activateDashboardTab(tabName) {
      var nav = document.querySelector('.ta-nav-link[data-tab="' + tabName + '"]');
      if (!nav || nav.classList.contains("ta-nav-link--locked")) return;
      nav.click();
    }

    function leaveJobsPanelIfLocked(user, hasCv) {
      var jobsPanel = document.getElementById("ta-panel-jobs");
      if (!jobsPanel || jobsPanel.classList.contains("hidden")) return;
      activateDashboardTab(preferredFallbackTab(user, hasCv));
    }

    function updateTaJobsTabLock(user, hasCv) {
      var nav = document.querySelector('.ta-nav-link[data-tab="jobs"]');
      if (!nav || !window.taRecruitment) return;
      var profileOk = isProfileCompleteForJobs(user);
      var cvOk = !!hasCv;
      var ok = profileOk && cvOk;
      nav.classList.toggle("ta-nav-link--locked", !ok);
      nav.setAttribute("aria-disabled", ok ? "false" : "true");
      nav.title = window.taRecruitment.taJobsLockToast(user, hasCv ? [{ id: 1 }] : []);
      if (!ok) {
        if (typeof window.taRecruitment.clearTaJobsList === "function") {
          window.taRecruitment.clearTaJobsList();
        }
        leaveJobsPanelIfLocked(user, hasCv);
      }
    }

    function applyDashboardHash(user, hasCv) {
      if (!document.getElementById("ta-panel-profile")) return;
      var hash = (location.hash || "").toLowerCase();
      if (hash === "#cv") {
        activateDashboardTab("cv");
        return;
      }
      if (hash === "#profile") {
        activateDashboardTab("profile");
        return;
      }
      if (hash === "#status") {
        activateDashboardTab("status");
        return;
      }
      if (hash === "#jobs") {
        var ready =
          window.taRecruitment &&
          typeof window.taRecruitment.isTaReadyForJobs === "function" &&
          window.taRecruitment.isTaReadyForJobs(user, hasCv ? [{ id: 1 }] : []);
        if (ready) {
          activateDashboardTab("jobs");
        } else {
          var fallback = preferredFallbackTab(user, hasCv);
          if (history.replaceState) {
            history.replaceState(null, "", "#" + fallback);
          } else {
            location.hash = fallback;
          }
          activateDashboardTab(fallback);
          if (window.taRecruitment && window.taRecruitment.showToast) {
            window.taRecruitment.showToast(
              window.taRecruitment.taJobsLockToast(user, hasCv ? [{ id: 1 }] : [])
            );
          }
        }
      }
    }

    window.taRecruitment.refreshTaJobsAccess = function () {
      if (!window.taApi || typeof window.taApi.me !== "function") {
        return Promise.resolve();
      }
      return window.taApi
        .me()
        .then(function (user) {
          applyToView(user);
          applyToForm(user);
          return window.taApi.cvList().catch(function () {
            return { files: [] };
          }).then(function (res) {
            var files = res && res.files ? res.files : [];
            var hasCv = files.length > 0;
            updateTaJobsTabLock(user, hasCv);
            applyDashboardHash(user, hasCv);
            return { user: user, hasCv: hasCv, files: files };
          });
        });
    };

    window.taRecruitment.refreshTaJobsAccess().catch(function (err) {
      if (window.taRecruitment && window.taRecruitment.showToast) {
        window.taRecruitment.showToast("Could not load profile: " + (err && err.message ? err.message : "error"));
      }
    });

    if (form) {
      form.addEventListener("submit", function (e) {
        e.preventDefault();
        var body = {
          name: ctrls[0] ? ctrls[0].value : "",
          major: ctrls[1] ? ctrls[1].value : "",
          educationBackground: ctrls[2] ? ctrls[2].value : "",
          technicalAbility: ctrls[3] ? ctrls[3].value : "",
          contact: ctrls[4] ? ctrls[4].value : "",
        };
        window.taApi
          .profileUpdate(body)
          .then(function (user) {
            applyToView(user);
            applyToForm(user);
            return window.taRecruitment.refreshTaJobsAccess();
          })
          .then(function () {
            window.taRecruitment.showToast("Profile saved");
          })
          .catch(function (err) {
            window.taRecruitment.showToast("Save failed: " + (err && err.message ? err.message : "error"));
          });
      });
    }
  });
})();
