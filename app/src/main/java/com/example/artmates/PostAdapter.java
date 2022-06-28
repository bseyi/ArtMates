package com.example.artmates;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<Post> posts;
    private List<Post> postsToDisplay;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivImage = itemView.findViewById(R.id.ivImage2);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            tvLocation = itemView.findViewById(R.id.tvLocation2);
            postContainer = itemView.findViewById(R.id.postContainer);
            profilePic = itemView.findViewById(R.id.ivProfileImage);
        }

        public void bind(Post post) {
            postContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Post post = posts.get(position);
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra("posts", post);
                        context.startActivity(intent);
                    }
                }
            });

            // Bind the post data to the view elements
            tvDescription.setText(post.getDescription());
            tvUsername.setText(post.getUser().getUsername());
            tvLocation.setText(post.getLocation());
            timeStamp.setText(calculateTimeAgo(post.getCreatedAt()));
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            ParseFile profileImg = (ParseFile) post.getUser().get("profileImage");
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(profilePic);
            }
        }

    }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }
        return "";
    }

    public void clear() {
        int size = posts.size();
        postsToDisplay.clear();
        posts.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void sortByPriceAscending() {
        Collections.sort(postsToDisplay, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                return o1.getPrice().compareTo(o2.getPrice());
            }
        });
        notifyDataSetChanged();
    }

    public void sortByPriceDescending() {
        Collections.sort(postsToDisplay, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                return o2.getPrice().compareTo(o1.getPrice());
            }
        });
        notifyDataSetChanged();
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
                //If the user does not enter anything, display the whole list
                filteredList.addAll(posts);
            } else {
                //toLowercase makes it so that the search is not case sensitive
                //trim takes away extra whitespaces
                String filterPattern = constraint.toString().toLowerCase().trim();

                //iterate to see which post matched filterPattern
                for (Post post : posts) {
                    if (post.getLocation().toLowerCase().contains(filterPattern)) {
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
}