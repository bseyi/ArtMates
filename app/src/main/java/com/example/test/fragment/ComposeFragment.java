package com.example.test.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
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

import com.example.test.DatePicker;
import com.example.test.ImageClassifier;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.activities.CanvasActivity;
import com.example.test.activities.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ComposeFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final String TAG = "ComposeFragment";


    private ImageView ivPostImage;
    private Button btnCanvas;
    private Button btnTakePhoto2;
    private Button btnUploadPhoto2;
    private EditText tvCaption;
    private TextView tvLocation2;
    private EditText etAboutWork;
    private TextView availableDate;
    private String photoFileName = "photo.jpg";
    private File photoFile;
    private Button btnSubmit;
    private ImageButton ibDate;
    private Button btnGetLocation;
    private static final int PICK_PHOTO_CODE = 1042;
    private ImageClassifier imageClassifier;
    private ListView lvLabels;
    private String labels = " ";
    private final List<String> predictionsList = new ArrayList<>();
    private LocationManager locationManager;
    private FusedLocationProviderClient client;
    private static final String  AUTHORITY_NAME = "com.example.fileprovider.test";
    private static final int REQUEST_LOCATION = 1;
    private Location location2;
    private FusedLocationProviderClient fusedLocationProviderClient;





    public ComposeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        availableDate = view.findViewById(R.id.availableDate);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        ibDate = view.findViewById(R.id.ibDate);
        lvLabels = view.findViewById(R.id.lvLabels);
        btnGetLocation = view.findViewById(R.id.btnGetLocation);
        btnCanvas = view.findViewById(R.id.btnCanvas);

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            imageClassifier = new ImageClassifier(this.getActivity());
        } catch (IOException e) {
            Log.e("Image Classifier Error", "ERROR: " + e);
        }


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
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (location.isEmpty()) {
                    Toast.makeText(getContext(), "Location cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (aboutWork.isEmpty()) {
                    Toast.makeText(getContext(), "About work cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (date.isEmpty()) {
                    Toast.makeText(getContext(), "Available date cannot be empty", Toast.LENGTH_SHORT).show();
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(getContext(), "Image box cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseUser currentUser = ParseUser.getCurrentUser();

                savePost(description, aboutWork, location, date, currentUser, photoFile, labels);

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

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity()
                        , Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity()
                                , Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                }
            }
        });

        btnCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CanvasActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && (grantResults.length > 0) &&
                (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            Toast.makeText(getActivity()
                    , "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            getLocationDetails(location.getLatitude(), location.getLongitude());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        com.google.android.gms.location.LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                try {
                                    getLocationDetails(location.getLatitude(), location.getLongitude());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        client.requestLocationUpdates(locationRequest
                                , locationCallback, Looper.myLooper());
                    }
//                    lat = location.getLatitude();
//                    lon = location.getLongitude();
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }



    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(getContext(), AUTHORITY_NAME, photoFile);
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
                LabelSave(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            File f = new File(getContext().getCacheDir(), photoFileName);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri photoUri = data.getData();
            Bitmap selectedImage = loadFromUri(photoUri);
            LabelSave(selectedImage);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitMapData = bos.toByteArray();

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(photoFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(bitMapData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }


        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoFile = getPhotoFileUri(photoFileName);


        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image", e);
        }
        return image;
    }

    private void savePost(String description, String aboutArt, String location, String date, ParseUser currentUser, File photoFile, String labels) {
        Post post = new Post();
        post.setDescription(description);
        post.setLocation(location);
        post.setAboutArt(aboutArt);
        post.setDateCreated(date);
        post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.setLabels(labels);
//        post.setGeoLocation(currentUserLocation);



        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
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

    public void goToPostsFragment() {
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

    private void LabelSave(Bitmap takenImage) {
        ivPostImage.setImageBitmap(takenImage);
        List<ImageClassifier.Recognition> predictions = imageClassifier.recognizeImage(
                takenImage, 0);

        for (ImageClassifier.Recognition recog : predictions) {
            predictionsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
            if (recog.getName() != null) {
                labels = labels + " " + recog.getName();
            }
        }
        ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, predictionsList);
        lvLabels.setAdapter(predictionsAdapter);
    }

    private void getLocationDetails(Double latitude, Double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if(addresses != null && addresses.size() > 0){
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();

            tvLocation2.setText(city + ", " + state);
        }

    }

}