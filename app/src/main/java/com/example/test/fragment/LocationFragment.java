package com.example.test.fragment;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.test.JsonParser;
import com.example.test.Post;
import com.example.test.R;
import com.example.test.activities.DetailsActivity;
import com.example.test.activities.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LocationFragment extends Fragment {
    private static final int COMPRESSION_QUALITY = 50;
    private static final int BITMAP_SIZE = 200;
    private static final int REQUEST_CODE = 44;

    private static final String TAG = "LocationFragment";
    private final int MAX_RADIUS = 25000;
    private final int MIN_RADIUS = 2000;
    private Spinner spType;
    private Button btFind;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat = 0, currentLong = 0;
    private ImageButton btnUp;
    private ImageButton btnDown;
    private ImageButton btnCurrentLoc;
    private int radius = 10000;
    private Circle circle;
    private SearchView idSearchView;
    private LatLng currentLocLatLng;


    public LocationFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spType = view.findViewById(R.id.sp_type);
        btFind = view.findViewById(R.id.bt_find);
        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        idSearchView = view.findViewById(R.id.idSearchView);
        btnCurrentLoc = view.findViewById(R.id.btnCurrentLoc);
        supportMapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);

        String[] placeTypeList = {"location_of_all_users ", "posts", "museum", "exhibition_centers"};
        String[] placeNameList = {"Location of all users", "Posts", "Museums close to you", "Exhibition Centers close to you"};

        spType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, placeNameList));

        if (permission()){
            getCurrentLocation();
        }

        idSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String label = idSearchView.getQuery().toString();
                showPostLabels(map, label);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = spType.getSelectedItemPosition();
                if (i == 0) {
                    map.clear();
                    btnDown.setVisibility(View.INVISIBLE);
                    btnUp.setVisibility(View.INVISIBLE);
                    getCurrentLocation();
                } else if (i == 1) {
                    btnDown.setVisibility(View.VISIBLE);
                    btnUp.setVisibility(View.VISIBLE);
                    idSearchView.setVisibility(View.VISIBLE);
                    map.clear();
                    showPosts(map);
                }
                else {
                    idSearchView.setVisibility(View.INVISIBLE);
                    btnDown.setVisibility(View.INVISIBLE);
                    btnUp.setVisibility(View.INVISIBLE);

                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?location=" + currentLat + "," + currentLong +
                            "&radius=5000" +
                            "&types=" + placeTypeList[i] +
                            "&sensor=true" +
                            "&key=" + getResources().getString(R.string.google_maps_key);

                    new PlaceTask().execute(url);
                }
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radius != MAX_RADIUS) {
                    radius = radius + 2000;
                }
                showPosts(map);
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radius != MIN_RADIUS) {
                    radius = radius - 2000;
                }
                showPosts(map);
            }
        });

        btnCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCamera(currentLocLatLng, map);
            }
        });

    }

    private boolean permission(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (permission()){
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }

            }).addOnSuccessListener(location -> {
                Location currentLocation = location;
                if (currentLocation != null){
                    currentLat = currentLocation.getLatitude();
                    currentLong = currentLocation.getLongitude();

                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            ParseGeoPoint currentUserLocation = new ParseGeoPoint(currentLat, currentLong);

                            ParseUser currentUser = ParseUser.getCurrentUser();
                            currentUser.put("Location", currentUserLocation);
                            currentUser.saveInBackground();
                            showUsers(googleMap);

                            LatLng latlng = new LatLng(currentLat, currentLong);
                            currentLocLatLng = latlng;
                            map = googleMap;
                            moveCamera(latlng, map);
                        }
                    });
                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = null;

            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        return data;
    }


    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = null;
            JSONObject object = null;
            try {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            map.clear();
            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMapList = hashMaps.get(i);
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng = Double.parseDouble(hashMapList.get("lng"));
                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat, lng);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);

            }
            getCurrentLocation();
        }
    }

    private ParseGeoPoint getParseUserLocation() {

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
        return currentUser.getParseGeoPoint("Location");

    }

    private void showParseUserInMap(final GoogleMap googleMap) {

        ParseGeoPoint currentUserLocation = getParseUserLocation();
        LatLng currentUser = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());

        googleMap.addMarker(new MarkerOptions().position(currentUser).title("Current Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser, 10));
    }


    private void showUsers(final GoogleMap googleMap) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles("Location", getParseUserLocation(), 1000);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> nearUsers, ParseException e) {
                if (e == null) {
                    ParseUser users = ParseUser.getCurrentUser();
                    for (int i = 0; i < nearUsers.size(); i++) {
                        if (!nearUsers.get(i).getObjectId().equals(users.getObjectId())) {
                            users = nearUsers.get(i);
                            showParseUserInMap(googleMap);
                            LatLng userLocation = new LatLng(users.getParseGeoPoint("Location").getLatitude(), users.getParseGeoPoint("Location").getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(userLocation).title(users.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        }
                    }
                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });
        ParseQuery.clearAllCachedResults();
    }


    private void showPosts(final GoogleMap googleMap) {
        googleMap.clear();

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint userLocation = currentUser.getParseGeoPoint("Location");
        LatLng userLoc = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    addMarker(post, googleMap, userLocation);
                }
                addCircle(googleMap, userLoc);
            }
        });
        markCurrentLocation(googleMap, userLoc);
    }

    private boolean isInMaxRadius(ParseGeoPoint currentUser, ParseGeoPoint post, int radius) {
        double distance = distance(currentUser.getLatitude(), currentUser.getLongitude(), post.getLatitude(), post.getLongitude());
        if (distance > radius / 2) {
            return false;
        }
        return true;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void showPostLabels(final GoogleMap googleMap, String label) {
        googleMap.clear();

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint userLocation = currentUser.getParseGeoPoint("Location");
        LatLng userLoc = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    if (post.getLabels().toLowerCase().contains(label)) {
                        addMarker(post, googleMap, userLocation);
                    }

                }
            }
        });
        markCurrentLocation(googleMap, userLoc);
        addCircle(googleMap, userLoc);
    }

    private void addMarker(Post post, GoogleMap googleMap, ParseGeoPoint userLocation) {
        ParseGeoPoint postLocation = post.getGeoLocation();
        if (postLocation != null) {
            LatLng currentPost = new LatLng(postLocation.getLatitude(), postLocation.getLongitude());
            ParseFile image = post.getImage();
            image.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null && data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
                        final byte[] b = baos.toByteArray();

                        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(getCroppedBitmap(bitmap), BITMAP_SIZE, BITMAP_SIZE);

                        Marker newMarker = googleMap.addMarker(new MarkerOptions()
                                .title("Posts")
                                .icon(BitmapDescriptorFactory.fromBitmap(thumbnail))
                                .position(currentPost)
                        );
                        if(isInMaxRadius(userLocation, postLocation, radius)) {
                            newMarker.setVisible(true);
                        }
                        else {
                            newMarker.setVisible(false);
                        }
                        newMarker.setTag(post);
                        markerListener(googleMap);
                    }
                }
            });
        }
    }

    private void moveCamera(LatLng latLng, GoogleMap googleMap){
        latLng = new LatLng(currentLat, currentLong);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentLat, currentLong), 12
        ));
    }

    private void addCircle(GoogleMap googleMap, LatLng userLoc){
        circle = googleMap.addCircle(new CircleOptions()
                .center(userLoc)
                .radius(radius)
                .strokeColor(Color.rgb(0, 136, 255))
                .fillColor(Color.argb(20, 0, 136, 255)));
    }

    private void markCurrentLocation(GoogleMap googleMap, LatLng userLoc){
        googleMap.addMarker(new MarkerOptions().position(userLoc).title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    }

    private void markerListener(GoogleMap googleMap){
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                marker.getTag();
                Post post = (Post) marker.getTag();
                intent.putExtra("posts", Parcels.wrap(post));
                startActivity(intent);
                return true;
            }
        });
    }

}