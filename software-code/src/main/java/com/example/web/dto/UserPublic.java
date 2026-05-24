package com.example.web.dto;

/**
 * User profile exposed via the API (password fields omitted).
 */
public class UserPublic {

    public Long id;
    public String username;
    public String role;
    public String qmNumber;
    public String name;
    public String major;
    public String educationBackground;
    public String technicalAbility;
    public String contact;

    /**
     * Builds a safe DTO from a domain {@link com.example.web.model.User}.
     *
     * @param u source user entity
     * @return public profile without password hash
     */
    public static UserPublic from(com.example.web.model.User u) {
        UserPublic p = new UserPublic();
        p.id = u.id;
        p.username = u.username;
        p.role = u.role;
        p.qmNumber = u.qmNumber;
        p.name = u.name;
        p.major = u.major;
        p.educationBackground = u.educationBackground;
        p.technicalAbility = u.technicalAbility;
        p.contact = u.contact;
        return p;
    }
}
