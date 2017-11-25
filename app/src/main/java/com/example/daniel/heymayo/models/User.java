package com.example.daniel.heymayo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String tokenID;
    public String uid;
    public int karmaPoints = 0;

    public User() {}

    public User(String userId, String tokenID) {
        this.tokenID = tokenID;
        this.uid = userId;
    }

    public User(String userId, String tokenID, int points) {
        this.tokenID = tokenID;
        this.uid = userId;
        this.karmaPoints = points;
    }

}
