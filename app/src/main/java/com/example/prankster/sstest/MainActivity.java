package com.example.prankster.sstest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import logger.LogFragment;

import com.example.prankster.sstest.DataBase.DBHelper;
import com.example.prankster.sstest.Tracking.DetectActivitiesService;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
//    protected FenceReceiver mBroadcastReceiver;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    private GoogleApiClient mApiClient;
    DBHelper dbHelper;
    // The fence key is how callback code determines which fence fired.
    private final String FENCE_KEY = "fence_key", TAG = getClass().getSimpleName();
    private PendingIntent mPendingIntent;
//    private FenceReceiver mFenceReceiver;
    private LogFragment mLogFragment;
    private Button startBtn, stopBtn;
    private FloatingActionButton fab;
    private SupportMapFragment mapFragment;

    // The intent action which will be fired when your fence is triggered.
    private final String FENCE_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";

    private static final int MY_PERMISSION_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setLayout();
    }

    private void setLayout() {
        startBtn    = (Button)findViewById(R.id.buttonStart);
        stopBtn     = (Button)findViewById(R.id.buttonStop);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLogFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        dbHelper = new DBHelper(getApplicationContext(), "MyInfo.db", null, 1);
        Toast.makeText(this,dbHelper.getStatus(),Toast.LENGTH_LONG).show();

        setListener();
    }

    private void setListener(){
        startBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DetectActivitiesService.class);
                //Intent intent = new Intent(MainActivity.this,DetectActivitiesService.class);
                startService(intent);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Service 끝",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,DetectActivitiesService.class);
                stopService(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printSnapshot();
            }
        });
    }

    @Override
    protected void onStop() {
//        if (mFenceReceiver != null) {
//            unregisterReceiver(mFenceReceiver);
//        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        // Unregister the fence:
//        Awareness.FenceApi.updateFences(
//                mApiClient,
//                new FenceUpdateRequest.Builder()
//                        .removeFence(FENCE_KEY)
//                        .build())
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, "Fence was successfully unregistered.");
//                        } else {
//                            Log.e(TAG, "Fence could not be unregistered: " + status);
//                        }
//                    }
//                });
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(37.523722, 126.926929);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        int REQUEST_CODE_LOCATION = 2;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }else{
            map.setMyLocationEnabled(true);
        }
    }


    /**
     * Uses the snapshot API to print out some contextual information the device is "aware" of.
     */
    private void printSnapshot() {
        // Clear the console screen of previous snapshot / fence log data
        mLogFragment.getLogView().setText("");

        Awareness.SnapshotApi.getDetectedActivity(mApiClient)
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                    @Override
                    public void onResult(@NonNull DetectedActivityResult dar) {
                        ActivityRecognitionResult arr = dar.getActivityRecognitionResult();

                        DetectedActivity probableActivity = arr.getMostProbableActivity();

                        // Confidence is an int between 0 and 100.
                        int confidence = probableActivity.getConfidence();
                        String activityStr = probableActivity.toString();
                        mLogFragment.getLogView().println("Activity: " + activityStr
                                + ", Confidence: " + confidence + "/100");
                    }
                });
    }
}