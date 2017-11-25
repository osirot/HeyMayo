package com.example.daniel.heymayo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.heymayo.models.Reply;
import com.example.daniel.heymayo.models.Time;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.daniel.heymayo.models.User;
import com.example.daniel.heymayo.models.Request;

/**
 * Created by jsayler on 11/5/17.
 */

public class ReplyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ReplyActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private static final String REQUIRED = "Required";

    private DatabaseReference mRequestReference;
    private DatabaseReference mReplyReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private ReplyAdapter mAdapter;
    private TextView mBodyView;
    private TextView mTimeStamp;
    private EditText mReplyField;
    private Button mReplyButton;
    private RecyclerView mReplyRecycler;
    private DatabaseReference mDatabase;
    private ImageView starView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

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
        mBodyView = findViewById(R.id.include_body);
        mTimeStamp = findViewById(R.id.request_time);
        mReplyField = findViewById(R.id.field_reply_text);
        mReplyButton = findViewById(R.id.button_post_reply);
        mReplyRecycler = findViewById(R.id.recycler_replies);
        starView = (ImageView) findViewById(R.id.reply_star);

        mReplyButton.setOnClickListener(this);
        mReplyRecycler.setLayoutManager(new LinearLayoutManager(this));

        // keeping the following code because we may need it later
