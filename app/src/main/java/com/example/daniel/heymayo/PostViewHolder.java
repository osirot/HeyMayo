package com.example.daniel.heymayo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.daniel.heymayo.models.Post;

import org.w3c.dom.Text;

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

    public void bindToPost(Post post) {
        //titleView.setText(post.title);
        //authorView.setText(post.author);
        userId.setText(post.uid);
        bodyView.setText(post.body);
    }
}
