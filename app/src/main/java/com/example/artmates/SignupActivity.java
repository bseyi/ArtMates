package com.example.artmates;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.File;
import java.io.IOException;

public class SignupActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;

    private static final String TAG = "SignupActivity";
    private EditText etNewUser;
    private EditText etEmail;
    private EditText etNewPassword;
    private ImageView ivProfileImage2;
    private Button  goToSignIn;
    private Button btnSignUp2;
    private Button btnTakePhoto;
    private Button btnUploadPhoto;
    private File photoFile;
    private static final int PICK_PHOTO_CODE = 1042;
    private Context context;
    private String photoFileName = "photo.jpg";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etNewUser = findViewById(R.id.etNewUser);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        goToSignIn = findViewById(R.id.goToSignIn);
        btnSignUp2 = findViewById(R.id.btnSignUp2);
        btnTakePhoto = findViewById(R.id.btnTakePhoto2);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto2);
        ivProfileImage2 = findViewById(R.id.ivProfileImage2);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });


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
                ParseUser user = ParseUser.getCurrentUser();
                signUpUser(username, password, email);
            }
        });

    }

    private void signUpUser(String username, String password, String email) {
        Log.i(TAG, "Attempting to login user");

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

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

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(SignupActivity.this, "com.codepath.fileprovider.artmates", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivProfileImage2.setImageBitmap(takenImage);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            Bitmap selectedImage = loadFromUri(photoUri);

            ImageView ivPreview;
            ivPreview = (ImageView)findViewById(R.id.ivProfileImage2);
            ivPreview.setImageBitmap(selectedImage);
        }
    }

    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }


        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
        }
        return image;
    }



}




