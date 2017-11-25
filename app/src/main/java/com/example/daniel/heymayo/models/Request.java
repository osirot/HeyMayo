package com.example.daniel.heymayo.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Request {

    public String uid;
    public String body;
    public long timestamp;

    public Request() {}

    public Request(String body, String uid) {
        this.uid = uid;
        this.body = body;
    }

    public Request(String body, String uid, long timestamp) {
        this.uid = uid;
        this.body = body;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("body", body);
        result.put("timestamp", ServerValue.TIMESTAMP);

        return result;
    }
}

