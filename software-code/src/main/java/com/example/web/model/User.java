package com.example.web.model;

/**
 * 用户实体（Gson 反序列化，使用可空包装类型便于与 JSON 对齐）。
 */
public class User {

    public Long id;
    public String username;
    public String password;
    private String passwordHash;
    public String role;
    public String qmNumber;
    public String name;
    public String major;
    public String educationBackground;
    public String technicalAbility;
    public String contact;

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
