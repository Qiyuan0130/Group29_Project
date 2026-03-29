package com.example.web.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单的API日志工具
 * 用于记录和追踪API请求和错误
 */
public final class ApiLogger {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String LOG_PREFIX = "[%s] [%-5s]";
    
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL
    }
    
    private ApiLogger() {
    }
    
    /**
     * 记录DEBUG级别日志
     */
    public static void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }
    
    /**
     * 记录INFO级别日志
     */
    public static void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }
    
    /**
     * 记录WARN级别日志
     */
    public static void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }
    
    /**
     * 记录ERROR级别日志
     */
    public static void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }
    
    /**
     * 记录ERROR并附带异常堆栈
     */
    public static void error(String message, Throwable ex, Object... args) {
        log(LogLevel.ERROR, message, args);
        if (ex != null) {
            logException(ex);
        }
    }
    
    /**
     * 记录FATAL级别日志
     */
    public static void fatal(String message, Object... args) {
        log(LogLevel.FATAL, message, args);
    }
    
    /**
     * 记录FATAL并附带异常堆栈
     */
    public static void fatal(String message, Throwable ex, Object... args) {
        log(LogLevel.FATAL, message, args);
        if (ex != null) {
            logException(ex);
        }
    }
    
    /**
     * 记录API请求（进入）
     */
    public static void logApiIn(String path, String method, String clientIp) {
        info("API_IN [%s %s] from %s", method, path, clientIp);
    }
    
    /**
     * 记录API请求（离开）
     */
    public static void logApiOut(String path, String method, int statusCode, long elapsedMs) {
        info("API_OUT [%s %s] status=%d time=%dms", method, path, statusCode, elapsedMs);
    }
    
    /**
     * 记录业务操作
     */
    public static void logOperation(String operation, String details) {
        info("OPERATION [%s] %s", operation, details);
    }
    
    /**
     * 记录数据访问
     */
    public static void logDataAccess(String entity, String action, Object id) {
        debug("DATA_ACCESS [%s] %s id=%s", entity, action, id);
    }
    
    /**
     * 通用日志记录方法
     */
    private static void log(LogLevel level, String message, Object... args) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String prefix = String.format(LOG_PREFIX, timestamp, level.name());
        String formattedMessage = args.length > 0 ? String.format(message, args) : message;
        System.out.println(prefix + " " + formattedMessage);
    }
    
    /**
     * 记录异常堆栈
     */
    private static void logException(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        System.err.println(sw.toString());
    }
    
    /**
     * 从HttpServletRequest获取客户端IP
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
