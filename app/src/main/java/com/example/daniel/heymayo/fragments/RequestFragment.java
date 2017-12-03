package com.example.daniel.heymayo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniel.heymayo.R;
import com.example.daniel.heymayo.ReplyActivity;
import com.example.daniel.heymayo.models.Request;
import com.example.daniel.heymayo.models.Time;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.example.daniel.heymayo.models.Time.getUnixTime;

public class RequestFragment extends Fragment {

    private static final String TAG = "RequestFragment";

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Request, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;


    public RequestFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_request, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        // forces recyclerview to move 1 page or element at a time instead of free scrolling thru it all
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecycler);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        //mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(postsQuery, Request.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Request, PostViewHolder>(options) {
            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_request_fragment, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(PostViewHolder viewHolder, int position, final Request model) {
                viewHolder.bindToPost(model);

                final DatabaseReference postRef = getRef(position);

                // Set click listener to view replies for a request
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), ReplyActivity.class);
                        intent.putExtra(ReplyActivity.EXTRA_POST_KEY, postKey); //
                        startActivity(intent);
                    }
                });
            }
        };

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition = mManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mManager.scrollToPosition(positionStart);
                }
            }
        });
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    // queries DB at the requests node. orders all requests by timestamp, then it takes the current
    // time in milliseconds and subtracts an hour (converted to ms) to determine which post
    // to start getting. it then gets all posts beyond where it started (so it gets all posts less than
    // or equal to an hour old)
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("requests").orderByChild("timestamp").startAt((getUnixTime() - 3600000));
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView bodyView;
        public TextView timeStamp;

        public PostViewHolder(View itemView) {
            super(itemView);
            bodyView = itemView.findViewById(R.id.include_body);
            timeStamp = itemView.findViewById(R.id.request_time);
        }

        public void bindToPost(Request request) {
            bodyView.setText(request.body);
            timeStamp.setText(Time.formatDateTime(request.timestamp));
        }
    }

}
