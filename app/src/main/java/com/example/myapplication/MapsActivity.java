package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import android.Manifest;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ServiceConnection, SerialListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int MY_LOCATION_REQUEST_CODE = 99;  // Unique request code
    String MAPS_API_KEY = "AIzaSyBfX2PxYcxF3B-i9PNUwR-ocrhDEdD0MnA";
    private Polyline previousPolyline;
    private Marker previousDestinationMarker;
    private LatLng currentDestination;
    private double distance;
    private final int initialZoom = 17;  // the greater this number, the closer the initial zoom



    private BluetoothAdapter bluetoothAdapter;
    final private String deviceAddress = "94:B5:55:23:E9:36";
    private SerialService service;
    private enum Connected { False, Pending, True }
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private double dist2Dest;  // Distance in format "xxx km" or "xx m"


    Handler handler = new Handler();
    //a runnable that once activated measures distance to destination every 2 seconds
    Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("wtf", "happens every 2 seconds");
            if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                            Log.d("wtf", "" + origin.latitude + ", " + origin.longitude);
                            (new FetchDistanceTask()).execute(origin, currentDestination);
                        }
                        else {
                            Log.d("wtf", "location null");
                        }
                    }
                });
            }
            // Repeat this runnable code block again every 2 seconds
            handler.postDelayed(this, 5000);  //TODO: CHANGE THIS BACK TO 2000
        }
    };



    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Toast.makeText(getApplicationContext(), "pressed back lol!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else{
            this.startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
            this.bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        }


    }

    private LatLng getCurrentLocation(){
        final LatLng[] currentLocation = new LatLng[1];
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation[0] = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                    else {
                        Log.d("wtf", "location null");
                    }
                }
            });
        }
        return currentLocation[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.d("wtf", "after bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();");
        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Python things:
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("test");

        PyObject pyObject1 = pyObject.callAttr("will_it_work");

        Toast.makeText(this, pyObject1.toString(), Toast.LENGTH_LONG).show();


        //Search location things:
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), MAPS_API_KEY);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.OPENING_HOURS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("wtf", "Place: " + place.getName() + ", " + place.getLatLng() + ", Openning hours: " + place.getOpeningHours() + ", Phone: " + place.getPhoneNumber());
                onPlaceSelectedOrTouched(place.getLatLng());
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("wtf", "An error occurred: " + status);
            }
        });


        // Check if we have the necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        } else {
            // If permissions are already granted, initialize the map
            initMap();
        }
    }


    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted! Initialize the map.
                initMap();
            } else {
                // Permission was denied. Display an error message, or handle the lack of map functionality gracefully.
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Initialize the map
    private void initMap() {
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                onPlaceSelectedOrTouched(point);
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable the location layer
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);

                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Move the camera to the user's location and zoom in
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), initialZoom));
                                    // todo (Add a marker at the user's location)
