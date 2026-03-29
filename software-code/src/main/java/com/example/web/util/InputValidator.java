package com.example.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 输入验证工具
 * 提供各种数据验证方法，用于后端业务逻辑验证
 */
public final class InputValidator {
    
    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[\\d\\-\\(\\)\\+]{10,}$"
    );
    
    private static final Pattern QM_NUMBER_PATTERN = Pattern.compile(
        "^\\d{6}$|^\\d{8}$|^\\d{9}$"
    );
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    );
    
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]+$"
    );
    
    private InputValidator() {
    }
    
    /**
     * 验证器建造者类
     * 支持链式验证
     */
    public static class Validator {
        private Map<String, String> errors = new HashMap<>();
        
        /**
         * 验证字符串不为空
         */
        public Validator notEmpty(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                addError(fieldName, fieldName + "不能为空");
            }
            return this;
        }
        
        /**
         * 验证字符串长度
         */
        public Validator length(String value, String fieldName, int min, int max) {
            if (value == null) {
                return this;
            }
            int len = value.length();
            if (len < min || len > max) {
                addError(fieldName, fieldName + "长度必须在" + min + "到" + max + "之间");
            }
            return this;
        }
        
        /**
         * 验证邮箱格式
         */
        public Validator isEmail(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                addError(fieldName, fieldName + "格式不正确");
            }
            return this;
        }
        
        /**
         * 验证电话号码格式
         */
        public Validator isPhoneNumber(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!PHONE_PATTERN.matcher(value).matches()) {
                addError(fieldName, fieldName + "格式不正确");
            }
            return this;
        }
        
        /**
         * 验证QM号格式
         */
        public Validator isQmNumber(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!QM_NUMBER_PATTERN.matcher(value.trim()).matches()) {
                addError(fieldName, "QM号格式不正确（应为6-9位数字）");
            }
            return this;
        }
        
        /**
         * 验证密码强度
         * 至少9个字符，包含数字和字母
         */
        public Validator isStrongPassword(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (value.length() < 9) {
                addError(fieldName, "密码长度不能少于9个字符");
            } else if (!value.matches(".*\\d.*")) {
                addError(fieldName, "密码必须包含数字");
            } else if (!value.matches(".*[a-zA-Z].*")) {
                addError(fieldName, "密码必须包含字母");
            }
            return this;
        }
        
        /**
         * 验证数字范围
         */
        public Validator numberRange(Number value, String fieldName, double min, double max) {
            if (value == null) {
                return this;
            }
            double num = value.doubleValue();
            if (num < min || num > max) {
                addError(fieldName, fieldName + "必须在" + min + "到" + max + "之间");
            }
            return this;
        }
        
        /**
         * 验证正整数
         */
        public Validator isPositiveInteger(Number value, String fieldName) {
            if (value == null) {
                return this;
            }
            long num = value.longValue();
            if (num <= 0) {
                addError(fieldName, fieldName + "必须是正整数");
            }
            return this;
        }
        
        /**
         * 验证仅包含字母和数字
         */
        public Validator isAlphanumeric(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!ALPHANUMERIC_PATTERN.matcher(value).matches()) {
                addError(fieldName, fieldName + "仅能包含字母和数字");
            }
            return this;
        }
        
        /**
         * 验证安全的文件名
         */
        public Validator isSafeFilename(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!SAFE_FILENAME_PATTERN.matcher(value).matches()) {
                addError(fieldName, fieldName + "包含非法字符");
            }
            return this;
        }
        
        /**
         * 验证正则表达式
         */
        public Validator matches(String value, String fieldName, Pattern pattern) {
            if (value == null || value.trim().isEmpty()) {
                return this;
            }
            if (!pattern.matcher(value).matches()) {
                addError(fieldName, fieldName + "格式不正确");
            }
            return this;
        }
        
        /**
         * 验证两个值是否相等
         */
        public Validator equals(String value1, String value2, String fieldName) {
            if (value1 == null || !value1.equals(value2)) {
                addError(fieldName, fieldName + "不匹配");
            }
            return this;
        }
        
        /**
         * 自定义验证
         */
        public Validator custom(boolean isValid, String fieldName, String errorMessage) {
            if (!isValid) {
                addError(fieldName, errorMessage);
            }
            return this;
        }
        
        /**
         * 检查是否有错误
         */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        /**
         * 获取所有错误
         */
        public Map<String, String> getErrors() {
            return new HashMap<>(errors);
        }
        
        /**
         * 获取第一个错误消息
         */
        public String getFirstError() {
            if (errors.isEmpty()) {
                return null;
            }
            return errors.values().iterator().next();
        }
        
        /**
         * 获取特定字段的错误
         */
        public String getError(String fieldName) {
            return errors.get(fieldName);
        }
        
        /**
         * 如果有错误则抛出异常
         */
        public void throwIfHasErrors() throws IllegalArgumentException {
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(getFirstError());
            }
        }
        
        /**
         * 添加错误消息
         */
        private void addError(String fieldName, String message) {
            // 只保存第一个错误
            if (!errors.containsKey(fieldName)) {
                errors.put(fieldName, message);
            }
        }
    }
    
    /**
     * 创建新的验证器实例
     */
    public static Validator create() {
        return new Validator();
    }
    
    // 静态便利方法
    
    /**
     * 验证字符串不为空
     */
    public static void requireNotEmpty(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
    
    /**
     * 验证对象不为空
     */
    public static void requireNonNull(Object value, String fieldName) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
    
    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 验证QM号格式
     */
    public static boolean isValidQmNumber(String qmNumber) {
        return qmNumber != null && QM_NUMBER_PATTERN.matcher(qmNumber.trim()).matches();
    }
    
    /**
     * 验证电话号码格式
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * 清理用户输入（防止XSS）
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
    
    /**
     * 截断字符串到指定长度
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
    
    /**
     * 规范化空白字符
     */
    public static String normalizeWhitespace(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
