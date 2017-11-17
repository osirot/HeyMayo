package com.example.daniel.heymayo;


import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daniel.heymayo.fragments.PostListFragment;
import com.example.daniel.heymayo.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // variables for map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;


    //variables for carousel
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton FABcreateNewPost;
    private FloatingActionButton FABsubmitPost;
    private EditText mBodyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //connect to google services
        createGoogleApiClient();
        createLocationRequest();


        ///////////carousel on create stuff below here /////////////

        //loads PostListFragment into viewPager on maps activity view
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] { new PostListFragment() };
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
        FABcreateNewPost = (FloatingActionButton) findViewById(R.id.fab_post);
        FABsubmitPost = (FloatingActionButton) findViewById(R.id.fab_submit_request);

        FABcreateNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getParent();
                EditText newHelpRequest = (EditText) findViewById(R.id.new_Post);
                // make createNewPost FAb invisible
                FABcreateNewPost.setVisibility(View.INVISIBLE);
                //when button is clicked make edit text and submitFAb visible
                newHelpRequest.setVisibility(View.VISIBLE);
                FABsubmitPost.setVisibility(View.VISIBLE);

                //after submit is pressed make edit text invisible,
                // also make other fab button invisible
                // and FAB reappear by setting visible

            }
        });

        ////////////end carousel on create stuff here

    }//end on create

    // deprecated; called in MainActivity
    //public void postActivity(View view) {
    //    Intent intent = new Intent(this, SignInActivity.class);
    //    startActivity(intent);
    //}

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
            LatLng myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.setMinZoomPreference(20);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
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

    /////////////carousel code here //////////////



    ///////////end carousel code here/////////////
}
