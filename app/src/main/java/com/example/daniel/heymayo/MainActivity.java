package com.example.daniel.heymayo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.daniel.heymayo.fragments.MapFragment;
import com.example.daniel.heymayo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by kipta on 10/14/2017.
 */

public class MainActivity extends AppCompatActivity {

    private final String firstStart = "firstStart";
    private static final String TAG = "***AnonymousAuth***";

    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //set true boolean is first time app is ran after download
        boolean isFirstStart = mPrefs.getBoolean(firstStart, true);
        //isFirstStart run tutorial
        if (isFirstStart) {
            Intent runTutorial = new Intent(this, IntroActivity.class);
            startActivity(runTutorial);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(firstStart, false);
            editor.apply();
        }
    }

    //Anonymous Login code => very sensitive; do not edit unless you know what you are doing!
    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            // signed in, moving on to next activity
            //writeNewUser(user.getUid(), getToken());
            onAuthSuccess();
            Log.d(TAG, "onAuthStateChanged:signed in:" + user.getUid());
        } else {
            // new sign in, creating anonymous account
            signInAnonymously();
            Log.d(TAG, "onAuthStateChanged:not signed in; calling signInAnonymously()");
        }
    }

    private void onAuthSuccess() {
        startMapsActivity();
        Log.d(TAG, "Auth successful, starting MapActivity");
        finish();
    }

    private void signInAnonymously() {
        showProgressDialog();
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    writeNewUser(user.getUid(), getToken());
                    onAuthSuccess();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });
    }

    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    private void writeNewUser(String userId, String token) {
        User user = new User(userId, token);
        mDatabase.child("users").child(userId).setValue(user);
        Log.d(TAG, "Saving user ID " + userId + " to database");
    }

    protected void startMapsActivity() {
        startActivity(new Intent(MainActivity.this, MapFragment.class));
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
