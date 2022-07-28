package com.example.test.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.R;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private EditText etNewUser;
    private EditText etEmail;
    private EditText etNewPassword;
    private Button goToSignIn;
    private Button btnSignUp2;
    private EditText etFullName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etNewUser = findViewById(R.id.etNewUser);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        goToSignIn = findViewById(R.id.goToSignIn);
        btnSignUp2 = findViewById(R.id.btnSignUp2);
        etFullName = findViewById(R.id.etFullName);

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLoginActivity();
            }
        });

        btnSignUp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etNewUser.getText().toString();
                String password = etNewPassword.getText().toString();
                String email = etEmail.getText().toString();
                String fullName = etFullName.getText().toString();

                signUpUser(username, password, email, fullName);
            }
        });
    }

    private void signUpUser(String username, String password, String email, String fullName) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("fullName", fullName);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Toast.makeText(SignupActivity.this, "Success signing up", Toast.LENGTH_SHORT).show();

                    goToLoginActivity();
                }
                else{
                    Toast.makeText(SignupActivity.this, "Error signing up", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}