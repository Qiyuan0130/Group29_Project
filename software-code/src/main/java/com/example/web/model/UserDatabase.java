package com.example.web.model;

import java.util.ArrayList;
import java.util.List;

/** Root JSON document for {@code users.json}. */
public class UserDatabase {

    public long nextUserId = 1;
    public List<User> users = new ArrayList<>();
}
