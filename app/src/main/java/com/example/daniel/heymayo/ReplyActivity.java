package com.example.daniel.heymayo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.heymayo.fragments.RequestFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.example.daniel.heymayo.models.User;
import com.example.daniel.heymayo.models.Reply;
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
    //private TextView mAuthorView;
    private TextView mBodyView;
    private TextView mTimeStamp;
    private EditText mReplyField;
    private Button mReplyButton;
    private RecyclerView mReplyRecycler;

    private DatabaseReference mDatabase;

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
        //mAuthorView = findViewById(R.id.post_author);
        mBodyView = findViewById(R.id.post_body);
        mTimeStamp = findViewById(R.id.post_time);
        mTimeStamp = findViewById(R.id.post_time);
        mReplyField = findViewById(R.id.field_reply_text);
        mReplyButton = findViewById(R.id.button_post_reply);
        mReplyRecycler = findViewById(R.id.recycler_replies);

        mReplyButton.setOnClickListener(this);
        mReplyRecycler.setLayoutManager(new LinearLayoutManager(this));

        // creates a listener attached to recyclerView that activates when an element in the
        // recyclerView is touched or long touched
        mReplyRecycler.addOnItemTouchListener(new RecyclerTouchListener(this, mReplyRecycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Log.d("RPA_TOUCH_LISTENER", "single touch event on position " + position);
                //Toast.makeText(ReplyActivity.this, "Single press on position: " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onLongClick(View view, int position) {
                Log.d("RPA_TOUCH_LISTENER", "long touch event on position " + position);
                //Toast.makeText(ReplyActivity.this, "Long press on position: " + position, Toast.LENGTH_SHORT).show();
            }
        }));
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
                mTimeStamp.setText(RequestFragment.formatDateTime(post));
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_reply) {
            postReply();
        }
    }

    private void postReply() {
        final String body = mReplyField.getText().toString();
        final String userId = getUid();
        final long timestamp = getUnixTime();

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

                            writeNewRequest(userId, body, timestamp);
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

    private void writeNewRequest(String userId, String body, long unixTime) {
        String key = mReplyReference.push().getKey();
        Reply reply = new Reply(userId, body, unixTime);
        Map<String, Object> postValues = reply.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/replies/" + mPostKey + "/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/replies/" + mPostKey + "/" + key, postValues);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private long getUnixTime() {
        return System.currentTimeMillis();
    }

    private static String formatDateTime(Reply reply) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        sdf.setTimeZone(tz);
        Date date = new Date(reply.timestamp);
        String localtime = sdf.format(date);
        return localtime;
    }

//---------------

    //custom view holder static class
    private static class ReplyViewHolder extends RecyclerView.ViewHolder {

        //public TextView authorView;
        public TextView bodyView;
        public TextView timeStamp;


        public ReplyViewHolder(View itemView) {
            super(itemView);
            //authorView = itemView.findViewById(R.id.reply_author);
            bodyView = itemView.findViewById(R.id.reply_body);
            timeStamp = itemView.findViewById(R.id.reply_time);
        }

        public void bind(Reply reply) {
            //authorView.setText(reply.uid);
            bodyView.setText(reply.body);
            timeStamp.setText(formatDateTime(reply));
        }
    }

    //custom view holder static class
    private static class RequestViewHolder extends RecyclerView.ViewHolder {

        //public TextView authorView;
        public TextView bodyView;
        public TextView timeStamp;

        public RequestViewHolder(View itemView) {
            super(itemView);
            //authorView = itemView.findViewById(R.id.requestor_reply_author);
            bodyView = itemView.findViewById(R.id.requestor_reply_body);
            timeStamp = itemView.findViewById(R.id.requestor_reply_time);
        }

        public void bind(Reply reply) {
            //authorView.setText(reply.uid);
            bodyView.setText(reply.body);
            timeStamp.setText(formatDateTime(reply));
        }
    }

//--------------

    //custom adapter class
    private class ReplyAdapter extends RecyclerView.Adapter {

        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mReplyIds = new ArrayList<>();
        private List<Reply> mReplies = new ArrayList<>();
        private List<String> mTimeStamps = new ArrayList<>();

        public ReplyAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                    //Log.d(TAG, "TestOutput:" + mReplyReference.child(dataSnapshot.getKey()).child("timestamp"));
                    // A new reply has been added, add it to the displayed list
                    Reply reply = dataSnapshot.getValue(Reply.class);

                    // Update RecyclerView
                    mReplyIds.add(dataSnapshot.getKey());
                    //Log.d(TAG,"TESTOUTPUT:" + dataSnapshot.getKey());
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
                    //Log.d(TAG,"TESTOUTPUT:" + ReplyIndex);
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
                return new RequestViewHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = inflater.inflate(R.layout.item_reply_others, parent, false);
                return new ReplyViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Reply reply = mReplies.get(position);
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((RequestViewHolder) holder).bind(reply);
                    //Log.d("viewHolder:", "you");
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReplyViewHolder) holder).bind(reply);
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
            // checks if message is from your or someone else
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

//-------

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
}
