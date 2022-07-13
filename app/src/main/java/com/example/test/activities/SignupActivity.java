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
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;

public class SignupActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final String CONTENT_TYPE = "profilePicture.png";
    private static final String  AUTHORITY_NAME = "com.example.fileprovider.test";
    private static final int PICK_PHOTO_CODE = 1042;
    private static final String TAG = "SignupActivity";
    private EditText etNewUser;
    private EditText etEmail;
    private EditText etNewPassword;
    private Button goToSignIn;
    private Button btnSignUp2;
    private EditText etFullName;
    private File photoFile;
    private String photoFileName = "photo.jpg";


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

//                ParseFile profileImage = new ParseFile(photoFile, CONTENT_TYPE);
//                photoFile = getPhotoFileUri(photoFileName);

                signUpUser(username, password, email, fullName);
            }
        });


    }
//
//    private void launchCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        photoFile = getPhotoFileUri(photoFileName);
//
//        Uri fileProvider = FileProvider.getUriForFile(SignupActivity.this, AUTHORITY_NAME, photoFile);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
//
//        if (intent.resolveActivity(SignupActivity.this.getPackageManager()) != null) {
//            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
////                ivProfileImage2.setImageBitmap(takenImage);
//            }
//            else {
//                Toast.makeText(SignupActivity.this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
//            }
//        }
//        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
//            Uri photoUri = data.getData();
//
//            Bitmap selectedImage = loadFromUri(photoUri);
//
//            ImageView ivPreview;
//            ivPreview = findViewById(R.id.ivPostImage);
//            ivPreview.setImageBitmap(selectedImage);
//        }
//    }
//
//    public File getPhotoFileUri(String fileName) {
//        File mediaStorageDir = new File(SignupActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
//        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
//            Log.d(TAG, "failed to create directory");
//        }
//
//
//        return new File(mediaStorageDir.getPath() + File.separator + fileName);
//    }
//
//    public void onPickPhoto(View view) {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(intent, PICK_PHOTO_CODE);
//    }
//
//    public Bitmap loadFromUri(Uri photoUri) {
//        Bitmap image = null;
//        try {
//            if(Build.VERSION.SDK_INT > 27){
//                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
//                image = ImageDecoder.decodeBitmap(source);
//            } else {
//                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error loading image", e);
//        }
//        return image;
//    }

    private void signUpUser(String username, String password, String email, String fullName) {
        Log.i(TAG, "Attempting to login user");

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
                    Log.e(TAG, "Error signing up");
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