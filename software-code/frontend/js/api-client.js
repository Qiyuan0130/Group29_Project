/**
 * 改进的API客户端
 * 支持超时、重试、请求拦截等功能
 */
(function (global) {
  "use strict";

  var ApiClient = {
    /**
     * 配置
     */
    config: {
      timeout: 30000, // 默认30秒超时
      retryCount: 2, // 默认重试2次
      retryDelay: 1000, // 重试延迟1秒
      enableLogging: true, // 是否启用日志
    },

    /**
     * 请求拦截器队列
     */
    requestInterceptors: [],

    /**
     * 响应拦截器队列
     */
    responseInterceptors: [],

    /**
     * 错误拦截器队列
     */
    errorInterceptors: [],

    /**
     * 添加请求拦截器
     * @param interceptor 拦截器函数，参数为(config)，返回config或Promise<config>
     */
    addRequestInterceptor: function (interceptor) {
      this.requestInterceptors.push(interceptor);
    },

    /**
     * 添加响应拦截器
     * @param interceptor 拦截器函数，参数为(response)，返回response或Promise<response>
     */
    addResponseInterceptor: function (interceptor) {
      this.responseInterceptors.push(interceptor);
    },

    /**
     * 添加错误拦截器
     * @param interceptor 拦截器函数，参数为(error)，可以处理或重新抛出错误
     */
    addErrorInterceptor: function (interceptor) {
      this.errorInterceptors.push(interceptor);
    },

    /**
     * 执行请求拦截器
     */
    executeRequestInterceptors: function (config) {
      var self = this;
      var promise = Promise.resolve(config);
      this.requestInterceptors.forEach(function (interceptor) {
        promise = promise.then(interceptor);
      });
      return promise;
    },

    /**
     * 执行响应拦截器
     */
    executeResponseInterceptors: function (response) {
      var self = this;
      var promise = Promise.resolve(response);
      this.responseInterceptors.forEach(function (interceptor) {
        promise = promise.then(interceptor);
      });
      return promise;
    },

    /**
     * 执行错误拦截器
     */
    executeErrorInterceptors: function (error) {
      var self = this;
      for (var i = 0; i < this.errorInterceptors.length; i++) {
        try {
          this.errorInterceptors[i](error);
        } catch (e) {
          // 继续执行其他拦截器
        }
      }
    },

    /**
     * 发送HTTP请求（核心方法，支持超时和重试）
     */
    request: function (method, path, data, options) {
      var self = this;
      options = options || {};

      var timeout = options.timeout || this.config.timeout;
      var retryCount = options.retryCount !== undefined ? options.retryCount : this.config.retryCount;

      var config = {
        method: method,
        path: path,
        data: data,
        timeout: timeout,
        retryCount: retryCount,
      };

      return this.executeRequestInterceptors(config).then(function (finalConfig) {
        return self._requestWithRetry(finalConfig, 0);
      });
    },

    /**
     * 带重试的请求
     */
    _requestWithRetry: function (config, attemptNumber) {
      var self = this;

      return this._requestWithTimeout(config).then(
        function (response) {
          return self.executeResponseInterceptors(response);
        },
        function (error) {
          self.executeErrorInterceptors(error);

          if (attemptNumber < config.retryCount) {
            self._log("Retry attempt " + (attemptNumber + 1) + "/" + config.retryCount);
            return new Promise(function (resolve, reject) {
              setTimeout(function () {
                resolve(self._requestWithRetry(config, attemptNumber + 1));
              }, self.config.retryDelay * (attemptNumber + 1));
            });
          } else {
            return Promise.reject(error);
          }
        }
      );
    },

    /**
     * 带超时的请求
     */
    _requestWithTimeout: function (config) {
      var self = this;
      var opts = {
        method: config.method,
        credentials: "same-origin",
        headers: {},
      };

      if (config.data !== undefined && config.data !== null) {
        opts.headers["Content-Type"] = "application/json;charset=UTF-8";
        opts.body = JSON.stringify(config.data);
      }

      var fetchPromise = fetch(self._resolveUrl(config.path), opts).then(function (res) {
        var ct = res.headers.get("Content-Type") || "";
        var isJson = ct.indexOf("application/json") !== -1;
        return (isJson ? res.json() : res.text()).then(function (data) {
          var response = {
            status: res.status,
            statusText: res.statusText,
            data: data,
            headers: res.headers,
            ok: res.ok,
          };

          // 检查业务状态码
          if (typeof data === "object" && data.code !== undefined) {
            response.businessCode = data.code;
            response.businessMessage = data.message;
          }

          if (!res.ok) {
            var error = new Error(response.businessMessage || data.error || res.statusText);
            error.response = response;
            throw error;
          }

          return response;
        });
      });

      // 添加超时处理
      return Promise.race([
        fetchPromise,
        new Promise(function (resolve, reject) {
          setTimeout(function () {
            reject(new Error("Request timeout after " + config.timeout + "ms"));
          }, config.timeout);
        }),
      ]);
    },

    /**
     * GET请求
     */
    get: function (path, options) {
      return this.request("GET", path, null, options);
    },

    /**
     * POST请求
     */
    post: function (path, data, options) {
      return this.request("POST", path, data, options);
    },

    /**
     * PUT请求
     */
    put: function (path, data, options) {
      return this.request("PUT", path, data, options);
    },

    /**
     * DELETE请求
     */
    delete: function (path, options) {
      return this.request("DELETE", path, null, options);
    },

    /**
     * 解析URL
     */
    _resolveUrl: function (path) {
      var apiBase = this._resolveApiBase();
      var p = path.replace(/^\//, "");
      return apiBase + p;
    },

    /**
     * 获取API基础URL
     */
    _resolveApiBase: function () {
      var pathStr = location.pathname || "";
      // Example: /Group29_Project/software-code/frontend/register.html
      if (/^\/[^/]+\/software-code\//.test(pathStr)) {
        var seg = pathStr.split("/");
        return "/" + seg[1] + "/software-code/api/";
      }
      // Example: /java-web-json/register.html or /java-web-json/ta/dashboard.html
      if (/^\/[^/]+\//.test(pathStr)) {
        var ctx = pathStr.split("/")[1];
        return "/" + ctx + "/api/";
      }
      // Last-resort fallback
      return "api/";
    },

    /**
     * 日志
     */
    _log: function (message) {
      if (this.config.enableLogging && console && console.log) {
        console.log("[ApiClient] " + message);
      }
    },

    /**
     * 获取URL
     */
    getUrl: function (path) {
      return this._resolveUrl(path);
    },

    /**
     * 设置超时时间
     */
    setTimeout: function (timeout) {
      this.config.timeout = timeout;
      return this;
    },

    /**
     * 设置重试次数
     */
    setRetryCount: function (count) {
      this.config.retryCount = count;
      return this;
    },

    /**
     * 启用或禁用日志
     */
    setLogging: function (enable) {
      this.config.enableLogging = enable;
      return this;
    },
  };

  /**
   * 旧版API客户端，保持向后兼容
   */
  var LegacyApiClient = {
    url: function (path) {
      return ApiClient.getUrl(path);
    },

    login: function (login, password) {
      return ApiClient.post("auth/login", { login: login, password: password }).then(function (res) {
        return res.data;
      });
    },

    register: function (name, email, password, role, moKey) {
      return ApiClient.post("auth/register", {
        name: name,
        email: email,
        password: password,
        role: role,
        moKey: moKey || "",
      }).then(function (res) {
        return res.data;
      });
    },

    logout: function () {
      return ApiClient.post("auth/logout", {}).then(function (res) {
        return res.data;
      });
    },

    me: function () {
      return ApiClient.get("auth/me").then(function (res) {
        return res.data;
      });
    },

    cvUpload: function (file) {
      var fd = new FormData();
      fd.append("file", file);
      var opts = {
        method: "POST",
        credentials: "same-origin",
        body: fd,
      };
      return fetch(ApiClient.getUrl("cv/upload"), opts).then(function (res) {
        return res.json().then(function (data) {
          if (!res.ok) {
            var msg = data && data.error ? data.error : res.statusText;
            throw new Error(msg || "Upload failed");
          }
          return data;
        });
      });
    },

    cvList: function () {
      return ApiClient.get("cv/list").then(function (res) {
        return res.data;
      });
    },

    cvDelete: function (id) {
      return ApiClient.delete("cv/" + id).then(function (res) {
        return res.data;
      });
    },

    cvViewUrl: function (id) {
      return ApiClient.getUrl("cv/" + id + "/view");
    },

    jobsList: function () {
      return ApiClient.get("jobs").then(function (res) {
        return res.data;
      });
    },

    jobsCreate: function (job) {
      return ApiClient.post("jobs", job || {}).then(function (res) {
        return res.data;
      });
    },

    jobsUpdate: function (id, job) {
      return ApiClient.put("jobs/" + id, job || {}).then(function (res) {
        return res.data;
      });
    },

    applicationsMe: function () {
      return ApiClient.get("applications/me").then(function (res) {
        return res.data;
      });
    },
  };

  // 导出新API客户端
  global.ApiClient = ApiClient;

  // 保持向后兼容，导出旧的taApi
  global.taApi = LegacyApiClient;
})(typeof window !== "undefined" ? window : this);
