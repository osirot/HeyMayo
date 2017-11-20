package com.example.daniel.heymayo.models;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jsayler on 11/19/17.
 */

@IgnoreExtraProperties
public class Karma {

    public Boolean karmaPt;

    public Karma() {}

    public Karma(Boolean karmaPt) {
        this.karmaPt = karmaPt;

    }

    public void storeInDb() {

    }

    public static void logPost (String text) {
        Log.d("KARMA POINTS:",text);
    }
}
