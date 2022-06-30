package com.example.artmates.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.artmates.DatePicker;
import com.example.artmates.ImageClassifier;
import com.example.artmates.activities.MainActivity;
import com.example.artmates.Post;
import com.example.artmates.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
    private TextView availableDate;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private Button btnSubmit;
    private ImageButton ibDate;
    public static final int PICK_PHOTO_CODE = 1042;
    private ImageClassifier imageClassifier;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 10001;
    private ListView lvLabels;
    private List<String> labels;
    final List<String> predictionsList = new ArrayList<>();





    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);

//        resultLauncher = registerForActivityResult(
//                new ActivityResultContracts.GetContent(),
//                new ActivityResultCallback<Uri>() {
//
//                    @Override
//                    public void onActivityResult(Uri result) {
//                        if (result != null) {
////                            Glide.with(getContext())
////                                    .load(result)
////                                    .circleCrop()
////                                    .into(ivProfilePicture);
//
//
//                            Bitmap selectedImageBitmap;
//                            try {
//                                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), result);
//                                selectedImageBitmap = ImageDecoder.decodeBitmap(source);
//                            }
//                            catch (IOException e) {
//                                e.printStackTrace();
//                                return;
//                            }
//
//                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                            selectedImageBitmap.compress(Bitmap.CompressFormat.PNG, 20, outputStream);
//                            byte[] image = outputStream.toByteArray();
//
//
//                            ParseFile profileImage = new ParseFile("profilePicture.png", image);
//
//                            ParseUser.getCurrentUser().put("profilePicture", profileImage);
//                            ParseUser.getCurrentUser().saveInBackground(e -> {
//                                if(e == null) {
//                                    Log.i(TAG, "Updated profile.");
//                                    Toast.makeText(getActivity(), "Updated profile picture.", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Log.e(TAG, "Failed to update profile.", e);
//                                }
//                            });

//                        } else { // Result was a failure
//                            Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

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
        availableDate = view.findViewById(R.id.availableDate);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        ibDate = view.findViewById(R.id.ibDate);
        lvLabels = view.findViewById(R.id.lvLabels);

        try {
            imageClassifier = new ImageClassifier(this.getActivity());
        } catch (IOException e) {
            Log.e("Image Classifier Error", "ERROR: " + e);
        }
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
                if(date.isEmpty()){
                    Toast.makeText(getContext(), "Available date cannot be empty", Toast.LENGTH_SHORT).show();
                }
                if(photoFile == null || ivPostImage.getDrawable() == null){
                    Toast.makeText(getContext(), "Image box cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                labels = predictionsList;
                savePost(description,aboutWork, location, date, currentUser, photoFile, labels);
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
                List<ImageClassifier.Recognition> predicitons = imageClassifier.recognizeImage(
                        takenImage, 0);

//                 creating a list of string to display in list view
//                final List<String> predictionsList = new ArrayList<>();
                for (ImageClassifier.Recognition recog : predicitons) {
                    predictionsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
                }

//                 creating an array adapter to display the classification result in list view
                ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                        getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, predictionsList);
                lvLabels.setAdapter(predictionsAdapter);
            }
            else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
         else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            Bitmap selectedImage = loadFromUri(photoUri);

            ivPostImage = getView().findViewById(R.id.ivPostImage);
            ivPostImage.setImageBitmap(selectedImage);

            List<ImageClassifier.Recognition> predicitons = imageClassifier.recognizeImage(
                    selectedImage, 0);

            // creating a list of string to display in list view
            final List<String> predictionsList = new ArrayList<>();
            for (ImageClassifier.Recognition recog : predicitons) {
                predictionsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
            }

            // creating an array adapter to display the classification result in list view
            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, predictionsList);
            lvLabels.setAdapter(predictionsAdapter);
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
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.artmates", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if(Build.VERSION.SDK_INT > 27){
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
        }
        return image;
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    private void savePost(String description,String aboutArt,String location, String date, ParseUser currentUser, File photoFile, List<String> labels) {
        Post post = new Post();
        post.setDescription(description);
        post.setLocation(location);
        post.setAboutArt(aboutArt);
        post.setAvailableDate(date);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.setLabels(labels);

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