package com.example.daniel.heymayo.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RepliesFragment extends PostListFragment {

    public RepliesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child("requests");
    }
}
