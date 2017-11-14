package com.example.daniel.heymayo;

import android.content.Intent;
import android.content.SharedPreferences;

import android.media.audiofx.PresetReverb;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * Created by kipta on 10/14/2017.
 */

public class MainActivity extends BaseActivity {
    final String firstStart = "firstStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //set true boolean is first time app is ran after download
        //boolean isFirstStart = mPrefs.getBoolean(firstStart, true);
        boolean isFirstStart = mPrefs.getBoolean(getString(R.string.pref_first_start), TRUE);

        //isFirstStart run tutorial
        if (isFirstStart) {
            Intent runTutorial = new Intent(this, IntroActivity.class);
            startActivity(runTutorial);

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(getString(R.string.pref_first_start), FALSE);
            editor.apply();
        }

        //not first start go to display map with location

        if (!isFirstStart) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);


        }

    }


    public void mapActivity(View view) {

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }


    public void postActivity(View view) {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
