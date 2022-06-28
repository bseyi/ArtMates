package com.example.artmates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>{
    private Context context;
    private List<Post> posts;
    private List<Post> postsToDisplay;

    public ProfileAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        postsToDisplay = new ArrayList<>(posts);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_grid, parent, false);
        return new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postsToDisplay.get(position);
        holder.bind(post);

    }

    @Override
    public int getItemCount() {
        return postsToDisplay.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivImage2;
//        private TextView tvUsername2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage2 = itemView.findViewById(R.id.ivImage2);
//            tvUsername2 = itemView.findViewById(R.id.tvUsername2);
        }

        public void bind(Post post) {
//            tvUsername2.setText(post.getUser().getUsername());

            ParseFile profileImg = (ParseFile) post.getUser().get("profileImage");
            ParseFile image = post.getImage();


            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage2);
            }
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(ivImage2);
            }
        }
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        postsToDisplay.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        int size = posts.size();
        postsToDisplay.clear();
        posts.clear();
        notifyItemRangeRemoved(0, size);
    }
}
