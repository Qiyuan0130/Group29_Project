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
    var nameInput = editCard ? editCard.querySelector("#ta-profile-name") : null;
    var buptNumberInput = editCard ? editCard.querySelector("#ta-profile-bupt-number") : null;
    var majorInput = editCard ? editCard.querySelector("#ta-profile-major") : null;
    var educationInput = editCard ? editCard.querySelector("#ta-profile-education-background") : null;
    var technicalInput = editCard ? editCard.querySelector("#ta-profile-technical-ability") : null;
    var contactInput = editCard ? editCard.querySelector("#ta-profile-contact") : null;

    function applyToView(user) {
      if (!user) return;
      var buptNumber = user.buptNumber || user.qmNumber || "—";
      var vals = [
        user.name || "—",
        buptNumber,
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
      if (!user) return;
      var buptNumber = user.buptNumber != null ? user.buptNumber : user.qmNumber;
      if (nameInput) nameInput.value = user.name != null ? user.name : "";
      if (buptNumberInput) buptNumberInput.value = buptNumber != null ? buptNumber : "";
      if (majorInput) majorInput.value = user.major != null ? user.major : "";
      if (educationInput) educationInput.value = user.educationBackground != null ? user.educationBackground : (user.major != null ? user.major : "");
      if (technicalInput) technicalInput.value = user.technicalAbility != null ? user.technicalAbility : "";
      if (contactInput) contactInput.value = user.contact != null ? user.contact : "";
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
          name: nameInput ? nameInput.value : "",
          qmNumber: buptNumberInput ? buptNumberInput.value : "",
          major: majorInput ? majorInput.value : "",
          educationBackground: educationInput ? educationInput.value : "",
          technicalAbility: technicalInput ? technicalInput.value : "",
          contact: contactInput ? contactInput.value : "",
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
