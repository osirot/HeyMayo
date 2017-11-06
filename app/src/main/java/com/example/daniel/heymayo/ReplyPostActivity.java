package com.example.daniel.heymayo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.daniel.heymayo.models.User;
import com.example.daniel.heymayo.models.Reply;
import com.example.daniel.heymayo.models.Request;

/**
 * Created by jsayler on 11/5/17.
 */

public class ReplyPostActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ReplyPostActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private static final String REQUIRED = "Required";

    private DatabaseReference mRequestReference;
    private DatabaseReference mReplyReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private ReplyAdapter mAdapter;

    private TextView mBodyView;
    private EditText mReplyField;
    private Button mReplyButton;
    private RecyclerView mReplyRecycler;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRequestReference = mDatabase.child("requests").child(mPostKey);
        mReplyReference = mDatabase.child("replies").child(mPostKey);

        // Initialize Views
        mBodyView = findViewById(R.id.post_body);
        mReplyField = findViewById(R.id.field_reply_text);
        mReplyButton = findViewById(R.id.button_post_reply);
        mReplyRecycler = findViewById(R.id.recycler_replies);

        mReplyButton.setOnClickListener(this);
        mReplyRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Request post = dataSnapshot.getValue(Request.class);
                //mAuthorView.setText(post.author);
                mBodyView.setText(post.body);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(ReplyPostActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mRequestReference.addValueEventListener(postListener);

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for replies
        mAdapter = new ReplyAdapter(this, mReplyReference);
        mReplyRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mRequestReference.removeEventListener(mPostListener);
        }

        // Clean up reply listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_reply) {
            postReply();
        }
    }

    private void postReply() {
        final String body = mReplyField.getText().toString();
        if (TextUtils.isEmpty(body)) {
            mReplyField.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);

        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        final String userId = getUid();

        // addValueEventListener continually checks view for changes
        mDatabase.child("users").child(userId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(ReplyPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeNewRequest(userId, body);
                            mReplyField.setText(null);
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
        mReplyField.setEnabled(enabled);
        if (enabled) {
            mReplyButton.setVisibility(View.VISIBLE);
        } else {
            mReplyButton.setVisibility(View.GONE);
        }
    }

    private void writeNewRequest(String userId, String body) {
        String key = mReplyReference.push().getKey();
        Reply reply = new Reply(userId, body);
        Map<String, Object> postValues = reply.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/replies/" + mPostKey + "/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/replies/" + mPostKey + "/" + key, postValues);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

//---------------


    //custom view holder static class
    private static class ReplyViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;

        public ReplyViewHolder(View itemView) {
            super(itemView);
            authorView = itemView.findViewById(R.id.reply_author);
            bodyView = itemView.findViewById(R.id.reply_body);
        }
    }

//--------------

    //custom adapter static class
    private static class ReplyAdapter extends RecyclerView.Adapter<ReplyViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mReplyIds = new ArrayList<>();
        private List<Reply> mReplies = new ArrayList<>();

        public ReplyAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new reply has been added, add it to the displayed list
                    Reply reply = dataSnapshot.getValue(Reply.class);

                    // Update RecyclerView
                    mReplyIds.add(dataSnapshot.getKey());
                    mReplies.add(reply);
                    notifyItemInserted(mReplies.size() - 1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A reply has changed, use the key to determine if we are displaying this
                    // reply and if so displayed the changed reply.
                    Reply newReply = dataSnapshot.getValue(Reply.class);
                    String replyKey = dataSnapshot.getKey();

                    int ReplyIndex = mReplyIds.indexOf(replyKey);
                    if (ReplyIndex > -1) {
                        // Replace with the new data
                        mReplies.set(ReplyIndex, newReply);

                        // Update the RecyclerView
                        notifyItemChanged(ReplyIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + replyKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postReplies:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load replies.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public ReplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_reply, parent, false);
            return new ReplyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReplyViewHolder holder, int position) {
            Reply reply = mReplies.get(position);
            holder.authorView.setText(reply.uid);
            holder.bodyView.setText(reply.body);
        }

        @Override
        public int getItemCount() {
            return mReplies.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
