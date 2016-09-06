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
//        setClient();
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

//    private void setClient(){
//        Context context = getApplicationContext();
//        mApiClient = new GoogleApiClient.Builder(context)
//                .addApi(Awareness.API)
//                .enableAutoManage(this, 1, null)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@Nullable Bundle bundle) {
//                        // Set up the PendingIntent that will be fired when the fence is triggered.
//                        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
//                        mPendingIntent =
//                                PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
//
//                        // The broadcast receiver that will receive intents when a fence is triggered.
//                        mFenceReceiver = new FenceReceiver();
//                        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
//                        setupVehicleFences();
//                    }
//                    @Override
//                    public void onConnectionSuspended(int i) {
//                    }
//                })
//                .build();
//    }

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

                        // getMostProbableActivity() is good enough for basic Activity detection.
                        // To work within a threshold of confidence,
                        // use ActivityRecognitionResult.getProbableActivities() to get a list of
                        // potential current activities, and check the confidence of each one.
                        DetectedActivity probableActivity = arr.getMostProbableActivity();

                        // Confidence is an int between 0 and 100.
                        int confidence = probableActivity.getConfidence();
                        String activityStr = probableActivity.toString();
                        mLogFragment.getLogView().println("Activity: " + activityStr
                                + ", Confidence: " + confidence + "/100");
                    }
                });

    }


    /**
     * Sets up {@link AwarenessFence}'s for the sample app, and registers callbacks for them
     * with a custom {@link BroadcastReceiver}
     */
//    private void setupVehicleFences() {
//        // DetectedActivityFence will fire when it detects the user performing the specified
//        // activity.  In this case it's walking.
//        //AwarenessFence vehicleFence = DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
//        AwarenessFence vehicleFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);
//        // Register the fence to receive callbacks.
//        Awareness.FenceApi.updateFences(
//                mApiClient,
//                new FenceUpdateRequest.Builder()
//                        .addFence(FENCE_KEY, vehicleFence, mPendingIntent)
//                        .build())
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        if(status.isSuccess()) {
//                            Log.i(TAG, "Vehicle Fence was successfully registered.");
//                        } else {
//                            Log.e(TAG, "Error. VehicleFence could not be registered: " + status);
//                        }
//                    }
//                });
//    }
//    protected void queryFence(final String fenceKey) {
//        Awareness.FenceApi.queryFences(mApiClient,
//                FenceQueryRequest.forFences(Arrays.asList(fenceKey)))
//                .setResultCallback(new ResultCallback<FenceQueryResult>() {
//                    @Override
//                    public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
//                        if (!fenceQueryResult.getStatus().isSuccess()) {
//                            Log.e(TAG, "Could not query fence: " + fenceKey);
//                            return;
//                        }
//                        FenceStateMap map = fenceQueryResult.getFenceStateMap();
//                        for (String fenceKey : map.getFenceKeys()) {
//                            FenceState fenceState = map.getFenceState(fenceKey);
//                            Log.i(TAG, "Fence " + fenceKey + ": "
//                                    + fenceState.getCurrentState()
//                                    + ", was="
//                                    + fenceState.getPreviousState()
//                                    + ", lastUpdateTime="
//                                    + DATE_FORMAT.format(
//                                    String.valueOf(new Date(fenceState.getLastFenceUpdateTimeMillis()))));
//                        }
//                    }
//                });
//    }
    /**
     * A basic BroadcastReceiver to handle intents from from the Awareness API.
     */
//    public class FenceReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
//                mLogFragment.getLogView()
//                        .println("Received an unsupported action in FenceReceiver: action="
//                                + intent.getAction());
//                return;
//            }
//            queryFence(FENCE_KEY);
//            Log.d("되나","된다");
//            // The state information for the given fence is em
//            FenceState fenceState = FenceState.extract(intent);
//            Log.d("확인",fenceState.getFenceKey()+";;;"+ FENCE_KEY);
//            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
//                String fenceStateStr;
//                switch (fenceState.getCurrentState()) {
//
//                    case FenceState.TRUE:
//                        fenceStateStr = "운전중, 또는 대중교통을 이용중입니다. 병원을 찾아가는 중이라고 가정하겠습니다.";
//                        break;
//                    case FenceState.FALSE:
//                        fenceStateStr = "이동을 종료하였습니다. 반경 200m 내의 병원을 펜스로 등록합니다.";
//                        break;
//                    case FenceState.UNKNOWN:
//                        fenceStateStr = "unknown";
//                        break;
//                    default:
//                        fenceStateStr = "unknown value";
//                }
//                mLogFragment.getLogView().println("Fence state: " + fenceStateStr);
//            }
//        }
//    }

}