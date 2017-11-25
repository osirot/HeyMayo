package com.example.daniel.heymayo;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.heymayo.fragments.PostListFragment;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    private FloatingActionButton firebaseButton;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private static final String TAG = "MapsActivity";
    //for geoFire
    private LatLng myLocation;
    private Circle searchCircle;
    private GeoQuery geoQuery;
    private GeoFire geoFire;
    private Map<String,Marker> markers;
    private static final String GEO_FIRE_DB = "https://heymayo-test.firebaseio.com/";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "locations";

    private FloatingActionButton FABcreateNewPost;
    private FloatingActionButton FABsubmitPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Intent postRequest = new Intent(this, RequestPostActivity.class);

        //connect to google services
        createGoogleApiClient();
        createLocationRequest();

        //loads RequestFragment into viewPager on maps activity view
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] { new RequestFragment() };
            @Override
            public Fragment getItem(int position) { return mFragments[position]; }
            @Override
            public int getCount() { return mFragments.length; }
        };
        // this points program to the right view element
        mViewPager = findViewById(R.id.viewPager);
        // this sets the above adapter to the view pager
        mViewPager.setAdapter(mPagerAdapter);


        //instantiate FAB buttons
        FABcreateNewPost = findViewById(R.id.fab_post);
        FABsubmitPost = findViewById(R.id.fab_submit_request);

        FABcreateNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getParent();
                EditText newHelpRequest = findViewById(R.id.new_Post);
                // make createNewPost FAb invisible
                FABcreateNewPost.setVisibility(View.INVISIBLE);

                startActivity(postRequest);
                finish();

                //when button is clicked make edit text and submitFAb visible
                newHelpRequest.setVisibility(View.VISIBLE);
                FABsubmitPost.setVisibility(View.VISIBLE);

                //after submit is pressed make edit text invisible,
                // also make other fab button invisible
                // and FAB reappear by setting visible

            }
        });
    }

    // deprecated; called in MainActivity
    public void postActivity(View view) {
        Intent intent = new Intent(this, RequestPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle connectionHint){
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        } catch (SecurityException e){
            Log.e("Exception: %s", e.getMessage());
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
        try {
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void updateUI() {
        if (mLastLocation == null){
            startLocationUpdates();
        } else{
            myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.setMinZoomPreference(15);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            this.searchCircle = this.mMap.addCircle(new CircleOptions().center(myLocation).radius(200));
            this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
            this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));

            this.geoFire = new GeoFire(FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(GEO_FIRE_REF));

            GeoLocation INITIAL_CENTER = new GeoLocation(myLocation.latitude, myLocation.longitude);
            this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);
            this.markers = new HashMap<String, Marker>();
            Log.e(TAG, "Value: " + markers);

            //For saving current Lat + Long into pref as strings
            SharedPreferences locationPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = locationPrefs.edit();
            String currentLat = Double.toString(myLocation.latitude);
            String currentLong = Double.toString(myLocation.longitude);
            editor.putString("Latitude", currentLat);
            editor.putString("Longitude", currentLong);
            editor.apply();
        }
    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        showToast("Connection failed.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            showToast("Disconnected. Please re-connect.");
        } else if (i == CAUSE_NETWORK_LOST) {
            showToast("Network lost. Please re-connect.");
        }
    }

    // Create an instance of GoogleAPIClient.
    protected synchronized void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
