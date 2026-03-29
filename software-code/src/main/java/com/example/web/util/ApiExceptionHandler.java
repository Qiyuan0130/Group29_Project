package com.example.web.util;

import com.example.web.dto.ApiResponse.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API异常处理工具
 * 将各类异常映射为合适的HTTP响应
 */
public final class ApiExceptionHandler {
    
    private ApiExceptionHandler() {
    }
    
    /**
     * 处理异常并返回错误响应
     * @param ex 异常
     * @param resp HTTP响应
     */
    public static void handle(Exception ex, HttpServletResponse resp) throws java.io.IOException {
        if (ex instanceof SecurityException) {
            handleSecurityException((SecurityException) ex, resp);
        } else if (ex instanceof IllegalArgumentException) {
            handleIllegalArgumentException((IllegalArgumentException) ex, resp);
        } else if (ex instanceof IllegalStateException) {
            handleIllegalStateException((IllegalStateException) ex, resp);
        } else {
            handleGenericException(ex, resp);
        }
    }
    
    /**
     * 处理用户认证和授权相关异常
     */
    private static void handleSecurityException(SecurityException ex, HttpServletResponse resp) throws java.io.IOException {
        String message = ex.getMessage();
        ApiLogger.warn("Security Exception: %s", message);
        
        if (message != null && message.contains("Not logged in")) {
            HttpApiResponse.unauthorized(resp, "请先登录");
        } else if (message != null && message.contains("权限不足")) {
            HttpApiResponse.forbidden(resp, "您没有权限执行此操作");
        } else {
            HttpApiResponse.forbidden(resp, message != null ? message : "访问被拒绝");
        }
    }
    
    /**
     * 处理参数验证异常
     */
    private static void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse resp) throws java.io.IOException {
        String message = ex.getMessage();
        ApiLogger.warn("Illegal Argument Exception: %s", message);
        HttpApiResponse.badRequest(resp, message != null ? message : "请求参数无效");
    }
    
    /**
     * 处理业务状态异常
     */
    private static void handleIllegalStateException(IllegalStateException ex, HttpServletResponse resp) throws java.io.IOException {
        String message = ex.getMessage();
        ApiLogger.warn("Illegal State Exception: %s", message);
        HttpApiResponse.error(resp, ErrorCode.BUSINESS_LOGIC_ERROR, message != null ? message : "操作状态不正确");
    }
    
    /**
     * 处理通用异常
     */
    private static void handleGenericException(Exception ex, HttpServletResponse resp) throws java.io.IOException {
        ApiLogger.error("Unexpected Exception", ex);
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "服务器发生错误，请稍后再试";
        }
        HttpApiResponse.internalServerError(resp, message);
    }
    
    /**
     * 验证参数不为空
     */
    public static void requireNonNull(Object value, String paramName) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }
    
    /**
     * 验证字符串不为空
     */
    public static void requireNonEmpty(String value, String paramName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }
    
    /**
     * 验证用户权限
     */
    public static void requireRole(String userRole, String... requiredRoles) throws SecurityException {
        if (userRole == null) {
            throw new SecurityException("Not logged in");
        }
        for (String role : requiredRoles) {
            if (userRole.equals(role)) {
                return;
            }
        }
        throw new SecurityException("权限不足");
    }
}
