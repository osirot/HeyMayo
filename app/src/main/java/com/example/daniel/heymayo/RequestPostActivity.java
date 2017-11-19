package com.example.daniel.heymayo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.heymayo.models.Request;
import com.example.daniel.heymayo.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RequestPostActivity extends MainActivity {

    private static final String TAG = "RequestPostActivity";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;
    //private FragmentPagerAdapter mPagerAdapter;
    //private ViewPager mViewPager;


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
            }
        });
/*
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new RequestsFragment()
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
          };

        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
*/
    }

    private void submitPost() {
        final String body = mBodyField.getText().toString();
        final String userId = getUid();
        final long timestamp = getUnixTime();
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
                            Toast.makeText(RequestPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeNewRequest(userId, body, timestamp);
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

    private void writeNewRequest(String userId, String body, Long timestamp) {
        String key = mDatabase.child("requests").push().getKey();
        Request request = new Request(body, userId, timestamp);
        Map<String, Object> postValues = request.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/requests/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/requests/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private long getUnixTime() {
        return System.currentTimeMillis();
    }

    //deprecated - not needed (no menu bar)
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
*/
}
