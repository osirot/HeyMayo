package com.example.daniel.heymayo.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AnonUser {

    public String tokenID;
    public String uid;

    public AnonUser() {}

    public AnonUser(String userId, String tokenID) {
        this.tokenID = tokenID;
        this.uid = userId;
    }

}
