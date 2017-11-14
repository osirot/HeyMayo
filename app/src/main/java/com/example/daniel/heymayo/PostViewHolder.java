package com.example.daniel.heymayo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.daniel.heymayo.models.Request;

public class PostViewHolder extends RecyclerView.ViewHolder {

    //public TextView titleView;
    //public TextView authorView;
    public TextView userId;
    public TextView bodyView;

    public PostViewHolder(View itemView) {
        super(itemView);

        //titleView = itemView.findViewById(R.id.post_title);
        //authorView = itemView.findViewById(R.id.post_author);
        userId = itemView.findViewById(R.id.post_author);
        bodyView = itemView.findViewById(R.id.post_body);
    }

    public void bindToPost(Request request) {
        //titleView.setText(request.title);
        //authorView.setText(request.author);
        userId.setText(request.uid);
        bodyView.setText(request.body);
    }
}
