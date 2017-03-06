package com.example.ankitkumar.lbsee_sbu;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, OnMapReadyCallback, com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private GoogleMap mMap;
    private static final int REQUEST_OK = 1;

    private GoogleApiClient client;
    private ProgressBar spinner;
    boolean markersLoaded = false;
    Location mLastLocation;
    double mylat, mylong;
    LocationRequest mLocationRequest;
    String typeSelected = "";
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    Time mLastUpdateTime;
    public AddressResultReceiver mResultReceiver;
    Polyline mapPolyLine = null;

    private Marker myMarker;

    String addressText = "";
    String address;




    EditText location_tf;

    TextView distdur;

    MarkerOptions currPos = null;


    ArrayList<LatLng> markerPoints;
    public ArrayList<Marker> markers = new ArrayList<Marker>();
    private MarkerManager.Collection mc;





    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextInputLayout ttf = (TextInputLayout) findViewById(R.id.TFaddress);
        location_tf = ttf.getEditText();

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (client == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(AppIndex.API).build();
        }


        if (client != null) {
            client.connect();
        }

        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    client);


            findViewById(R.id.button1).setOnClickListener(this);//voice button
            distdur = location_tf;

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);


            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            fm.getMapAsync(this);




            // Initializing
            markerPoints = new ArrayList<>();

            spinner = (ProgressBar) findViewById(R.id.progressBar);

            assert spinner != null;
            spinner.setVisibility(View.GONE);




        }

    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, (Parcelable) mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }




    private class ReverseGeocodingTask extends AsyncTask<Double, Void, String> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Double... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].doubleValue();
            double longitude = params[1].doubleValue();

            List<Address> addresses = null;


            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            // Setting address of the touched Position
            location_tf.setText(addressText);
        }
    }


        @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client,
                        builder.build());



        // Enable MyLocation Button in the Map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);


            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();

                    if(mapPolyLine != null && !marker.getPosition().equals(currPos.getPosition())) {
                        mapPolyLine.remove();
                    }
                    // Checks, whether start and end locations are captured
                    if (markerPoints.size() >= 1) {
                        LatLng origin = currPos.getPosition();
                        LatLng dest = marker.getPosition();

                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);

                        DownloadTask downloadTask = new DownloadTask();

                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                    //Toast.makeText(getApplicationContext(),"Marker touched",Toast.LENGTH_SHORT).show();
                    return true;
                }
            });


        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already map contain destination location
                if (markerPoints.size() >= 1) {
                    clearMap();
                }

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
               options.position(point);


                double touchLat=point.latitude;
                double touchLong=point.longitude;



                try {
                    Geocoder geo = new Geocoder(MainActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(touchLat,touchLong, 1);
                    if (addresses.isEmpty()) {
                        Toast.makeText(getApplicationContext(),"Waiting for Location",Toast.LENGTH_SHORT).show();
                    }
                    else {

                        if (addresses.size() > 0) {
                            address =addresses.get(0).getFeatureName()
                                    + ", " + addresses.get(0).getLocality()
                                    + ", " + addresses.get(0).getAdminArea()
                                    + ", " + addresses.get(0).getCountryName();
                            Toast.makeText(getApplicationContext(), "Touched Address:- " +address, Toast.LENGTH_LONG).show();
                        }

                        // draws the marker at the currently touched location
                        drawMarker(point,"Your Destination",address+"");

                    }
                }
                catch (Exception e) {
                    e.printStackTrace(); // getFromLocation() may sometimes fail
                }




                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 1) {
                    LatLng origin = currPos.getPosition();
                    LatLng dest = markerPoints.get(0);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
            }


        });
    }



   /* private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            location_tf.setText(locationAddress);
        }

        @Override
        public void close() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void publish(LogRecord record) {

        }
    }*/



    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service

        String url="https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return  url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    /**
     * A class to download data from Google Directions URL
     */
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

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        String distance = "";
        String duration = "";
        Marker marker;

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
            PolylineOptions lineOptions =null;
            MarkerOptions markerOptions = new MarkerOptions();


            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Route Found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);



                // Fetching all the points in i-th route
                for (int j = 0; j < path.size();j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);

            }


           // Toast.makeText(MainActivity.this,"Distance:"+distance+"  , Duration:"+duration,Toast.LENGTH_SHORT).show();



            // Drawing polyline in the Google Map for the i-th route
            mapPolyLine = mMap.addPolyline(lineOptions);



        }
    }

   /* protected float calculateMiles() {
        float totalDistance = 0;

        for(int i = 1; i < markerPoints.size(); i++) {
            Location currLocation = new Location("this");
            currLocation.setLatitude(markerPoints.get(i).latitude);
            currLocation.setLongitude(markerPoints.get(i).longitude);

            Location lastLocation = new Location("this");
            currLocation.setLatitude(markerPoints.get(i-1).latitude);
            currLocation.setLongitude(markerPoints.get(i-1).longitude);

            totalDistance += lastLocation.distanceTo(currLocation);

            Toast.makeText(MainActivity.this,""+totalDistance,Toast.LENGTH_LONG).show();
        }

        return totalDistance;

    }*/


    protected void clearMap(){
        markerPoints.clear();
        mMap.clear();
        mMap.addMarker(currPos);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                client);
        if (mLastLocation != null) {
            mylat = mLastLocation.getLatitude();
            mylong = mLastLocation.getLongitude();
        }


        try {
            Geocoder geo = new Geocoder(MainActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(mylat, mylong, 1);
            if (addresses.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Waiting for Location",Toast.LENGTH_SHORT).show();
            }
            else {

                    if (addresses.size() > 0) {
                        address =addresses.get(0).getFeatureName()
                                + ", " + addresses.get(0).getLocality()
                                + ", " + addresses.get(0).getAdminArea()
                                + ", " + addresses.get(0).getCountryName();
                        Toast.makeText(getApplicationContext(), "Address:- " +address, Toast.LENGTH_LONG).show();
                    }

            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }


        LatLng myPoint = new LatLng(mylat,mylong);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mylat, mylong))      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPoint));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        currPos = new MarkerOptions().title("Your Current Position").position(myPoint)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


        mMap.addMarker(currPos);






        startLocationUpdates();

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }

            /*if (mAddressRequested) {
                startIntentService();
            }*/
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, mLocationRequest, MainActivity.this);
    }




    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mylat = mLastLocation.getLatitude();
        mylong = mLastLocation.getLongitude();
        // Draw the marker, if destination location is not set

        LatLng point = new LatLng(mylat, mylong);
        currPos.position(point);



        //mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {



        if (markerPoints.size()>=1) {
            clearMap();
        }



        int id = item.getItemId();
        String st= (String) item.getTitle();

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading "+st);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;






        // Handle navigation view item clicks here.

        if(id==R.id.home){

            // Restart the Activity
            Intent intent = getIntent();
            finish();
            startActivity(intent);

            spinner.setVisibility(View.INVISIBLE);

        }

        String url="http://chennaiessentials.co.in/getMarkers.php?lat="+mylat+"&long="+mylong;
        if (id == R.id.atm) {
            //Request data
            typeSelected = "atm";
            new JSONTask().execute(url+"&q=atm");
        } else if (id == R.id.bank) {
            typeSelected = "banks";
            new JSONTask().execute(url+"&q=banks");
        } else if (id == R.id.fire) {
            typeSelected = "fire";
            new JSONTask().execute(url+"&q=fire");
        } else if (id == R.id.hospitals) {
            typeSelected = "hospital";
            new JSONTask().execute(url+"&q=hospital");
        } else if (id == R.id.police) {
            typeSelected = "police";
            new JSONTask().execute(url+"&q=police");
        } else if (id == R.id.post) {
            typeSelected = "postoffice";
            new JSONTask().execute(url+"&q=postoffice");
        } else if (id == R.id.college) {
            typeSelected = "colleges";
            new JSONTask().execute(url+"&q=colleges");
        } else if (id == R.id.school) {
            typeSelected = "schools";
            new JSONTask().execute(url+"&q=schools");
        } else if (id == R.id.theater) {
            typeSelected = "theatre";
            new JSONTask().execute(url + "&q=theater");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;



    }




    private void drawMarker(LatLng point, String name, String address){
        markerPoints.add(point);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(point);
        options.title(name);
        options.snippet(address);

        // Add new marker to the Google Map Android API V2
        mMap.addMarker(options);

        MainActivity.this.mMap.animateCamera(CameraUpdateFactory.newLatLng(point));



    }

    //-------change of view of map(sattelite,normal).

    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            location_tf.setText(thingsYouSaid.get(0));
        }
    }

    @Override
    public void onClick(View v) {
        Intent j = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        j.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(j, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }

    }
