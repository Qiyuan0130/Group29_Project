/**
 * Shared scripts: login/register navigation, TA/MO tabs, dropzone UI.
 */
(function () {
  "use strict";

  window.taRecruitment = {
    /** Shown when Jobs is locked or API rejects job access until profile is complete. */
    taProfileIncompleteToast:
      "Please complete Name, Major, Education Background, Technical Ability, and Contact in Profile, then save.",

    taCvRequiredToast:
      "Please upload at least one PDF resume in CV Upload before viewing or applying for jobs.",

    /** Same rule as server: all five fields non-empty (trimmed). */
    isTaProfileCompleteForJobs: function (user) {
      if (!user) return false;
      function ne(s) {
        return s != null && String(s).trim().length > 0;
      }
      return (
        ne(user.name) &&
        ne(user.major) &&
        ne(user.educationBackground) &&
        ne(user.technicalAbility) &&
        ne(user.contact)
      );
    },

    isTaCvReadyForJobs: function (cvFiles) {
      return Array.isArray(cvFiles) && cvFiles.length > 0;
    },

    isTaReadyForJobs: function (user, cvFiles) {
      return this.isTaProfileCompleteForJobs(user) && this.isTaCvReadyForJobs(cvFiles);
    },

    taJobsLockToast: function (user, cvFiles) {
      if (!this.isTaProfileCompleteForJobs(user)) {
        return this.taProfileIncompleteToast;
      }
      if (!this.isTaCvReadyForJobs(cvFiles)) {
        return this.taCvRequiredToast;
      }
      return "";
    },

    /** TA / MO dashboard tab switching */
    bindTabs: function (navSelector, panelPrefix) {
      var links = document.querySelectorAll(navSelector);
      links.forEach(function (link) {
        link.addEventListener("click", function (e) {
          e.preventDefault();
          if (link.classList.contains("ta-nav-link--locked")) {
            if (window.taRecruitment && window.taRecruitment.showToast) {
              var msg =
                (link.title && String(link.title).trim()) ||
                window.taRecruitment.taProfileIncompleteToast;
              window.taRecruitment.showToast(msg);
            }
            return;
          }
          var tab = link.getAttribute("data-tab");
          if (!tab) return;

          document.querySelectorAll(navSelector).forEach(function (l) {
            l.classList.toggle("is-active", l === link);
          });
          document.querySelectorAll('[id^="' + panelPrefix + '"]').forEach(function (panel) {
            panel.classList.add("hidden");
          });
          var target = document.getElementById(panelPrefix + tab);
          if (target) target.classList.remove("hidden");
        });
      });
    },

    /** MO top section tabs */
    bindMoSectionTabs: function () {
      var tabs = document.querySelectorAll("[data-mo-section]");
      if (!tabs.length) return;

      function applySection(sec) {
        tabs.forEach(function (b) {
          b.classList.toggle("is-active", b.getAttribute("data-mo-section") === sec);
        });
        document.querySelectorAll("[data-mo-panel]").forEach(function (p) {
          p.classList.toggle("hidden", p.getAttribute("data-mo-panel") !== sec);
        });
        if (sec === "applications" && typeof window.loadMoSelectApplicationsData === "function") {
          window.loadMoSelectApplicationsData();
        }
      }

      tabs.forEach(function (btn) {
        btn.addEventListener("click", function (e) {
          e.preventDefault();
          var sec = btn.getAttribute("data-mo-section");
          if (!sec) return;
          applySection(sec);
        });
      });

      var params = new URLSearchParams(location.search);
      var wanted = params.get("section");
      var valid = false;
      if (wanted) {
        tabs.forEach(function (btn) {
          if (btn.getAttribute("data-mo-section") === wanted) valid = true;
        });
      }
      if (valid) {
        applySection(wanted);
        return;
      }

      var active = null;
      tabs.forEach(function (btn) {
        if (!active && btn.classList.contains("is-active")) {
          active = btn.getAttribute("data-mo-section");
        }
      });
      if (!active) {
        active = tabs[0].getAttribute("data-mo-section");
      }
      applySection(active);
    },

    /** Admin sub-panels */
    bindAdminTabs: function () {
      var tabs = document.querySelectorAll("[data-admin-tab]");
      tabs.forEach(function (btn) {
        btn.addEventListener("click", function () {
          var t = btn.getAttribute("data-admin-tab");
          tabs.forEach(function (b) {
            b.classList.toggle("is-active", b === btn);
          });
          document.querySelectorAll("[data-admin-panel]").forEach(function (p) {
            p.classList.toggle("hidden", p.getAttribute("data-admin-panel") !== t);
          });
        });
      });
    },

    /** TA Jobs: filter chips (demo only) */
    bindJobChips: function () {
      var chips = document.querySelectorAll(".filter-chips .chip");
      chips.forEach(function (chip) {
        chip.addEventListener("click", function () {
          chips.forEach(function (c) {
            c.classList.remove("is-active");
          });
          chip.classList.add("is-active");
        });
      });
    },

    initDropzone: function () {
      var zone = document.getElementById("cv-dropzone");
      var input = document.getElementById("cv-file-input");
      if (!zone || !input) return;

      zone.addEventListener("click", function () {
        input.click();
      });

      ["dragenter", "dragover", "dragleave", "drop"].forEach(function (ev) {
        zone.addEventListener(ev, function (e) {
          e.preventDefault();
          e.stopPropagation();
        });
      });
      ["dragenter", "dragover"].forEach(function (ev) {
        zone.addEventListener(ev, function () {
          zone.classList.add("is-dragover");
        });
      });
      ["dragleave", "drop"].forEach(function (ev) {
        zone.addEventListener(ev, function () {
          zone.classList.remove("is-dragover");
        });
      });
      zone.addEventListener("drop", function (e) {
        var files = e.dataTransfer.files;
        if (files.length) {
          window.taRecruitment.showToast("File selected (demo): " + files[0].name);
        }
      });
      input.addEventListener("change", function () {
        if (input.files.length) {
          window.taRecruitment.showToast("File selected (demo): " + input.files[0].name);
        }
      });
    },

    /**
     * Card-style toast (bottom-right). Optional second arg: { variant, duration }.
     * variant: 'info' | 'success' | 'warning' | 'error'. If omitted, inferred from message.
     */
    showToast: function (msg, opts) {
      opts = opts || {};
      var icons = {
        info:
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>',
        success:
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>',
        warning:
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><path d="M12 9v4M12 17h.01"/></svg>',
        error:
          '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/></svg>',
      };

      function inferVariant(text) {
        var s = String(text || "").toLowerCase();
        if (
          /submitted|saved|uploaded|deleted|profile saved|analysis complete|successfully|^\s*ok\b/.test(s) &&
          !/failed|error/.test(s)
        ) {
          return "success";
        }
        if (
          /failed|error|required|invalid|missing|not logged|sign in again|could not|403|401|forbidden|denied|unable|upload a pdf|please upload|cv upload|cvid/.test(
            s
          )
        ) {
          return "warning";
        }
        return "info";
      }

      var variant = opts.variant;
      if (!variant || !icons[variant]) {
        variant = inferVariant(msg);
      }

      var duration = opts.duration;
      if (duration == null) {
        var len = String(msg || "").length;
        duration = Math.min(9500, Math.max(3000, 2400 + len * 38));
      }

      var t = document.getElementById("toast");
      if (!t) {
        t = document.createElement("div");
        t.id = "toast";
        t.setAttribute("role", "status");
        t.setAttribute("aria-live", "polite");
        document.body.appendChild(t);
      } else {
        t.removeAttribute("style");
      }

      t.className = "toast toast--" + variant;
      t.innerHTML =
        '<span class="toast__icon" aria-hidden="true">' +
        (icons[variant] || icons.info) +
        '</span><span class="toast__msg"></span>';
      var msgEl = t.querySelector(".toast__msg");
      if (msgEl) msgEl.textContent = msg;

      clearTimeout(t._hideOut);
      t.classList.remove("toast--in");
      requestAnimationFrame(function () {
        requestAnimationFrame(function () {
          t.classList.add("toast--in");
        });
      });

      t._hideOut = setTimeout(function () {
        t.classList.remove("toast--in");
      }, duration);
    },
  };

  document.addEventListener("DOMContentLoaded", function () {
    window.taRecruitment.initDropzone();
    window.taRecruitment.bindTabs(".ta-nav-link", "ta-panel-");
    window.taRecruitment.bindMoSectionTabs();
    window.taRecruitment.bindAdminTabs();
    window.taRecruitment.bindJobChips();
  });
})();
