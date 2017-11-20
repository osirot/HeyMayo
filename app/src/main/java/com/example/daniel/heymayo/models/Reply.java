package com.example.daniel.heymayo.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jsayler on 11/5/17.
 */
@IgnoreExtraProperties
public class Reply {

    public String uid;
    public String body;
    public long timestamp;
    public Boolean points;

    public Reply() {}

    public Reply(String uid, String body, long timestamp) {
        this.uid = uid;
        this.body = body;
        this.timestamp = timestamp;
        this.points = false;
    }

    public Reply(String uid, String body, long timestamp, Boolean stars) {
        this.uid = uid;
        this.body = body;
        this.timestamp = timestamp;
        this.points = stars;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("body", body);
        result.put("timestamp", timestamp);
        result.put("points", points);

        return result;
    }
}
