package com.example.daniel.heymayo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.heymayo.models.Request;
import com.example.daniel.heymayo.models.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {

    private static final String TAG = "RequestActivity";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;
    private GeoFire geoFire;
    private static final String GEO_FIRE_DB = "https://heymayo-test.firebaseio.com/";
    //private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/_geofire";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mBodyField = (EditText) findViewById(R.id.field_body);

        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_request);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
                mBodyField.getText().clear();
                finish();
            }
        });
    }

    private void submitPost() {
        final String body = mBodyField.getText().toString();
        final String userId = getUid();
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // addValueEventListener continually checks view for changes
        mDatabase.child("users").child(userId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(RequestActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeNewRequest(userId, body);
                        }
                        setEditingEnabled(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewRequest(String userId, String body) {
        SharedPreferences locationPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Double currentLat = Double.parseDouble(locationPrefs.getString("Latitude", "None"));
        Double currentLong = Double.parseDouble(locationPrefs.getString("Longitude", "None"));
        this.geoFire = new GeoFire(FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(GEO_FIRE_DB + "locations"));
        String key = mDatabase.child("requests").push().getKey();
        Request request = new Request(body, userId);
        geoFire.setLocation(key, new GeoLocation(currentLat, currentLong));
        Map<String, Object> postValues = request.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/requests/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/requests/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
