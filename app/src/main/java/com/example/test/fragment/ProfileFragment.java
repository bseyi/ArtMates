package com.example.test.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.activities.InfoActivity;
import com.example.test.activities.LandingPageActivity;
import com.example.test.adapter.ProfileAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int POST_LIMIT = 20;
    private RecyclerView rvProfile ;
    private ProfileAdapter adapter;
    private List<Post> allPosts;
    private Button btnInfo;
    private ImageButton btnLogout;
    private TextView tvUsername2;
    private ImageView imageView2;


    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProfile = view.findViewById(R.id.rvProfile);
        btnInfo = view.findViewById(R.id.btnInfo);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvUsername2 = view.findViewById(R.id.tvUsername2);
        imageView2 = view.findViewById(R.id.imageView2);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvProfile.setLayoutManager(gridLayoutManager);

        allPosts = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPosts);

        rvProfile.setAdapter(adapter);
        queryPosts();


        if (ParseUser.getCurrentUser() != null) {
            ParseUser user = ParseUser.getCurrentUser();
            if (user.getParseFile("profilePicture") != null) {
                Glide.with(this)
                        .load(user.getParseFile("profilePicture").getUrl())
                        .circleCrop() // create an effect of a round profile picture
                        .into(imageView2);

            }
        }


        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), InfoActivity.class);
                startActivity(i);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();

                if(currentUser != null) {
                    ParseUser.logOut();
                    goToLandingPageActivity();
                }
            }
        });


    }

    private void queryPosts() {
        ParseQuery<Post> query = createBaseQuery();
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                tvUsername2.setText(posts.get(0).getUser().getUsername());

                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static ParseQuery createBaseQuery(){
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        return query;
    }

    private void goToLandingPageActivity() {
        Intent i = new Intent(getContext(), LandingPageActivity.class);
        startActivity(i);
        getActivity().finish();
    }



}