/*      // creates a listener attached to recyclerView that activates when an element in the
        // recyclerView is touched or long touched
        mReplyRecycler.addOnItemTouchListener(new RecyclerTouchListener(this, mReplyRecycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                ValueEventListener postListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Post object and use the values to update the UI
                        Reply post = dataSnapshot.getValue(Reply.class);
                        body = post.body;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    }
                };
                mReplyReference.addValueEventListener(postListener);
                Log.d("TESTING", "" + body);
                ImageView star_outline = view.findViewById(R.id.star_outline);
                star_outline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        karma.pointCounter();
                        //Toast.makeText(ReplyActivity.this, "Single Click on Image at position " + position, Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.d("RPA_TOUCH_LISTENER", "single touch event on position " + position);
                Toast.makeText(ReplyActivity.this, "Single press on position: " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onLongClick(View view, int position) {
                //Log.d("RPA_TOUCH_LISTENER", "long touch event on position " + position);
                Toast.makeText(ReplyActivity.this, "Long press on position: " + position, Toast.LENGTH_SHORT).show();
            }
        })); */
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
                //mAuthorView.setText(post.uid);
                mBodyView.setText(post.body);
                mTimeStamp.setText(Time.formatDateTime(post.timestamp));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(ReplyActivity.this, "Failed to load post.",
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

    // onClick method for the reply button
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_reply) {
            postReply();
        }
    }

    // handles all the steps in submitting a reply to db/client
    private void postReply() {
        final String body = mReplyField.getText().toString();
        final String userId = getUid();

        if (TextUtils.isEmpty(body)) {
            mReplyField.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);

        //Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // addValueEventListener continually checks view for changes
        mDatabase.child("users").child(userId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(ReplyActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            writeNewReply(userId, body);
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

    // temporarily makes submit button invisible to prevent spamming replies
    private void setEditingEnabled(boolean enabled) {
        mReplyField.setEnabled(enabled);
        if (enabled) {
            mReplyButton.setVisibility(View.VISIBLE);
        } else {
            mReplyButton.setVisibility(View.GONE);
        }
    }

    // writes new replies to database
    private void writeNewReply(String userId, String body) {
        String key = mReplyReference.push().getKey();
        Reply reply = new Reply(userId, body);
        Map<String, Object> postValues = reply.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/replies/" + mPostKey + "/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/replies/" + mPostKey + "/" + key, postValues);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

    // gets the id of the user
    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // this class handles setting the karma point flag in replies and calls updateKarmaPoints to apply points to user
    private void onKarmaClicked(final Reply reply, final DatabaseReference postRef, final String replyKey) {

        // set a flag in both of these places to indicate karma points given
        final DatabaseReference globalPostRef = mDatabase.child("replies").child(postRef.getKey()).child(replyKey);
        final DatabaseReference userPostRef = mDatabase.child("user-posts").child(reply.uid).child("replies").child(postRef.getKey()).child(replyKey);
        // location of where points count is kept - this gets incremented every time user earns karma point
        final DatabaseReference userCount = mDatabase.child("users").child(reply.uid);

        if (reply.karma) {
            globalPostRef.child("karma").setValue(false);
            userPostRef.child("karma").setValue(false);
            updateKarmaPoints(userCount, false);
        } else {
            globalPostRef.child("karma").setValue(true);
            userPostRef.child("karma").setValue(true);
            updateKarmaPoints(userCount, true);
        }

        // debugging code
        //Log.d("karma:", "" + reply.karma);
        //Log.d("postRef:", "" + postRef);
        //Log.d("globalPostRef:", "" + globalPostRef);
        //Log.d("userPostRef:", "" + userPostRef);
        //Log.d("userCount:", "" + userCount);
    }

    private void updateKarmaPoints(DatabaseReference postRef, final Boolean karma) {
        // this creates a transaction which puts a lock on the field being updated
        // to prevent simultaneous updates / data getting overwritten
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User u = mutableData.getValue(User.class);
                // if the user is null, it returns nothing
                if (u == null) {
                    return Transaction.success(mutableData);
                }
                // if the user has karma, add 1 to karmaPoints field
                if (karma) {
                    u.karmaPoints = u.karmaPoints + 1;
                // else, subtract 1 from karmaPoints (assumes being taken away)
                } else {
                    u.karmaPoints = u.karmaPoints - 1;
                }
                // Set value and report transaction success
                mutableData.setValue(u);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    // this view holder is for posts made by the user (person holding the phone)
    private static class UserViewHolder extends RecyclerView.ViewHolder {

        public TextView bodyView;
        public TextView timeStamp;

        public UserViewHolder(View itemView) {
            super(itemView);
            bodyView = itemView.findViewById(R.id.requestor_reply_body);
            timeStamp = itemView.findViewById(R.id.requestor_reply_time);
        }

        public void bindToPost(Reply reply) {
           bodyView.setText(reply.body);
           timeStamp.setText(Time.formatDateTime(reply.timestamp));
        }
    }
    // this view holder is for posts made by others
    public static class NotUserPostViewHolder extends RecyclerView.ViewHolder {

        public ImageView starView;
        public TextView bodyView;
        public TextView timeStamp;

        public NotUserPostViewHolder(View itemView) {
            super(itemView);
            starView = itemView.findViewById(R.id.reply_star);
            bodyView = itemView.findViewById(R.id.reply_body);
            timeStamp = itemView.findViewById(R.id.reply_time);
        }

        public void bindToPost(Reply reply, View.OnClickListener starClickListener) {
            bodyView.setText(reply.body);
            timeStamp.setText(Time.formatDateTime(reply.timestamp));
            starView.setOnClickListener(starClickListener);
        }
    }

    //custom adapter class that handles getting info from firebase, determining if userID matches
    // this user's id, and routes to different view holders based on that info
    private class ReplyAdapter extends RecyclerView.Adapter {

        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mReplyIds = new ArrayList<>();
        private List<Reply> mReplies = new ArrayList<>();


        public ReplyAdapter(final Context context, final DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    // debug code
                    //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                    //Log.d(TAG, "TEST DEBUG:" + ref);
                    //Log.d(TAG, "reply post key:" + dataSnapshot.getKey());

                    // A new reply has been added, add it to the displayed list
                    Reply reply = dataSnapshot.getValue(Reply.class);

                    // Update RecyclerView
                    mReplyIds.add(dataSnapshot.getKey());

                    mReplies.add(reply);
                    //Log.d(TAG, "onChildAdded-mReply size:" + getItemCount());
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
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            LayoutInflater inflater = LayoutInflater.from(mContext);
            // uses viewType to determine if this was sent by you or someone else and
            // redirects to the correct view holder
            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = inflater.inflate(R.layout.item_reply_user, parent, false);
                return new UserViewHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = inflater.inflate(R.layout.item_reply_not_user, parent, false);
                return new NotUserPostViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final Reply model = mReplies.get(position);
            final DatabaseReference postRef = mDatabaseReference.getRef();

            switch (holder.getItemViewType()) {
                // any replies that match your user id are sent to this view holder, which prevents
                // giving yourself karma points
                case VIEW_TYPE_MESSAGE_SENT:
                    ((UserViewHolder) holder).bindToPost(model);
                    //Log.d("viewHolder:", "you");
                    break;
                // this determines what data is sent to viewholder. it also holds logic for displaying if
                // a post has been given karma points or not
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    // Determine if the requestor has given user karma points and set UI accordingly
                    if (model.karma) {
                        ((NotUserPostViewHolder) holder).starView.setImageResource(R.drawable.ic_toggle_star_24);
                    } else {
                        ((NotUserPostViewHolder) holder).starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    }
                    // Bind Post to ViewHolder, setting OnClickListener for the karma button
                    ((NotUserPostViewHolder) holder).bindToPost(model, new View.OnClickListener() {
                        @Override
                        public void onClick(View starView) {
                            onKarmaClicked(model, postRef, mReplyIds.get(position));
                        }
                    });
                    //Log.d("viewHolder:", "not you");
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mReplies.size();
        }

        @Override
        public int getItemViewType(int position) {
            Reply reply = mReplies.get(position);
            String userID = getUid();
            // checks if message is from you or someone else
            if (reply.uid.equals(userID)) {
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }

// keeping this code - it goes along with the commented out code in onCreate()
/*
    // click listener interface
    private interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    // touch listener inner class for processing touch events
    private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){
            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null){
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
                //return true;
            }
            return false;
        }
        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {}
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    }
*/
}
