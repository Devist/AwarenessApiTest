package com.example.prankster.sstest.Tracking;

/**
 * Created by prankster on 2016-09-05.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.prankster.sstest.BuildConfig;
import com.example.prankster.sstest.MainActivity;
import com.example.prankster.sstest.R;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class DetectActivitiesService extends Service implements GoogleApiClient.OnConnectionFailedListener {
    NotificationManager Notifi_M;
    ServiceThread thread;
    Notification Notifi ;
    private GoogleApiClient uApiClient;
    private PendingIntent mPendingIntent;
    private FenceReceiver mFenceReceiver;
    // The fence key is how callback code determines which fence fired.
    private final String FENCE_KEY = "fence_key", TAG = getClass().getSimpleName();

    // The intent action which will be fired when your fence is triggered.
    private final String FENCE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        uApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("MYTEST","ApiClient에 연결되었습니다.");
                        Intent intent2 = new Intent(FENCE_RECEIVER_ACTION);
                        mPendingIntent =
                                PendingIntent.getBroadcast(DetectActivitiesService.this, 0, intent2, 0);
                        // The broadcast receiver that will receive intents when a fence is triggered.
                        mFenceReceiver = new FenceReceiver();
                        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                        setupVehicleFences();
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        uApiClient.connect();
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    /**
     * Sets up {@link AwarenessFence}'s for the sample app, and registers callbacks for them
     * with a custom {@link BroadcastReceiver}
     */
    private void setupVehicleFences() {
        Log.d("여기까지","여기까지");
        // DetectedActivityFence will fire when it detects the user performing the specified
        // activity.  In this case it's walking.
        //AwarenessFence vehicleFence = DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        AwarenessFence vehicleFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);
        // Register the fence to receive callbacks.
        Awareness.FenceApi.updateFences(
                uApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(FENCE_KEY, vehicleFence, mPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Log.d("MYTEST", "Vehicle Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Error. VehicleFence could not be registered: " + status);
                        }
                    }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("fail","fail");
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            //MainActivity.setupVehicleFences();
            FenceQueryResult bb=  new FenceQueryResult() {
                @Override
                public FenceStateMap getFenceStateMap() {
                    //Log.d("여기까지", );
                    return null;
                }

                @Override
                public Status getStatus() {
                    return null;
                }
            };

//            Intent intent = new Intent(DetectActivitiesService.this, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(DetectActivitiesService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//            Notifi = new Notification.Builder(getApplicationContext())
//                    .setContentTitle("Content Title")
//                    .setContentText("Content Text")
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setTicker("알림!!!")
//                    .setContentIntent(pendingIntent)
//                    .build();

            //소리추가
            //Notifi.defaults = Notification.DEFAULT_SOUND;

            //알림 소리를 한번만 내도록
            //Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

            //확인하면 자동으로 알림이 제거 되도록
            //Notifi.flags = Notification.FLAG_AUTO_CANCEL;


            //Notifi_M.notify( 777 , Notifi);

            //토스트 띄우기
            Toast.makeText(DetectActivitiesService.this, "동작중", Toast.LENGTH_LONG).show();
        }
    };

    public class FenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
//                mLogFragment.getLogView()
//                        .println("Received an unsupported action in FenceReceiver: action="
//                                + intent.getAction());
                return;
            }
            Log.d("여기까지","여기까지");
            // The state information for the given fence is em
            FenceState fenceState = FenceState.extract(intent);
            Log.d("확인",fenceState.getFenceKey()+";;;"+ FENCE_KEY);
            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                switch (fenceState.getCurrentState()) {

                    case FenceState.TRUE:
                        fenceStateStr = "운전중, 또는 대중교통을 이용중입니다. 병원을 찾아가는 중이라고 가정하겠습니다.";
                        break;
                    case FenceState.FALSE:
                        fenceStateStr = "이동을 종료하였습니다. 반경 200m 내의 병원을 펜스로 등록합니다.";
                        break;
                    case FenceState.UNKNOWN:
                        fenceStateStr = "unknown";
                        break;
                    default:
                        fenceStateStr = "unknown value";
                }
                            Intent intent2 = new Intent(DetectActivitiesService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(DetectActivitiesService.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                            Notifi = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Content Title")
                    .setContentText(fenceStateStr)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("알림!!!")
                    .setContentIntent(pendingIntent)
                    .build();

                //소리추가
                Notifi.defaults = Notification.DEFAULT_SOUND;

                //알림 소리를 한번만 내도록
                Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

                //확인하면 자동으로 알림이 제거 되도록
                Notifi.flags = Notification.FLAG_AUTO_CANCEL;


                Notifi_M.notify( 777 , Notifi);
                Log.d("MYTEST: " , fenceStateStr);
            }
        }
    }

}
