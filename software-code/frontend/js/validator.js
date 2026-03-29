/**
 * 前端表单验证工具
 * 提供各种常用的表单字段验证规则
 */
(function (global) {
  "use strict";

  var FormValidator = {
    /**
     * 验证规则库
     */
    rules: {
      /**
       * 检查是否为空
       * @param value 值
       * @param fieldName 字段名称
       * @return {String} 错误消息或null
       */
      required: function (value, fieldName) {
        if (value === null || value === undefined || (typeof value === "string" && value.trim() === "")) {
          return fieldName + "不能为空";
        }
        return null;
      },

      /**
       * 检查邮箱格式
       */
      email: function (value, fieldName) {
        if (!value) return null;
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
          return fieldName + "格式不正确";
        }
        return null;
      },

      /**
       * 检查最小长度
       */
      minLength: function (value, fieldName, minLen) {
        if (!value || value.length < minLen) {
          return fieldName + "长度不能少于" + minLen + "个字符";
        }
        return null;
      },

      /**
       * 检查最大长度
       */
      maxLength: function (value, fieldName, maxLen) {
        if (value && value.length > maxLen) {
          return fieldName + "长度不能超过" + maxLen + "个字符";
        }
        return null;
      },

      /**
       * 检查密码强度
       * 至少9个字符，包含数字和字母
       */
      passwordStrength: function (value, fieldName) {
        if (!value) return null;
        if (value.length < 9) {
          return "密码长度不能少于9个字符";
        }
        if (!/\d/.test(value)) {
          return "密码必须包含数字";
        }
        if (!/[a-zA-Z]/.test(value)) {
          return "密码必须包含字母";
        }
        return null;
      },

      /**
       * 检查是否为有效数字
       */
      number: function (value, fieldName) {
        if (!value) return null;
        if (isNaN(value) || value.trim() === "") {
          return fieldName + "必须是数字";
        }
        return null;
      },

      /**
       * 检查是否为正整数
       */
      positiveInteger: function (value, fieldName) {
        if (!value) return null;
        var num = parseInt(value, 10);
        if (isNaN(num) || num <= 0 || num.toString() !== value.trim()) {
          return fieldName + "必须是正整数";
        }
        return null;
      },

      /**
       * 检查字符串是否仅包含字母和数字
       */
      alphanumeric: function (value, fieldName) {
        if (!value) return null;
        if (!/^[a-zA-Z0-9]+$/.test(value)) {
          return fieldName + "仅能包含字母和数字";
        }
        return null;
      },

      /**
       * 检查是否匹配某个正则表达式
       */
      pattern: function (value, fieldName, pattern) {
        if (!value) return null;
        if (!pattern.test(value)) {
          return fieldName + "格式不正确";
        }
        return null;
      },

      /**
       * 检查是否为有效的QM号
       * 格式: 6位数字或8位数字 (如: 231220645 或 230654)
       */
      qmNumber: function (value, fieldName) {
        if (!value) return null;
        if (!/^\d{6}$|^\d{8}$|^\d{9}$/.test(value.trim())) {
          return "QM号格式不正确（应为6-9位数字）";
        }
        return null;
      },

      /**
       * 检查是否为有效的电话号码
       */
      phoneNumber: function (value, fieldName) {
        if (!value) return null;
        // 支持多种格式：1001234567, +861001234567, (100)1234567, 100-1234-5678
        if (!/^[\d\-\(\)\+]{10,}$/.test(value.trim())) {
          return "电话号码格式不正确";
        }
        return null;
      },

      /**
       * 检查两个字段是否相等
       */
      match: function (value1, value2, fieldName1, fieldName2) {
        if (value1 !== value2) {
          return fieldName1 + "和" + fieldName2 + "不一致";
        }
        return null;
      },

      /**
       * 检查日期格式 (YYYY-MM-DD)
       */
      date: function (value, fieldName) {
        if (!value) return null;
        if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
          return fieldName + "格式应为 YYYY-MM-DD";
        }
        try {
          var d = new Date(value);
          if (isNaN(d.getTime())) {
            return fieldName + "不是有效日期";
          }
        } catch (e) {
          return fieldName + "不是有效日期";
        }
        return null;
      },

      /**
       * 检查时间范围
       */
      timeRange: function (value, fieldName, min, max) {
        if (!value) return null;
        var num = parseFloat(value);
        if (isNaN(num) || num < min || num > max) {
          return fieldName + "必须在" + min + "到" + max + "之间";
        }
        return null;
      },
    },

    /**
     * 验证单个字段
     * @param value 字段值
     * @param fieldName 字段名称
     * @param rulesArray 规则数组，例如: [['required'], ['email'], ['minLength', 5]]
     * @return {String} 第一个错误消息或null
     */
    validateField: function (value, fieldName, rulesArray) {
      if (!rulesArray || !Array.isArray(rulesArray)) {
        return null;
      }

      for (var i = 0; i < rulesArray.length; i++) {
        var rule = rulesArray[i];
        if (!Array.isArray(rule) || rule.length === 0) continue;

        var ruleName = rule[0];
        var ruleFunc = this.rules[ruleName];
        if (!ruleFunc) continue;

        var args = [value, fieldName].concat(rule.slice(1));
        var error = ruleFunc.apply(this, args);
        if (error) {
          return error;
        }
      }
      return null;
    },

    /**
     * 验证整个表单
     * @param formElement Form DOM元素或表单数据对象
     * @param validationRules 验证规则对象，例如:
     *   {
     *     'email': [[['required'], ['email']]],
     *     'password': [[['required'], ['passwordStrength']]],
     *   }
     * @return {Object} {valid: bool, errors: {fieldName: errorMessage}}
     */
    validateForm: function (formElement, validationRules) {
      var errors = {};
      var valid = true;

      // 从Form DOM元素获取数据或直接使用对象
      var formData;
      if (formElement instanceof HTMLFormElement) {
        var fd = new FormData(formElement);
        formData = {};
        for (var pair of fd.entries()) {
          formData[pair[0]] = pair[1];
        }
      } else {
        formData = formElement || {};
      }

      // 验证每个字段
      for (var fieldName in validationRules) {
        if (validationRules.hasOwnProperty(fieldName)) {
          var rules = validationRules[fieldName];
          var value = formData[fieldName] || "";
          var error = this.validateField(value, fieldName, rules);
          if (error) {
            errors[fieldName] = error;
            valid = false;
          }
        }
      }

      return {
        valid: valid,
        errors: errors,
      };
    },

    /**
     * 显示验证错误到页面
     * @param errors 错误对象 {fieldName: errorMessage}
     * @param containerSelector 容器选择器（CSS selector）
     */
    displayErrors: function (errors, containerSelector) {
      var container = document.querySelector(containerSelector);
      if (!container) return;

      // 清除现有的错误样式
      var inputs = container.querySelectorAll("input, textarea, select");
      inputs.forEach(function (input) {
        input.classList.remove("is-invalid");
      });

      var errorDiv = container.querySelector(".form-errors");
      if (errorDiv) {
        errorDiv.remove();
      }

      // 显示新的错误
      if (Object.keys(errors).length === 0) {
        return;
      }

      var errorHtml = '<div class="form-errors alert alert-danger">\\n';
      for (var fieldName in errors) {
        if (errors.hasOwnProperty(fieldName)) {
          errorHtml += '<div class="error-item">' + errors[fieldName] + "</div>\\n";

          // 为对应的输入框添加错误样式
          var input = container.querySelector('[name="' + fieldName + '"]');
          if (input) {
            input.classList.add("is-invalid");
          }
        }
      }
      errorHtml += "</div>";

      container.insertAdjacentHTML("afterbegin", errorHtml);
    },

    /**
     * 清除验证错误样式
     * @param containerSelector 容器选择器
     */
    clearErrors: function (containerSelector) {
      var container = document.querySelector(containerSelector);
      if (!container) return;

      var inputs = container.querySelectorAll("input, textarea, select");
      inputs.forEach(function (input) {
        input.classList.remove("is-invalid");
      });

      var errorDiv = container.querySelector(".form-errors");
      if (errorDiv) {
        errorDiv.remove();
      }
    },
  };

  // 导出
  global.FormValidator = FormValidator;
})(typeof window !== "undefined" ? window : this);
