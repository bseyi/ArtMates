package com.example.test.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.test.Post;
import com.example.test.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;

public class DetailsActivity extends AppCompatActivity {
    public static final String TAG = "DetailsActivity";
    private TextView tvUsername;
    private ImageView ivImage;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvAboutArt;
    private TextView timestamp;
    private ImageView profilePic;
    private TextView tvDate;
    private TextView date;
    private Post post;
    private Context context;
    private ImageView imageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvUsername = findViewById(R.id.tvUsername);
        ivImage = findViewById(R.id.ivImage2);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocation = findViewById(R.id.tvLocation2);
        tvAboutArt = findViewById(R.id.tvAboutArt);
        timestamp = findViewById(R.id.timeStamp);
        profilePic = findViewById(R.id.profilePic);
        tvDate = findViewById(R.id.tvDate);
        date = findViewById(R.id.date);
        imageView = findViewById(R.id.imageView);

        Bundle bundle = getIntent().getExtras();
        Post post = Parcels.unwrap(bundle.getParcelable("posts"));

        
        ParseUser postUser = post.getUser();

        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(imageView);
        }

        if (tvDescription != null) {
            tvDescription.setText(post.getDescription());
        }

        if (tvLocation != null) {
            tvLocation.setText(post.getLocation());
        }

        if (tvAboutArt != null) {
            tvAboutArt.setText(post.getAboutArt());
        }


        if (postUser != null) {
            Log.d("DetailsActivity", "user = " + postUser.getUsername());
            tvUsername.setText(postUser.getUsername());
        }
        if (timestamp != null) {
            timestamp.setText(calculateTimeAgo(post.getCreatedAt()));
        }
        if (tvDate != null) {
            tvDate.setText((String) post.get("available_date"));
        }

        ParseUser user = ParseUser.getCurrentUser();
        if(user.getParseFile("profilePicture") != null){
            Glide.with(this)
                    .load(user.getParseFile("profilePicture").getUrl())
                    .circleCrop() // create an effect of a round profile picture
                    .into(profilePic);
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

}