//                                    mMap.addMarker(new MarkerOptions()
//                                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
//                                            .title("You are here"));
                                }
                            }
                        });
            } else {
                // Handle the case where you don't have permission.
                //TODO
            }
        } else {
            // Handle the case where you don't have permission.
            //TODO
        }

    }

    private boolean isNetworkAvailable() {  //TODO: Might need modifications to run on Doron's phone
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onPlaceSelectedOrTouched(LatLng point){
        Log.d("wtf", "onPlaceSelectedOrTouched");
         if (!isNetworkAvailable()) {
             Toast.makeText(MapsActivity.this, "You don't have internet connection", Toast.LENGTH_SHORT).show();
             return;
         }
        // Get the current location and create a route
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());

                        // Call a method to create a route
                        currentDestination = point;
                        drawRoutePutMarker(origin, point);
                        //(new FetchDistanceTask()).execute(origin, point);
//                                Toast.makeText(getApplicationContext(), "Distance: "+distance/1000+" km", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d("wtf", "location null");
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            Toast.makeText(MapsActivity.this, "GPS is weak or disabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public class FetchDistanceTask extends AsyncTask<LatLng, Void, Double> {

        private static final String TAG = "FetchDistanceTask";

        @Override
        protected Double doInBackground(LatLng... latLngs) {
            String str_origin = "origins=" + latLngs[0].latitude + "," + latLngs[0].longitude;
            String str_dest = "destinations=" + latLngs[1].latitude + "," + latLngs[1].longitude;
            String sensor = "sensor=false";
            String mode = "mode=walking";
            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/distancematrix/" + output + "?" + parameters + "&key=" + MAPS_API_KEY;
            Log.d("wtf", "distance matrix: " + url);

            try {
                URL urlObject = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection();
                conn.connect();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                JSONObject jsonObject = new JSONObject(result.toString());
                JSONArray array = jsonObject.getJSONArray("rows");
                JSONObject routes = array.getJSONObject(0);
                JSONArray legs = routes.getJSONArray("elements");
                JSONObject steps = legs.getJSONObject(0);
                JSONObject distance = steps.getJSONObject("distance");
                return distance.getDouble("value");  // in meters!!!

            } catch (Exception e) {
                Log.e(TAG, "Error in fetching distance", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Double result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d(TAG, "Distance: " + result);  // This will be distance in km
                dist2Dest = result;
                TextView txtView = findViewById(R.id.simpleTextView);
                txtView.setText("You have " + nicifyDistanceString(dist2Dest) + " left");
            } else {
                Log.d(TAG, "Error in fetching distance");
            }
        }
    }

    /*
    Gets a Double that represents distance in meters and returns a nice string representation o it.
    If the distance is more than 1000m, return in kms. Else, in meters.
    If the distance in meters is not actually an integer - rounds it.
     */
    private String nicifyDistanceString(Double distInMeteres){
        int distRounded = (int)(double)(distInMeteres);
        if (distInMeteres < 1000) {
            return distRounded + " m";
        } else {
            double distanceInKilometers = (distInMeteres / 1000.0);
            return distanceInKilometers + " km";
        }
    }





    private void drawRoutePutMarker(LatLng origin, LatLng destination) {

        this.currentDestination = destination;
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);

        //delete previous routes:
        if (previousPolyline != null)
            previousPolyline.remove();
        if (previousDestinationMarker != null)
            previousDestinationMarker.remove();


        // Add a marker at the clicked location
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(destination);
        markerOptions.title("Destination");
        previousDestinationMarker = mMap.addMarker(markerOptions);

//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, initialZoom));
        //moveCamera(origin, destination);

        drawRoute(origin, destination);

    }

    private void moveCamera(LatLng... places){
        /*
        Move the camera so that all the given places will be visible to the user.
         */
        // Create the builder and add your locations
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng place:
             places) {
            builder.include(place);

        }

        // Create the bounds
        LatLngBounds bounds = builder.build();

        // Create a camera update with a padding of 100 pixels from the edges of the map
        int padding = 250;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        // Move the camera
        mMap.moveCamera(cu);

    }

    private void drawRoute(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);

        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Mode
        String mode = "mode=walking";

        // Key
        String key = "key=" + MAPS_API_KEY;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        service = ((SerialService.SerialBinder) iBinder).getService();
        service.attach(this);
