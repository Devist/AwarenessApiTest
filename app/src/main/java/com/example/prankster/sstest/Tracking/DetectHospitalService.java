package com.example.prankster.sstest.Tracking;

/**
 * Created by prankster on 2016-09-05.
 */

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.prankster.sstest.BuildConfig;
import com.example.prankster.sstest.MainActivity;
import com.example.prankster.sstest.R;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;


//https://maps.googleapis.com/maps/api/place/autocomplete/xml?input=Amoeba&types=establishment&location=37.76999,-122.44696&radius=500&key=API_KEY
//radius 500 이내의 Amoeba 가 포함된 시설 요청
//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=AddYourOwnKeyHere

public class DetectHospitalService extends Service {
    NotificationManager Notifi_M;
    DetectHospitalServiceThread thread;
    Notification Notifi;
    private GoogleApiClient uApiClient;
    private PendingIntent mPendingIntent;
    //GetHospitalLocationHttp getHospitalHttp;
    private DetectActivitiesService.FenceReceiver mFenceReceiver;
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
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        //getHospitalHttp = new GetHospitalLocationHttp();
        //getHospitalHttp.SendByHttp("37.523759","126.926909",1000,"의원");
        thread = new DetectHospitalServiceThread(handler);
        thread.start();
        setupClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
        }
        printSnapshot();
        return START_STICKY;
    }

    private void setupClient() {
        uApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("MYTEST", "ApiClient에 연결되었습니다.");
                        //Intent intent2 = new Intent(FENCE_RECEIVER_ACTION);
                        //mPendingIntent =
                        //        PendingIntent.getBroadcast(DetectHospitalService.this, 0, intent2, 0);
                        // The broadcast receiver that will receive intents when a fence is triggered.
                        //mFenceReceiver = new DetectActivitiesService.FenceReceiver();
                        //registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                        //setupVehicleFences();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        uApiClient.connect();
    }
    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(DetectHospitalService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(DetectHospitalService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notifi = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Content Title")
                    .setContentText("Content Text")
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


            Notifi_M.notify(777, Notifi);

            //토스트 띄우기
            Toast.makeText(DetectHospitalService.this, "뜸?", Toast.LENGTH_LONG).show();
        }
    }

    ;


    private void printSnapshot() {
        // Clear the console screen of previous snapshot / fence log data

        // Each type of contextual information in the snapshot API has a corresponding "get" method.
        //  For instance, this is how to get the user's current Activity.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTEST", "내 위치 권한 설정 안됐음");
            return;
        }
        Awareness.SnapshotApi.getLocation(uApiClient)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        Location myLocation = locationResult.getLocation();
                        Log.d("MYTEST","현재 내 위치 : " + myLocation.getLatitude() + " , "+ myLocation.getLongitude());
                    }

                });
    }
}
