package com.example.web.dto;

/**
 * 统一的API响应格式
 * 用于标准化所有API端点的返回值
 */
public class ApiResponse<T> {
    /** 状态码：0表示成功，其他值表示错误 */
    public int code;
    
    /** 响应消息 */
    public String message;
    
    /** 实际响应数据 */
    public T data;
    
    /** 响应时间戳 */
    public long timestamp;
    
    // 私有构造函数，通过builder模式创建
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建成功响应
     * @param data 返回的数据
     * @return 成功的ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }
    
    /**
     * 创建成功响应（无数据）
     * @return 成功的ApiResponse
     */
    public static ApiResponse<Object> success() {
        return new ApiResponse<>(0, "success", null);
    }
    
    /**
     * 创建成功响应（自定义消息）
     * @param message 自定义消息
     * @param data 返回的数据
     * @return 成功的ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }
    
    /**
     * 创建错误响应
     * @param code 错误码（非0）
     * @param message 错误消息
     * @return 错误的ApiResponse
     */
    public static ApiResponse<Object> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 创建错误响应（使用预定义的错误码）
     * @param errorType 错误类型枚举
     * @param message 错误消息
     * @return 错误的ApiResponse
     */
    public static ApiResponse<Object> error(ErrorCode errorType, String message) {
        return new ApiResponse<>(errorType.code, message, null);
    }
    
    /**
     * 错误码定义
     */
    public enum ErrorCode {
        BAD_REQUEST(400, "请求参数错误"),
        UNAUTHORIZED(401, "未授权或登录过期"),
        FORBIDDEN(403, "无权限访问"),
        NOT_FOUND(404, "资源不存在"),
        CONFLICT(409, "数据冲突"),
        INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
        INVALID_INPUT(1001, "输入验证失败"),
        DATABASE_ERROR(1002, "数据库操作失败"),
        BUSINESS_LOGIC_ERROR(1003, "业务逻辑错误");
        
        public final int code;
        public final String desc;
        
        ErrorCode(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
