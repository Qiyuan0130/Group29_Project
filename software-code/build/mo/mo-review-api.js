/**
 * MO Review API - ADDED FILE
 * API functions for MO to review TA applications
 */
(function (global) {
  "use strict";

  function resolveApiBase() {
    if (global.taApi && typeof global.taApi.url === "function") {
      var sample = global.taApi.url("auth/me");
      return sample.replace(/auth\/me$/, "");
    }
    var seg = (location.pathname || "").split("/");
    var ctx = seg.length > 1 ? seg[1] : "java-web-json";
    return "/" + ctx + "/api/";
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
    return fetch(resolveApiBase() + path, opts).then(function (res) {
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

  // ADDED: MO Review API methods
  global.moReviewApi = {
    /**
     * Review an application (Accept or Reject)
     * @param {string} applicationId - The application ID
     * @param {string} decision - 'ACCEPT' or 'REJECT'
     * @param {string} note - Optional review note
     */
    reviewApplication: function (applicationId, decision, note) {
      return request("POST", "applications/" + applicationId + "/review", {
        decision: decision,
        note: note || "",
      });
    },

    /**
     * Get review history for an application
     * @param {string} applicationId - The application ID
     */
    getReviewHistory: function (applicationId) {
      return request("GET", "applications/" + applicationId + "/review-history", null);
    },

    /**
     * Batch review multiple applications
     * @param {Array} reviews - Array of {applicationId, decision, note}
     */
    batchReview: function (reviews) {
      return request("POST", "applications/batch-review", {
        reviews: reviews,
      });
    },
  };
})(typeof window !== "undefined" ? window : this);