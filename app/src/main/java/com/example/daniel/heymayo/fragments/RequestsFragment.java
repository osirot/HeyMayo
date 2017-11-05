package com.example.daniel.heymayo.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RequestsFragment extends PostListFragment {

    public RequestsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("posts");
                //.child(getUid());
    }
}
