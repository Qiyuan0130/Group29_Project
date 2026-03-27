/**
 * Servlet API client (relative paths when same-origin).
 */
(function (global) {
  "use strict";

  function resolveApiBase() {
    var path = location.pathname || "";
    // Example: /Group29_Project/software-code/frontend/register.html
    if (/^\/[^/]+\/software-code\//.test(path)) {
      var seg = path.split("/");
      return "/" + seg[1] + "/software-code/api/";
    }
    // Example: /java-web-json/register.html or /java-web-json/ta/dashboard.html
    if (/^\/[^/]+\//.test(path)) {
      var ctx = path.split("/")[1];
      return "/" + ctx + "/api/";
    }
    // Last-resort fallback
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

  function requestFormData(method, path, formData) {
    var opts = {
      method: method,
      credentials: "same-origin",
      body: formData,
    };
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

  global.taApi = {
    url: apiUrl,
    login: function (login, password) {
      return request("POST", "auth/login", { login: login, password: password });
    },
    register: function (name, email, password, role, moKey) {
      return request("POST", "auth/register", {
        name: name,
        email: email,
        password: password,
        role: role,
        moKey: moKey || "",
      });
    },
    logout: function () {
      return request("POST", "auth/logout", {});
    },
    me: function () {
      return request("GET", "auth/me", null);
    },
    cvUpload: function (file) {
      var fd = new FormData();
      fd.append("file", file);
      return requestFormData("POST", "cv/upload", fd);
    },
    cvList: function () {
      return request("GET", "cv/list", null);
    },
    cvDelete: function (id) {
      return request("DELETE", "cv/" + id, null);
    },
    cvViewUrl: function (id) {
      return apiUrl("cv/" + id + "/view");
    },
    jobsList: function () {
      return request("GET", "jobs", null);
    },
    jobsCreate: function (job) {
      return request("POST", "jobs", job || {});
    },
    jobsUpdate: function (id, job) {
      return request("PUT", "jobs/" + id, job || {});
    },
  };
})(typeof window !== "undefined" ? window : this);
