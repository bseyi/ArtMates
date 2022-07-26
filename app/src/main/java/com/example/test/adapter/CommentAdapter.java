package com.example.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.Comment;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.TimeAgo;
import com.parse.ParseFile;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    private Context context;
    private List<Comment> comments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View commentView = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(commentView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePic;
        private TextView userComment;
        private TextView username;
        private TextView timeAgoTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            userComment = itemView.findViewById(R.id.userComment);
            username = itemView.findViewById(R.id.username);
            timeAgoTxt = itemView.findViewById(R.id.timeAgo);
        }

        public void bind(Comment comment) {
            username.setText(comment.getUser().getUsername());
            userComment.setText(comment.getText());
            timeAgoTxt.setText(TimeAgo.calculateTimeAgo(comment.getCreatedAt()));

            ParseFile profileImg = (ParseFile) comment.getUser().get("profilePicture");
            if (profileImg != null) {
                Glide.with(context).load(profileImg.getUrl()).circleCrop().into(profilePic);
            }

        }
    }
}


