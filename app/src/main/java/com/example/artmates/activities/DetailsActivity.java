package com.example.artmates.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.artmates.ImageClassifier;
import com.example.artmates.Post;
import com.example.artmates.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

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


        post = getIntent().getParcelableExtra("posts");

        Log.d("DetailsActivity", "User received = " + post.getDescription());

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



//        final List<String> predicitonsList = new ArrayList<>();
//        for (ImageClassifier.Recognition recog : predicitons) {
//            predicitonsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
//        }
//        predicitonsList.add("test 1");
//        predicitonsList.add("test 2");
//        predicitonsList.add("test 3");
//
//        ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
//                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, predicitonsList);
//        lvLabels.setAdapter(predictionsAdapter);

        ParseFile profileImg = (ParseFile) postUser.get("profileImage");
        if (profilePic != null) {
            // Glide.with(this).load(profileImg.getUrl()).circleCrop().into(profilePic);
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance().format(calendar.getTime());
        date.setText(currentDateString);
    }
}

