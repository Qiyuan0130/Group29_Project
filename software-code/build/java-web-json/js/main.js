/**
 * 公共脚本：登录/注册跳转、TA/MO 标签切换、拖拽上传 UI（无后端）
 */
(function () {
  "use strict";

  window.taRecruitment = {
    /** TA / MO 仪表板内 tab 切换 */
    bindTabs: function (navSelector, panelPrefix) {
      var links = document.querySelectorAll(navSelector);
      links.forEach(function (link) {
        link.addEventListener("click", function (e) {
          e.preventDefault();
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

    /** MO 顶部子模块切换 */
    bindMoSectionTabs: function () {
      var tabs = document.querySelectorAll("[data-mo-section]");
      tabs.forEach(function (btn) {
        btn.addEventListener("click", function () {
          var sec = btn.getAttribute("data-mo-section");
          tabs.forEach(function (b) {
            b.classList.toggle("is-active", b === btn);
          });
          document.querySelectorAll("[data-mo-panel]").forEach(function (p) {
            p.classList.toggle("hidden", p.getAttribute("data-mo-panel") !== sec);
          });
        });
      });
    },

    /** Admin 子视图 */
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

    /** TA Jobs：筛选 chips 仅前端演示 */
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

    /**
     * AI Matching 页：底部单一按钮，一次性填充所有行的分数与说明（后端接入后可改为 fetch 结果）
     */
    initAiMatchingRunAll: function () {
      var btn = document.getElementById("ai-run-all-analysis");
      if (!btn) return;

      function fillRow(row) {
        var score = row.getAttribute("data-ai-score") || "—";
        var matched = row.getAttribute("data-ai-matched") || "—";
        var missing = row.getAttribute("data-ai-missing") || "—";
        var note = row.getAttribute("data-ai-note") || "";

        var elScore = row.querySelector(".js-ai-cell-score");
        var elMatched = row.querySelector(".js-ai-cell-matched");
        var elMissing = row.querySelector(".js-ai-cell-missing");
        var elNote = row.querySelector(".js-ai-cell-note");

        if (elScore) elScore.innerHTML = '<span class="score">' + score + "</span>";
        if (elMatched) {
          if (matched === "—") elMatched.textContent = "—";
          else elMatched.innerHTML = '<span class="skills-ok">' + matched + "</span>";
        }
        if (elMissing) {
          if (missing === "—") elMissing.textContent = "—";
          else elMissing.innerHTML = '<span class="skills-bad">' + missing + "</span>";
        }
        if (elNote) elNote.textContent = note;

        row.classList.add("is-analyzed");
      }

      btn.addEventListener("click", function () {
        document.querySelectorAll(".ai-matching-row").forEach(fillRow);
        btn.disabled = true;
        btn.textContent = "Analysis complete";
        window.taRecruitment.showToast("已为所有岗位/申请人生成分析");
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
          window.taRecruitment.showToast("已选择文件（演示）：" + files[0].name);
        }
      });
      input.addEventListener("change", function () {
        if (input.files.length) {
          window.taRecruitment.showToast("已选择文件（演示）：" + input.files[0].name);
        }
      });
    },

    showToast: function (msg) {
      var t = document.getElementById("toast");
      if (!t) {
        t = document.createElement("div");
        t.id = "toast";
        t.style.cssText =
          "position:fixed;bottom:1.5rem;left:50%;transform:translateX(-50%);background:#1a2332;color:#fff;padding:0.6rem 1rem;border-radius:8px;font-size:0.9rem;z-index:9999;opacity:0;transition:opacity .2s;";
        document.body.appendChild(t);
      }
      t.textContent = msg;
      t.style.opacity = "1";
      clearTimeout(t._hide);
      t._hide = setTimeout(function () {
        t.style.opacity = "0";
      }, 2500);
    },
  };

  document.addEventListener("DOMContentLoaded", function () {
    window.taRecruitment.initDropzone();
    window.taRecruitment.bindTabs(".ta-nav-link", "ta-panel-");
    window.taRecruitment.bindMoSectionTabs();
    window.taRecruitment.bindAdminTabs();
    window.taRecruitment.bindJobChips();
    window.taRecruitment.initAiMatchingRunAll();
  });
})();