//---------------------------------------voice to text - end-------------//


    public void onSearch1(View view) throws IllegalArgumentException {

        new CountDownTimer(5000,1000){


            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                progressBar.dismiss();
                //Toast.makeText(getApplication(),"No Places Found",Toast.LENGTH_SHORT).show();

            }
        }.start();



         String location = location_tf.getText().toString().toLowerCase();

        String id=location;

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Searching for "+id);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;

        if (TextUtils.isEmpty(location)) {
            progressBar.dismiss();
            Toast.makeText(getApplicationContext(),"Please Enter Something to Search",Toast.LENGTH_SHORT).show();
            return;
        }

        clearMap();

        String url="http://chennaiessentials.co.in/getMarkers.php?lat="+mylat+"&long="+mylong;
        switch (location) {
            case "atm":
                //Request data
                typeSelected = "atm";
                new JSONTask().execute(url+"&q=atm");
                break;
            case "bank":
                typeSelected = "banks";
                new JSONTask().execute(url+"&q=banks");
                break;
            case "fire":
                typeSelected = "fire";
                new JSONTask().execute(url+"&q=fire");
                break;
            case "hospitals":
                typeSelected = "hospital";
                new JSONTask().execute(url+"&q=hospital");
                break;
            case "police":
                typeSelected = "police";
                new JSONTask().execute(url+"&q=police");
                break;
            case "post":
                typeSelected = "postoffice";
                new JSONTask().execute(url+"&q=postoffice");
                break;
            case "college":
                typeSelected = "colleges";
                new JSONTask().execute(url+"&q=colleges");
                break;
            case "school":
                typeSelected = "schools";
                new JSONTask().execute(url+"&q=schools");
                break;
            case "theater":
                typeSelected = "theater";
                new JSONTask().execute(url+"&q=theater");
                break;
            default:
            {
                List<Address> addressList = null;
                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        try {
                            addressList = geocoder.getFromLocationName(location + ",Chennai,India", 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                    boolean flag=false;
                    for (int i = 0; i < addressList.size(); i++) {

                        MarkerOptions markerOptions = new MarkerOptions();
                        Address address = addressList.get(i);
                        double lat = Double.parseDouble(String.valueOf((address.getLatitude())));
                        double lng = Double.parseDouble(String.valueOf((address.getLongitude())));
                        //String name = (String)hmPlace.get("formatted_address");


                        LatLng latLng = new LatLng(lat, lng);
                        /*markerOptions.position(latLng);
                        markerOptions.title(address.getAddressLine(0));
                        markerOptions.snippet(address.getFeatureName()+","+address.getLocality()+","+address.getAdminArea());
                        MainActivity.this.mMap.addMarker(markerOptions);*/
                        drawMarker(latLng,address.getAddressLine(0),
                                address.getFeatureName()+","+address.getLocality()+","+address.getAdminArea());
                        progressBar.dismiss();

                        if(address.getAddressLine(0).toLowerCase().contains(location.toLowerCase())){
                            flag = true;
                        }

                        if (i == 0) {
                            MainActivity.this.mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                        }

                    }
                    if(!flag){
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(),"No Places Found",Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this.getBaseContext(), "No Place is entered", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }







    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.



        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            if (!isOnline()) {
                showOfflineNotification();
                return null;
            }
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                {
                    assert connection != null;
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) throws NullPointerException {
            super.onPostExecute(result);


            if (result.equals("null")) {
                Context context = getApplicationContext();
                CharSequence text = "No data for this service!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else if (result.isEmpty()) {
                showOfflineNotification();
            } else {
                JSONArray jarr = null;
                try {
                    jarr = new JSONArray(result);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jobb = null;
                assert jarr != null;
                for (int i = 0; i < jarr.length(); i++) {
                    try {
                        jobb = jarr.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    String Na = "", add = "";
                    Double lat = 0.0, lon = 0.0;
                    try {
                        assert jobb != null;
                        Na = jobb.getString("name");
                        lat = jobb.getDouble("lat");
                        lon = jobb.getDouble("long");
                        add = jobb.getString("add");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    //mClusterManager.addItem(new MyItem(lat, lon, Na, add));

                    drawMarker(new LatLng(lat, lon),Na,add);
                    /*
                    Marker mk = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lon))
                            .title(Na)
                            .snippet(add));
                    markers.add(mk);*/
                }
                //mClusterManager.cluster();
                markersLoaded=true;



            }
            //spinner.setVisibility(View.GONE);
           progressBar.dismiss();


        }
    }



    /*

    public void clearVisible(){
        if(markersLoaded) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
    }*/

    // Private class isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }





    @Override
    public void onStart() {

        client.connect();

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }


        @SuppressWarnings("deprecation")
        boolean isEnabled = Settings.System.getInt(this.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        if (isEnabled) {


            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Airplane Mode Is Enabled");
            dialog.setPositiveButton("Disable it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
           /* dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });*/
            AlertDialog alert = dialog.create();
            alert.show();

        } else if (!gps_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS Location Is Disabled");
            dialog.setPositiveButton("Enable it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            /*dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });*/
            dialog.show();
        } else

            // Call isNetworkAvailable class
            if (!isNetworkAvailable()) {
                // Create an Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Set the Alert Dialog Message
                builder.setMessage("Internet Connection Required")
                        .setCancelable(false)
                        .setPositiveButton("Access Internet ",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        // TODO Auto-generated method stub
                                        // Restart the Activity
                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                        startActivity(intent);
                                    }
                                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });

                builder.show();
            }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ankitkumar.lbsee/http/host/path")
        );
        //AppIndex.AppIndexApi.start(client, viewAction);

        super.onStart();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {


        /// ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.ankitkumar.lbsee/http/host/path")
        );
        //AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
    }



    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void showOfflineNotification(){
        Context context = getApplicationContext();
        CharSequence text = "Please enable internet to use!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    public class MyItem implements ClusterItem {

        private final LatLng mPosition;
        private final String Title;
        private final String Snippet;

        public MyItem(double lat, double lng, String ttl, String snpt) {
            mPosition = new LatLng(lat, lng);
            Title = ttl;
            Snippet = snpt;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        public String getSnippet() {
            return Snippet;
        }

        public String getTitle() {
            return Title;
        }
    }

    public class OwnRendring extends DefaultClusterRenderer<MyItem> {

        OwnRendring(Context context, GoogleMap map,
                    ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {

            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }


    }


    public class searcher implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            onSearch(query);
            return true;
        }

        private void onSearch(String query) {
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class AddressResultReceiver {
    }
}



