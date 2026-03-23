package com.example.web.dto;

public class UserPublic {

    public Long id;
    public String username;
    public String role;
    public String qmNumber;
    public String name;
    public String major;
    public String technicalAbility;
    public String contact;

    public static UserPublic from(com.example.web.model.User u) {
        UserPublic p = new UserPublic();
        p.id = u.id;
        p.username = u.username;
        p.role = u.role;
        p.qmNumber = u.qmNumber;
        p.name = u.name;
        p.major = u.major;
        p.technicalAbility = u.technicalAbility;
        p.contact = u.contact;
        return p;
    }
}
