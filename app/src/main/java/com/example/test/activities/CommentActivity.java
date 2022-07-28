package com.example.test.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.test.Comment;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.adapter.ChatAdapter;
import com.example.test.adapter.CommentAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "CommentActivity";
    private List<Comment> comments;
    private CommentAdapter commentAdapter;
    public RecyclerView recyclerView;
    private  Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        recyclerView = findViewById(R.id.rvComments);

        comments = new ArrayList<>();
        post = Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        commentAdapter = new CommentAdapter(this, comments);
        queryComments();

    }

    private void queryComments() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e == null) {
                    for (Post p : posts) {
                        if (Objects.equals(p.getObjectId(), post.getObjectId())) {
                            ParseRelation<Comment> mRelation = p.getRelation("comment");
                            ParseQuery<Comment> mQuery = mRelation.getQuery();
                            mQuery.orderByDescending("createdAt");
                            mQuery.findInBackground(new FindCallback<Comment>() {
                                @Override
                                public void done(List<Comment> comment, ParseException e) {
                                    if (e == null) {
                                        for (Comment c : comment) {
                                            comments.add(c);
                                            recyclerView.setAdapter(commentAdapter);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                        }
                                    } else {
                                        Log.e(TAG, "Error Loading Messages" + e);
                                    }
                                }
                            });
                        }

                    }
                }
            }
        });
    }
}


