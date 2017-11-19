package com.example.daniel.heymayo;

import android.content.Intent;
import android.content.SharedPreferences;

import android.media.audiofx.PresetReverb;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * Created by kipta on 10/14/2017.
 */

public class MainActivity extends AppCompatActivity {
    final String firstStart = "firstStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //set true boolean is first time app is ran after download

        //boolean isFirstStart = mPrefs.getBoolean(firstStart, true);
        boolean isFirstStart = mPrefs.getBoolean(firstStart, true);

        //isFirstStart run tutorial
        if (isFirstStart) {
            Intent runTutorial = new Intent(this, IntroActivity.class);
            startActivity(runTutorial);

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(firstStart, false);
            editor.apply();
        }

        //not first start go to display map with location

        if (!isFirstStart) {
            Intent intent = new Intent(this, SignInActivity.class);//MapsActivity.class);
            startActivity(intent);
        }

    }

    // deprecated, do not use (except for testing)

    //public void mapActivity(View view) {
    //    Intent intent = new Intent(this, MapsActivity.class);
    //    startActivity(intent);
    //}

    //public void postActivity(View view) {
    //    Intent intent = new Intent(this, SignInActivity.class);
    //    startActivity(intent);
    //}
}
