package com.example.daniel.heymayo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String tokenID;

    public User() {}

    public User(String username, String email, String tokenID) {
        this.username = username;
        this.email = email;
        this.tokenID = tokenID;
    }
}