//        if(initialStart && isResumed()) {
        if(initialStart) {
            initialStart = false;
//            this.runOnUiThread(this::connect);
            connect();
        }
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(this.getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void status(String str) {
//        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
//        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        receiveText.append(spn);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        String dataString;
        Log.d("wtf", "in onSerialRead " + (dataString = new String(data)));
        if (0 == dataString.length()){
            Log.d("wtf", "in SerialService onSerialRead(), 0 == dataString.length()");
            return;
        }
        String[] stringAcc = (new String(data)).split(",");
        try {
            float[] acc = new float[stringAcc.length];  // x, y, z, t
            for (int i = 0; i < stringAcc.length; i++) {
                acc[i] = Float.parseFloat(stringAcc[i]);
                Log.d("wtf", acc[i] + "");
            }
        }
       catch (Exception e){
            Log.d("wtf", "onSerialRead Exception occurred!");
        }

    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }


// Fetch


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }

        /** A class to parse the Google Places in JSON format */
        private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

            // Parsing the data in non-ui thread
            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;

                try {
                    jObject = new JSONObject(jsonData[0]);
                    DirectionsJSONParser parser = new DirectionsJSONParser();

                    // Starts parsing data
                    routes = parser.parse(jObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return routes;
            }

            // Executes in UI thread, after the parsing process
            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                // Traversing through all the routes
                for(int i=0;i<result.size();i++){
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.parseColor("#bd34eb"));
                }

                // Drawing polyline in the Google Map for the i-th route
                if(lineOptions != null) {
                    previousPolyline = mMap.addPolyline(lineOptions);
                    moveCamera(points.toArray(new LatLng[0]));
                }

                Toast.makeText(getApplicationContext(), "Distance: "+distance/1000+" km", Toast.LENGTH_SHORT).show();
                //Display in TextView the distance:
                TextView txtView = findViewById(R.id.simpleTextView);
                dist2Dest = distance;
                txtView.setText("You have " + nicifyDistanceString(distance) + " left");
            }
        }

        public class DirectionsJSONParser {

            /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
            public List<List<HashMap<String,String>>> parse(JSONObject jObject){

                List<List<HashMap<String, String>>> routes = new ArrayList<>();
                JSONArray jRoutes;
                JSONArray jLegs;
                JSONArray jSteps;

                try {

                    jRoutes = jObject.getJSONArray("routes");

                    double totalDistance = 0.0;

                    /** Traversing all routes */
                    for(int i=0;i<jRoutes.length();i++){
                        jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                        List<HashMap<String, String>> path = new ArrayList<>();

                        /** Traversing all legs */
                        for(int j=0;j<jLegs.length();j++){
                            jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
                            JSONObject Jdistance = ( (JSONObject)jLegs.get(j)).getJSONObject("distance");
                            totalDistance += Jdistance.getDouble("value");

                            /** Traversing all steps */
                            for(int k=0;k<jSteps.length();k++){
                                String polyline = "";
                                polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                                List<LatLng> list = decodePoly(polyline);

                                /** Traversing all points */
                                for(int l=0;l<list.size();l++){
                                    HashMap<String, String> hm = new HashMap<>();
                                    hm.put("lat", Double.toString((list.get(l)).latitude) );
                                    hm.put("lng", Double.toString((list.get(l)).longitude) );
                                    path.add(hm);

//                                    if (l > 0) {
//                                        LatLng prevPoint = list.get(l - 1);
//                                        LatLng currPoint = list.get(l);
//                                        distance += calculateDistance(prevPoint, currPoint);
//                                    }
                                }
                            }
                            routes.add(path);
                        }

                        distance = totalDistance;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return routes;
            }

            private double calculateDistance(LatLng startPoint, LatLng endPoint) {
                Location location1 = new Location("locationA");
                location1.setLatitude(startPoint.latitude);
                location1.setLongitude(startPoint.longitude);

                Location location2 = new Location("locationB");
                location2.setLatitude(endPoint.latitude);
                location2.setLongitude(endPoint.longitude);

                double distance = location1.distanceTo(location2);
                return distance;
            }


            /**
             * Method to decode polyline points
             * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
             * */
            private List<LatLng> decodePoly(String encoded) {

                List<LatLng> poly = new ArrayList<>();
                int index = 0, len = encoded.length();
                int lat = 0, lng = 0;

                while (index < len) {
                    int b, shift = 0, result = 0;
                    do {
                        b = encoded.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                    } while (b >= 0x20);
                    int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    lat += dlat;

                    shift = 0;
                    result = 0;
                    do {
                        b = encoded.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                    } while (b >= 0x20);
                    int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    lng += dlng;

                    LatLng p = new LatLng((((double) lat / 1E5)),
                            (((double) lng / 1E5)));
                    poly.add(p);
                }

                return poly;
            }
        }

    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an HTTP connection to communicate with the URL
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to the URL
            urlConnection.connect();

            // Reading data from the URL
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception download URL", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }









    private class GeocodeAsyncTask extends AsyncTask<String, Void, List<Address>> {

        private Geocoder geocoder;

        public GeocodeAsyncTask(Context context) {
            geocoder = new Geocoder(context);
        }

        @Override
        protected List<Address> doInBackground(String... locations) {
            List<Address> addressList = null;
            String location = locations[0];

            try {
                addressList = geocoder.getFromLocationName(location, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addressList) {
            // runs from the UI thread
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title("destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, initialZoom));
            } else {
                // Handle case where no location found
            }
        }
    }

}











