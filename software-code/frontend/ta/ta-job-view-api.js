/**
 * TA job browse / detail — API client only for these pages (does not modify shared api.js).
 */
(function (global) {
  "use strict";

  function resolveApiBase() {
    var path = location.pathname || "";
    if (/^\/[^/]+\/software-code\//.test(path)) {
      var seg = path.split("/");
      return "/" + seg[1] + "/software-code/api/";
    }
    if (/^\/[^/]+\//.test(path)) {
      var ctx = path.split("/")[1];
      return "/" + ctx + "/api/";
    }
    return "api/";
  }

  function apiUrl(path) {
    var p = path.replace(/^\//, "");
    return resolveApiBase() + p;
  }

  function request(method, path, bodyObj) {
    var opts = {
      method: method,
      credentials: "same-origin",
      headers: {},
    };
    if (bodyObj !== undefined && bodyObj !== null) {
      opts.headers["Content-Type"] = "application/json;charset=UTF-8";
      opts.body = JSON.stringify(bodyObj);
    }
    return fetch(apiUrl(path), opts).then(function (res) {
      var ct = res.headers.get("Content-Type") || "";
      var isJson = ct.indexOf("application/json") !== -1;
      return (isJson ? res.json() : res.text()).then(function (data) {
        if (!res.ok) {
          var msg = data && data.error ? data.error : res.statusText;
          throw new Error(msg || "Request failed");
        }
        return data;
      });
    });
  }

  global.taJobViewApi = {
    url: apiUrl,
    jobsList: function () {
      return request("GET", "jobs", null);
    },
    applicationsApply: function (jobId) {
      return request("POST", "applications", { jobId: jobId });
    },
    applicationsMe: function () {
      return request("GET", "applications/me", null);
    },
    logout: function () {
      return request("POST", "auth/logout", {});
    },
  };
})(typeof window !== "undefined" ? window : this);
