package com.example.test.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.test.R;
import com.parse.ParseUser;

public class InfoActivity extends AppCompatActivity {
    private static final String TAG = "InfoActivity";
    private ImageView ivProfilePic;
    private TextView tvUserName;

    private TextView tvEmail;
    private TextView tvFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserName = findViewById(R.id.tvUser);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);


        if(ParseUser.getCurrentUser() != null)
        {
            tvUserName.setText(ParseUser.getCurrentUser().getUsername());
            tvEmail.setText(ParseUser.getCurrentUser().getEmail());


            tvFullName.setText(ParseUser.getCurrentUser().getString("fullName"));

            ParseUser user = ParseUser.getCurrentUser();
            if(user.getParseFile("profilePicture") != null){
                Glide.with(this)
                        .load(user.getParseFile("profilePicture").getUrl())
                        .circleCrop() // create an effect of a round profile picture
                        .into(ivProfilePic);
            }

        }
    }


}