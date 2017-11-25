package com.example.daniel.heymayo.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

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
    public Boolean karma = false;

    public Reply() {}

    public Reply(String uid, String body) {
        this.uid = uid;
        this.body = body;
    }

    public Reply(String uid, String body, long timestamp) {
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
        result.put("karma", karma);

        return result;
    }

    @PropertyName("timestamp")
    public long getTimeStamp() {
        return this.timestamp;
    }

}
