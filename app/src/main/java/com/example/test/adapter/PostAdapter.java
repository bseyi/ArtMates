package com.example.test.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.Comment;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.TimeAgo;
import com.example.test.activities.CommentActivity;
import com.example.test.activities.DetailsActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "PostAdapter";
    private Context context;
    private List<Post> posts;
    private List<Post> postsToDisplay;
    private int icon;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postsToDisplay.get(position);
        holder.bind(post);
    }

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        postsToDisplay = new ArrayList<>(posts);
    }

    @Override
    public int getItemCount() {
        return postsToDisplay.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDescription;
        private TextView tvUsername;
        private ImageView ivImage;
        private TextView timeStamp;
        private TextView tvLocation;
        private ConstraintLayout postContainer;
        private ImageView profilePic;
        private ImageView likeButton;
        private TextView tvAllComments;
        private ImageView commentButton;
        private String myText;
        private boolean clicked = true;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage2);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            tvLocation = itemView.findViewById(R.id.tvLocation2);
            postContainer = itemView.findViewById(R.id.postContainer);
            profilePic = itemView.findViewById(R.id.ivProfileImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            tvAllComments = itemView.findViewById(R.id.tvAllComments);
            commentButton = itemView.findViewById(R.id.commentButton);
        }

        public void bind(Post post) {
            int position = getAdapterPosition();
            post = posts.get(position);
            Post finalPost = post;

            postContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra("posts", Parcels.wrap(finalPost));
                        context.startActivity(intent);
                    }
                }
            });

            tvAllComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(context, CommentActivity.class);
                        intent.putExtra(Post.class.getSimpleName(), Parcels.wrap(finalPost));
                        context.startActivity(intent);
                    }
                }
            });

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != RecyclerView.NO_POSITION) {
                        if (clicked) {
                            finalPost.setLike();
                            clicked = false;
                            icon = R.drawable.ufi_heart_active;

                        } else {
                            finalPost.resetLike();

                            clicked = true;
                            icon = R.drawable.ufi_heart;

                        }

                        likeButton.setImageDrawable(ContextCompat.getDrawable(context, icon));
                    }
                    finalPost.saveInBackground();
                }
            });

            Post finalPost1 = post;
            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder b = new  AlertDialog.Builder(context);
                    b.setTitle("Say something nice");

                    final EditText comment = new EditText(context);
                    comment.setInputType(InputType.TYPE_CLASS_TEXT);

                    b.setView(comment);
                    b.setPositiveButton("Comment",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    myText = comment.getText().toString();
                                    Comment comment = new Comment();
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    ParseObject currentPost = finalPost1.getUser();

                                    comment.setText(myText);
                                    comment.setUser(currentUser);
                                    comment.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(com.parse.ParseException e) {
                                            Toast.makeText(context, "Comment made successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    updateObject(comment, finalPost1.getObjectId());
                                }
                            }
                    );
                    b.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.dismiss();
                                }
                            }
                    );
                    b.show();
                }
            });

            TimeAgo timeAgo = new TimeAgo();
            tvDescription.setText(post.getDescription());
            tvUsername.setText(post.getUser().getUsername());
            tvLocation.setText(post.getLocation());
            timeStamp.setText(timeAgo.calculateTimeAgo(post.getCreatedAt()));
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            ParseFile profileImg = (ParseFile) post.getUser().get("profilePicture");
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(profilePic);
            }
        }

    }

    public void clear() {
        int size = posts.size();
        postsToDisplay.clear();
        posts.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void sortByDateEarliest() throws ParseException {

        Collections.sort(postsToDisplay, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                try {
                    return o1.getDateObject().compareTo(o2.getDateObject());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return -1;
            }

        });
        notifyDataSetChanged();
    }

    public void sortByDateLatest() {

        Collections.sort(postsToDisplay, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                try {
                    return o2.getDateObject().compareTo(o1.getDateObject());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return -1;
            }
        });
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        postsToDisplay.addAll(list);
        notifyDataSetChanged();
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Post> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(posts);
            } else {

                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Post post : posts) {
                    if (post.getLabels().toLowerCase().contains(filterPattern)) {
                        filteredList.add(post);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            postsToDisplay.clear();
            if (results.values == null) {
                Log.e("PostsAdapter", "results.values is null");
            } else {
                postsToDisplay.addAll((List) results.values);
                Log.d("PostsAdapter", "Displaying " + results.count + " results through filter");
            }
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private void updateObject(Comment comment, String objectId) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.getInBackground(objectId, (object, e) -> {
            if (e == null) {
                ParseRelation<Comment> relation = object.getRelation("comment");
                relation.add(comment);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {

                    }
                });

            } else {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, e.toString());
            }
        });
    }
}
