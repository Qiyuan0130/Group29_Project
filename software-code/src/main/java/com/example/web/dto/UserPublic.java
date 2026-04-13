package com.example.web.dto;

public class UserPublic {

    public Long id;
    public String username;
    public String role;
    public String buptNumber;
    public String qmNumber;
    public String name;
    public String major;
    public String educationBackground;
    public String technicalAbility;
    public String contact;

    public static UserPublic from(com.example.web.model.User u) {
        UserPublic p = new UserPublic();
        String mergedBuptNumber = u.qmNumber != null && !u.qmNumber.trim().isEmpty()
                ? u.qmNumber
                : u.buptNumber;
        String technical = u.technicalAbility;
        if (technical != null
                && !technical.trim().isEmpty()
                && u.username != null
                && technical.trim().equalsIgnoreCase(u.username.trim())) {
            technical = "";
        }
        p.id = u.id;
        p.username = u.username;
        p.role = u.role;
        p.buptNumber = mergedBuptNumber;
        p.qmNumber = mergedBuptNumber;
        p.name = u.name;
        p.major = u.major;
        p.educationBackground = u.educationBackground;
        p.technicalAbility = technical;
        p.contact = u.contact;
        return p;
    }
}
