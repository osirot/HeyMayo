package com.example.daniel.heymayo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.daniel.heymayo.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends BaseActivity {

    private static final String TAG = "AnonymousAuth";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            // signed in, moving on to next activity
            writeNewUser(user.getUid(), getToken());
            onAuthSuccess();
            Log.d(TAG, "onAuthStateChanged:signed in:" + user.getUid());
        } else {
            // new sign in, creating anonymous account
            signInAnonymously();
            Log.d(TAG, "onAuthStateChanged:not signed in; calling signInAnonymously()");
        }
    }

    private void onAuthSuccess() {
        startActivity(new Intent(SignInActivity.this, RequestPostActivity.class));
        Log.d(TAG, "Auth successful, starting RequestPostActivity");
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
                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
    }

    private void writeNewUser(String userId, String token) {
        User user = new User(userId, token);
        mDatabase.child("users").child(userId).setValue(user);
        Log.d(TAG, "Saving user ID " + userId + " to database");
    }
}
