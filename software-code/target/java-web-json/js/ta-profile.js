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

    window.taApi
      .me()
      .then(function (user) {
        applyToView(user);
        applyToForm(user);
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
            window.taRecruitment.showToast("Profile saved");
          })
          .catch(function (err) {
            window.taRecruitment.showToast("Save failed: " + (err && err.message ? err.message : "error"));
          });
      });
    }
  });
})();
