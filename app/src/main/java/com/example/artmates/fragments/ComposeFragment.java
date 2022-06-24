package com.example.artmates.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artmates.CameraActivity;
import com.example.artmates.DatePicker;
import com.example.artmates.MainActivity;
import com.example.artmates.Post;
import com.example.artmates.R;
import com.example.artmates.SignupActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;


public class ComposeFragment extends Fragment implements DatePickerDialog.OnDateSetListener{
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final String TAG = "ComposeFragment";


    private ImageView ivPostImage;
    private Button btnTakePhoto2;
    private Button btnUploadPhoto2;
    private EditText tvCaption;
    private EditText tvLocation2;
    private EditText etAboutWork;
    private ImageView ivProfileImage2;
    private EditText etPrice;
    private TextView availableDate;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private Button btnSubmit;
    private ImageButton ibDate;
    public static final int PICK_PHOTO_CODE = 1042;



    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        btnTakePhoto2 = view.findViewById(R.id.btnTakePhoto2);
        btnUploadPhoto2 = view.findViewById(R.id.btnUploadPhoto2);
        tvCaption = view.findViewById(R.id.tvCaption);
        tvLocation2 = view.findViewById(R.id.tvLocation2);
        etAboutWork = view.findViewById(R.id.etAboutWork);
        etPrice = view.findViewById(R.id.etPrice);
        availableDate = view.findViewById(R.id.availableDate);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        ibDate = view.findViewById(R.id.ibDate);
//        ivProfileImage2 = view.findViewById(R.id.ivProfileImage2);


        btnTakePhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        btnUploadPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = tvCaption.getText().toString();
                String location = tvLocation2.getText().toString();
                String aboutWork = etAboutWork.getText().toString();
                String price = etPrice.getText().toString();
                String date = availableDate.getText().toString();
                if(description.isEmpty()){
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(location.isEmpty()){
                    Toast.makeText(getContext(), "Location cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(aboutWork.isEmpty()){
                    Toast.makeText(getContext(), "About work cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(price.isEmpty()){
                    Toast.makeText(getContext(), "Price cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(date.isEmpty()){
                    Toast.makeText(getContext(), "Available date cannot be empty", Toast.LENGTH_SHORT).show();
                }
                if(photoFile == null || ivPostImage.getDrawable() == null){
                    Toast.makeText(getContext(), "Image box cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description,aboutWork, price, location, date, currentUser, photoFile);
            }
        });

        ibDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePicker();
                datePicker.setTargetFragment(ComposeFragment.this, 0);
                datePicker.show(getActivity().getSupportFragmentManager(), "date picker");
            }
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.artmates", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPostImage.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            Bitmap selectedImage = loadFromUri(photoUri);

            ImageView ivPreview;
            ivPreview = getView().findViewById(R.id.ivPostImage);
            ivPreview.setImageBitmap(selectedImage);
        }
    }

    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
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
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
        }
        return image;
    }

    private void savePost(String description,String aboutArt,String price,String location, String date, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setDescription(description);
        post.setLocation(location);
        post.setAboutArt(aboutArt);
        post.setPrice(price);
        post.setAvailableDate(date);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                {
                    Log.i(TAG, "Error saving post", e);
                    Toast.makeText(getContext(), "Error saving post", Toast.LENGTH_SHORT).show();
                }
                tvCaption.setText("");
                tvLocation2.setText("");
                etAboutWork.setText("");
                etPrice.setText("");
                availableDate.setText("");
                ivPostImage.setImageResource(0);

                goToPostsFragment();
            }

        });
    }
    public void goToPostsFragment(){
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance().format(calendar.getTime());
        availableDate.setText(currentDateString);
    }



}