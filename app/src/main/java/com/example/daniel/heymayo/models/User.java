package com.example.daniel.heymayo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String tokenID;
    public String uid;

    public User() {}

    public User(String userId, String tokenID) {
        this.tokenID = tokenID;
        this.uid = userId;
    }

}
