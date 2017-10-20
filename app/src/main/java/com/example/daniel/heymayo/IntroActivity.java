package com.example.daniel.heymayo;

import android.Manifest;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by kipta on 10/13/2017.
 */


public class IntroActivity extends AppIntro {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //default slides from AppIntro
        addSlide(SampleSlide.newInstance(R.layout.slide_1));
        addSlide(SampleSlide.newInstance(R.layout.slide_2));
        addSlide(SampleSlide.newInstance(R.layout.slide_3));
        addSlide(SampleSlide.newInstance(R.layout.slide_4));

        //you can control skip button with this. true or false method
        showSkipButton(false);
        //false hides the box outline at bottom for button area
        showSeparator(false);
        //false boolean hides page progress bar of dots that would display on bottom
        showPagerIndicator(false);



        //you can ask for location permission during slide, turn this off if we choose not too
        //not working at the moment so i commented it out
       //askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
    }


    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
