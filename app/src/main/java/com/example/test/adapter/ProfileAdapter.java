package com.example.test.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.activities.DetailsActivity;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        private ConstraintLayout postContainer2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage2 = itemView.findViewById(R.id.ivImage2);
            postContainer2 = itemView.findViewById(R.id.postContainer2);
        }

        public void bind(Post post) {

            ParseFile profileImg = (ParseFile) post.getUser().get("profileImage");
            ParseFile image = post.getImage();

            postContainer2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Post post = postsToDisplay.get(position);
                        Intent intent = new Intent(context, DetailsActivity.class);
                        intent.putExtra("posts", Parcels.wrap(post));
                        context.startActivity(intent);
                    }

                }
            });

            postContainer2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
                    saveDialog.setTitle("Delete?");
                    saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            Post post = postsToDisplay.get(position);
                            Log.i("Position", "Position is "+ post);

                            post.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e2) {
                                    if(e2==null){
                                        Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(context, "Error: "+e2.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            postsToDisplay.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    saveDialog.show();
                    return true;
                }
            });

            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage2);
            }
            if (image == null){
                Toast.makeText(context, "No posts to display", Toast.LENGTH_SHORT).show();
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
}
