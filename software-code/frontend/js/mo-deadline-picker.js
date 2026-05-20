/**
 * MO job deadline: English flatpickr calendar (local bundle), min = today.
 * Dates before today cannot be selected.
 */
(function (global) {
  "use strict";

  var MONTH_NAMES = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];

  var EN_LOCALE = {
    firstDayOfWeek: 0,
    weekdays: {
      shorthand: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
      longhand: [
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
      ],
    },
    months: {
      shorthand: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
      longhand: MONTH_NAMES,
    },
    rangeSeparator: " to ",
    weekAbbreviation: "Wk",
    scrollTitle: "Scroll to increment",
    toggleTitle: "Click to toggle",
    amPM: ["AM", "PM"],
    yearAriaLabel: "Year",
    monthAriaLabel: "Month",
    hourAriaLabel: "Hour",
    minuteAriaLabel: "Minute",
    time_24hr: false,
  };

  function pad2(n) {
    return String(n).padStart(2, "0");
  }

  function todayYmd() {
    var d = new Date();
    return d.getFullYear() + "-" + pad2(d.getMonth() + 1) + "-" + pad2(d.getDate());
  }

  function startOfToday() {
    var d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }

  function ymdToDate(ymd) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(ymd || "")) return null;
    var p = ymd.split("-");
    var dt = new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
    return Number.isNaN(dt.getTime()) ? null : dt;
  }

  function daysInMonth(year, month1to12) {
    return new Date(year, month1to12, 0).getDate();
  }

  function initEnglishSelectFallback(el) {
    var min = todayYmd();
    var minParts = min.split("-");
    var minYear = parseInt(minParts[0], 10);
    var minMonth = parseInt(minParts[1], 10);
    var minDay = parseInt(minParts[2], 10);

    el.type = "hidden";
    el.setAttribute("aria-hidden", "true");

    var container = document.createElement("div");
    container.className = "mo-deadline-english";
    container.setAttribute("role", "group");
    container.setAttribute("aria-label", "Application deadline");
    container.style.cssText = "display:flex;flex-wrap:wrap;gap:0.75rem;align-items:flex-end";

    function buildField(labelText, suffix) {
      var field = document.createElement("div");
      field.style.minWidth = suffix === "year" ? "7rem" : "9.5rem";
      var label = document.createElement("label");
      label.textContent = labelText;
      label.setAttribute("for", el.id + "-" + suffix);
      label.style.cssText = "display:block;font-size:0.85rem;margin-bottom:0.25rem;color:var(--muted,#666)";
      var sel = document.createElement("select");
      sel.id = el.id + "-" + suffix;
      sel.className = "form-control";
      sel.setAttribute("aria-label", labelText);
      field.appendChild(label);
      field.appendChild(sel);
      container.appendChild(field);
      return sel;
    }

    var monthSel = buildField("Month", "month");
    var daySel = buildField("Day", "day");
    var yearSel = buildField("Year", "year");

    MONTH_NAMES.forEach(function (name, i) {
      var opt = document.createElement("option");
      opt.value = String(i + 1);
      opt.textContent = name;
      monthSel.appendChild(opt);
    });

    var startYear = new Date().getFullYear();
    for (var y = startYear; y <= startYear + 5; y++) {
      var yOpt = document.createElement("option");
      yOpt.value = String(y);
      yOpt.textContent = String(y);
      yearSel.appendChild(yOpt);
    }

    function fillDays() {
      var y = parseInt(yearSel.value, 10);
      var m = parseInt(monthSel.value, 10);
      var maxDay = daysInMonth(y, m);
      var prev = parseInt(daySel.value, 10) || minDay;
      daySel.innerHTML = "";
      for (var d = 1; d <= maxDay; d++) {
        var dOpt = document.createElement("option");
        dOpt.value = String(d);
        dOpt.textContent = String(d);
        daySel.appendChild(dOpt);
      }
      daySel.value = String(Math.min(prev, maxDay));
    }

    function clampToMin() {
      var y = parseInt(yearSel.value, 10);
      var m = parseInt(monthSel.value, 10);
      var d = parseInt(daySel.value, 10);
      if (y < minYear) {
        yearSel.value = String(minYear);
        y = minYear;
      }
      if (y === minYear && m < minMonth) {
        monthSel.value = String(minMonth);
        m = minMonth;
      }
      fillDays();
      if (y === minYear && m === minMonth && d < minDay) {
        daySel.value = String(minDay);
      }
    }

    function syncToInput() {
      clampToMin();
      var y = parseInt(yearSel.value, 10);
      var m = parseInt(monthSel.value, 10);
      var d = parseInt(daySel.value, 10);
      el.value = y + "-" + pad2(m) + "-" + pad2(d);
    }

    monthSel.value = String(minMonth);
    yearSel.value = String(minYear);
    fillDays();
    daySel.value = String(minDay);
    syncToInput();

    monthSel.addEventListener("change", syncToInput);
    yearSel.addEventListener("change", syncToInput);
    daySel.addEventListener("change", syncToInput);

    el.parentNode.insertBefore(container, el.nextSibling);

    return {
      setDate: function (ymd) {
        var dt = ymdToDate(ymd);
        if (!dt || dt < startOfToday()) {
          monthSel.value = String(minMonth);
          yearSel.value = String(minYear);
          fillDays();
          daySel.value = String(minDay);
        } else {
          monthSel.value = String(dt.getMonth() + 1);
          yearSel.value = String(dt.getFullYear());
          fillDays();
          daySel.value = String(dt.getDate());
        }
        syncToInput();
      },
      destroy: function () {
        container.remove();
        el.type = "text";
        el.removeAttribute("aria-hidden");
      },
    };
  }

  /**
   * @param {string|HTMLElement} target
   * @param {object} [extra]
   */
  function initDeadlinePicker(target, extra) {
    var el = typeof target === "string" ? document.querySelector(target) : target;
    if (!el) return null;

    var min = todayYmd();
    el.setAttribute("placeholder", "Select date — e.g. 30 May 2026");

    if (global.flatpickr) {
      if (typeof global.flatpickr.localize === "function") {
        global.flatpickr.localize(EN_LOCALE);
      }
      var fp = global.flatpickr(el, Object.assign(
        {
          dateFormat: "Y-m-d",
          altInput: true,
          altFormat: "j F Y",
          altInputClass: "form-control",
          altInputPlaceholder: "Select date — e.g. 30 May 2026",
          locale: EN_LOCALE,
          allowInput: false,
          clickOpens: true,
          disableMobile: true,
          minDate: min,
          onChange: function (_selected, _str, inst) {
            if (inst.selectedDates.length && inst.selectedDates[0] < startOfToday()) {
              inst.setDate(min, true);
            }
          },
        },
        extra || {}
      ));
      return {
        setDate: function (ymd) {
          fp.setDate(ymd || "", true, "Y-m-d");
        },
        destroy: function () {
          fp.destroy();
        },
      };
    }

    var fallback = initEnglishSelectFallback(el);
    return {
      setDate: function (ymd) {
        fallback.setDate(ymd);
      },
      destroy: function () {
        fallback.destroy();
      },
    };
  }

  global.moDeadlinePicker = {
    EN_LOCALE: EN_LOCALE,
    todayYmd: todayYmd,
    initDeadlinePicker: initDeadlinePicker,
  };
})(window);
