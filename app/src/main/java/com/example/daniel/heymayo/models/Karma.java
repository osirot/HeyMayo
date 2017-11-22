package com.example.daniel.heymayo.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jsayler on 11/19/17.
 */

@IgnoreExtraProperties
public class Karma {

    public Boolean karma;
    public int points = 0;

    public Karma() {}

    public Karma(Boolean karma, int points) {
        this.karma = karma;
        this.points = points;
    }

    public void pointCounter() {
        this.points++;
    }

    public int getPoints() {
        return this.points;
    }

    public void storeInDb() {

    }

    public static void logPost (String text) {
        Log.d("KARMA POINTS:",text);
    }
}
