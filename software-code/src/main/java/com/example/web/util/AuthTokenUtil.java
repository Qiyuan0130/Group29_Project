package com.example.web.util;

import java.util.UUID;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 身份验证令牌工具
 * 用于生成和验证注册、登录等安全令牌
 */
public final class AuthTokenUtil {
    
    private AuthTokenUtil() {
    }
    
    /**
     * 生成唯一的认证令牌
     * 用于注册成功、密码重置等场景
     * @return 安全的令牌字符串
     */
    public static String generateAuthToken() {
        UUID uuid = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        String combined = uuid.toString() + timestamp;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(combined.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            // 如果SHA-256失败，返回Base64编码的简单令牌
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes());
        }
    }
    
    /**
     * 生成邮箱验证码
     * 6位数字
     */
    public static String generateEmailVerificationCode() {
        int code = (int) (Math.random() * 1000000);
        return String.format("%06d", code);
    }
    
    /**
     * 生成会话密钥
     * 用于临时验证身份
     */
    public static String generateSessionKey() {
        return generateAuthToken();
    }
    
    /**
     * 验证令牌格式（基本验证）
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // 令牌应该是Base64编码的SHA-256结果，长度通常为43
        return token.length() > 20 && token.length() < 100;
    }
}
