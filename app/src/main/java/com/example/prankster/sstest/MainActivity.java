package com.example.prankster.sstest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import logger.LogFragment;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.Date;

import static android.provider.Settings.System.DATE_FORMAT;
import static com.google.android.gms.wearable.DataMap.TAG;


/**
 * Sample application which sets up a few context fences using the Awareness API, and takes
 * "snapshots" of data about the user and the device's surroundings.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Declare variable for MyFenceReceiver class.
    private MyFenceReceiver mMyFenceReceiver;

    private final String FENCE_KEY = "fence_key";
    private final String TAG = getClass().getSimpleName();
    private LogFragment mLogFragment;

    GoogleApiClient mGoogleApiClient;

    // The intent action which will be fired when your fence is triggered.
    private final String FENCE_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";

    private static final int MY_PERMISSION_LOCATION = 1;

    // Declare variable for PendingIntent
    private PendingIntent mPendingIntent;

    double currentLocationLat;  // current location latitude
    double currentLocationLong;  // current location longitude
    // Create the primitive fences.
    AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
    AwarenessFence vehicleFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);
    AwarenessFence inHospitalFence = AwarenessFence.and(LocationFence.in(36,128,1L,3000));
    AwarenessFence exitHospitalFence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mMyFenceReceiver = new MyFenceReceiver();
        registerReceiver(mMyFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));

        Button checkBtn = (Button)findViewById(R.id.buttonCheck);
        checkBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //지도 띄우기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogFragment =
                (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);

        // Clear the console screen of previous snapshot / fence log data
        mLogFragment.getLogView().setText("");

       // mLogFragment.getLogView().println("Activity: " + activityStr
         //       + ", Confidence: " + confidence + "/100");
    }


    //펜스 등록
    protected void registerFence(String fenceKey, AwarenessFence fence) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, mPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                            queryFence(fenceKey);
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }
    //펜스 해제
    protected void unregisterFence(final String fenceKey) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + fenceKey + " could NOT be removed.");
            }
        });
    }

    // Extend the BroadcastReceiver class.
    public class MyFenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), "headphoneFence")) {
                switch(fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        Log.i(TAG, "Headphones are plugged in.");
                        break;
                    case FenceState.FALSE:
                        Log.i(TAG, "Headphones are NOT plugged in.");
                        break;
                    case FenceState.UNKNOWN:
                        Log.i(TAG, "The headphone fence is in an unknown state.");
                        break;
                }
            }
        }
    }

    protected void queryFence(final String fenceKey) {
        Awareness.FenceApi.queryFences(mGoogleApiClient,
                FenceQueryRequest.forFences(Arrays.asList(fenceKey)))
                .setResultCallback(new ResultCallback<FenceQueryResult>() {
                    @Override
                    public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
                        if (!fenceQueryResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not query fence: " + fenceKey);
                            return;
                        }
                        FenceStateMap map = fenceQueryResult.getFenceStateMap();
                        for (String fenceKey : map.getFenceKeys()) {
                            FenceState fenceState = map.getFenceState(fenceKey);
                            Log.i(TAG, "Fence " + fenceKey + ": "
                                    + fenceState.getCurrentState()
                                    + ", was="
                                    + fenceState.getPreviousState()
                                    + ", lastUpdateTime="
                                    + DATE_FORMAT.format(
                                    String.valueOf(new Date(fenceState.getLastFenceUpdateTimeMillis()))));
                        }
                    }
                });
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


    private boolean checkAndRequestWeatherPermissions() {
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION
                );
            } else {
                Log.i(TAG, "Permission previously denied and app shouldn't ask again.  Skipping" +
                        " weather snapshot.");
            }
            return false;

        } else {
            return true;
        }
    }

}

//import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.google.android.gms.awareness.Awareness;
//import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.location.ActivityRecognitionResult;
//import com.google.android.gms.location.DetectedActivity;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//       Context context = this;
//        GoogleApiClient client = new GoogleApiClient.Builder(context)
//                .addApi(Awareness.API)
//                .build();
//
//        client.connect();
//
//        Awareness.SnapshotApi.getDetectedActivity(client)
//                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
//                    @Override
//                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
//                        if (!detectedActivityResult.getStatus().isSuccess()) {
//                            Log.d("Sorry", "Could not get the current activity.");
//                            return;
//                        }
//                        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
//                        DetectedActivity probableActivity = ar.getMostProbableActivity();
//                        Log.d("Good", probableActivity.toString());
//                    }
//                });
//
//    }
//}
