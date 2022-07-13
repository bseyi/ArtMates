package com.example.test.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.test.JsonParser;
import com.example.test.R;
import com.example.test.activities.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class LocationFragment extends Fragment {
    private Spinner spType;
    private Button btFind;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat = 0, currentLong = 0;
    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;




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
        supportMapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.google_map);

        String[] placeTypeList = {"location_of_all_users ","museum", "exhibition_centers"};
        String[] placeNameList = {"Location of all users", "Museums close to you", "Exhibition Centers close to you"};

        spType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, placeNameList));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = spType.getSelectedItemPosition();
                if (i == 0) {
                    map.clear();
                    getCurrentLocation();
                }
                else {
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
    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            ParseGeoPoint currentUserLocation = new ParseGeoPoint(currentLat, currentLong);

                            ParseUser currentUser = ParseUser.getCurrentUser();
                            currentUser.put("Location", currentUserLocation);
                            currentUser.saveInBackground();
                            showUsers(googleMap);

                            LatLng latlng = new LatLng(currentLat, currentLong);
                            map = googleMap;
                            map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLat, currentLong), 12
                            ));
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    private class PlaceTask extends AsyncTask<String,Integer,String> {

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

        while((line = reader.readLine()) != null){
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        return data;
    }


    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>>  {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String,String>> mapList = null;
            JSONObject object = null;
            try {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.ParseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            map.clear();
            for(int i=0; i<hashMaps.size(); i++){
                HashMap<String,String> hashMapList = hashMaps.get(i);
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng = Double.parseDouble(hashMapList.get("lng"));
                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat,lng);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);

            }
            getCurrentLocation();
        }
    }

    private ParseGeoPoint getParseUserLocation(){

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
        return currentUser.getParseGeoPoint("Location");

    }

    private void showParseUserInMap(final GoogleMap googleMap){

        ParseGeoPoint currentUserLocation = getParseUserLocation();
        LatLng currentUser = new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude());

        googleMap.addMarker(new MarkerOptions().position(currentUser).title("Current Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser, 10));
    }


    private void showUsers(final GoogleMap googleMap){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinMiles("Location", getParseUserLocation(), 1000);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override  public void done(List<ParseUser> nearUsers, ParseException e) {
                if (e == null) {
                    ParseUser users = ParseUser.getCurrentUser();
                    for(int i = 0; i < nearUsers.size(); i++) {
                        if(!nearUsers.get(i).getObjectId().equals(users.getObjectId())) {
                            users = nearUsers.get(i);
                            showParseUserInMap(googleMap);
                            LatLng UserLocation = new LatLng(users.getParseGeoPoint("Location").getLatitude(), users.getParseGeoPoint("Location").getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(UserLocation).title(users.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                        }
                    }
                } else {
                    Log.d("store", "Error: " + e.getMessage());
                }
            }
        });
        ParseQuery.clearAllCachedResults();
    }


}