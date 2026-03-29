package com.example.web.util;

import com.example.web.dto.ApiResponse;
import com.example.web.dto.ApiResponse.ErrorCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 改进的API响应工具
 * 使用统一的ApiResponse格式返回所有API结果
 */
public final class HttpApiResponse {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    
    private HttpApiResponse() {
    }
    
    /**
     * 返回成功响应
     * @param resp HttpServletResponse
     * @param data 响应数据
     */
    public static void success(HttpServletResponse resp, Object data) throws IOException {
        write(resp, HttpServletResponse.SC_OK, ApiResponse.success(data));
    }
    
    /**
     * 返回成功响应（无数据）
     * @param resp HttpServletResponse
     */
    public static void success(HttpServletResponse resp) throws IOException {
        write(resp, HttpServletResponse.SC_OK, ApiResponse.success());
    }
    
    /**
     * 返回成功响应（自定义消息）
     * @param resp HttpServletResponse
     * @param message 自定义消息
     * @param data 响应数据
     */
    public static void success(HttpServletResponse resp, String message, Object data) throws IOException {
        write(resp, HttpServletResponse.SC_OK, ApiResponse.success(message, data));
    }
    
    /**
     * 返回错误响应
     * @param resp HttpServletResponse
     * @param statusCode HTTP状态码
     * @param message 错误消息
     */
    public static void error(HttpServletResponse resp, int statusCode, String message) throws IOException {
        ApiResponse<Object> errorResponse = ApiResponse.error(statusCode, message);
        write(resp, statusCode, errorResponse);
    }
    
    /**
     * 返回错误响应（使用预定义的错误码）
     * @param resp HttpServletResponse
     * @param errorCode 错误码枚举
     * @param message 错误消息
     */
    public static void error(HttpServletResponse resp, ErrorCode errorCode, String message) throws IOException {
        ApiResponse<Object> errorResponse = ApiResponse.error(errorCode, message);
        write(resp, errorCode.code, errorResponse);
    }
    
    /**
     * 返回客户端错误
     * @param resp HttpServletResponse
     * @param message 错误消息
     */
    public static void badRequest(HttpServletResponse resp, String message) throws IOException {
        error(resp, ErrorCode.BAD_REQUEST, message);
    }
    
    /**
     * 返回未授权错误
     * @param resp HttpServletResponse
     * @param message 错误消息
     */
    public static void unauthorized(HttpServletResponse resp, String message) throws IOException {
        error(resp, ErrorCode.UNAUTHORIZED, message);
    }
    
    /**
     * 返回禁止访问错误
     * @param resp HttpServletResponse
     * @param message 错误消息
     */
    public static void forbidden(HttpServletResponse resp, String message) throws IOException {
        error(resp, ErrorCode.FORBIDDEN, message);
    }
    
    /**
     * 返回资源不存在错误
     * @param resp HttpServletResponse
     * @param message 错误消息
     */
    public static void notFound(HttpServletResponse resp, String message) throws IOException {
        error(resp, ErrorCode.NOT_FOUND, message);
    }
    
    /**
     * 返回服务器错误
     * @param resp HttpServletResponse
     * @param message 错误消息
     */
    public static void internalServerError(HttpServletResponse resp, String message) throws IOException {
        error(resp, ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
    
    /**
     * 写入响应
     * @param resp HttpServletResponse
     * @param statusCode HTTP状态码
     * @param body 响应体对象
     */
    private static void write(HttpServletResponse resp, int statusCode, Object body) throws IOException {
        resp.setStatus(statusCode);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        if (body != null) {
            resp.getWriter().write(GSON.toJson(body));
        }
    }
}
