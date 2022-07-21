package com.example.test.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.test.Post;
import com.example.test.R;
import com.example.test.adapter.PostAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private static final String TAG = "PostsFragment";
    private static final int POST_LIMIT = 20;
    protected RecyclerView rvPosts;
    private PostAdapter adapter;
    private List<Post> allPosts;
    private SwipeRefreshLayout swipeContainer;

    public PostsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_green_light);
        rvPosts = view.findViewById(R.id.rvPosts);
        allPosts = new ArrayList<>();
        adapter = new PostAdapter(getContext(), allPosts);

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts();
    }

    private void fetchTimelineAsync(int i) {
        adapter.clear();
        queryPosts();
        swipeContainer.setRefreshing(false);
    }

    private final FindCallback<Post> mPostQueryCallback = new FindCallback<Post>() {
        @Override
        public void done(List<Post> posts, ParseException e) {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            adapter.addAll(posts);
            adapter.notifyDataSetChanged();
        }
    };

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(mPostQueryCallback);
    }

    private void queryPostsNewest() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(mPostQueryCallback);
    }
    private void queryByMileRadius(){
        Post post = new Post();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint userLocation = currentUser.getParseGeoPoint("Location");
        post.setGeoLocation(userLocation);
        query.whereNear(Post.KEY_GEOLOCATION, userLocation);
        query.setLimit(POST_LIMIT);
        query.findInBackground(mPostQueryCallback);


    }


    private void queryPostsOldest() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(POST_LIMIT);
        query.addAscendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(mPostQueryCallback);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nearest_post:
                adapter.clear();
                queryByMileRadius();
                adapter.notifyDataSetChanged();
                return true;

            case R.id.newest:
                adapter.clear();
                queryPostsNewest();
                adapter.notifyDataSetChanged();

                return true;

            case R.id.oldest:
                adapter.clear();
                queryPostsOldest();
                adapter.notifyDataSetChanged();

                return true;

            case R.id.early_date:
                try {
                    adapter.sortByDateEarliest();
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                return true;
            case R.id.late_date:
                adapter.sortByDateLatest();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}