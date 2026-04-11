package com.example.web.model;

/**
 * User entity (Gson-deserialized; fields align with JSON).
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
