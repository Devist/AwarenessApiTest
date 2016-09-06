package com.example.prankster.sstest.Tracking;

/**
 * Created by prankster on 2016-09-05.
 */

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.prankster.sstest.BuildConfig;
import com.example.prankster.sstest.DataBase.DBHelper;
import com.example.prankster.sstest.MainActivity;
import com.example.prankster.sstest.R;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


//https://maps.googleapis.com/maps/api/place/autocomplete/xml?input=Amoeba&types=establishment&location=37.76999,-122.44696&radius=500&key=API_KEY
//radius 500 이내의 Amoeba 가 포함된 시설 요청
//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=AddYourOwnKeyHere

public class DetectHospitalService extends Service {
    DBHelper dbHelper;
    Handler innerHandler = new Handler();
    Thread innerThread;
    NotificationManager Notifi_M;
    String[][] hospitalInfo;
    DetectHospitalServiceThread thread;
    Notification Notifi;
    private GoogleApiClient hApiClient;
    private PendingIntent mPendingIntent;
    GetHospitalLocationHttp getHospitalHttp;
    // The fence key is how callback code determines which fence fired.
    private final String FENCE_KEY = "fence_key", TAG = getClass().getSimpleName();
    // The intent action which will be fired when your fence is triggered.
    private final String FENCE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";
    private HospitalFenceReceiver mFenceReceiver;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        if (hApiClient == null) {
            setupService();
            setupClient();
            printMyLocationSnapshot();

        }

        return START_STICKY;
    }

    private void setupService() {

        dbHelper = new DBHelper(getApplicationContext(), "MyInfo.db", null, 1);
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new DetectHospitalServiceThread(handler);
        thread.start();
        getHospitalHttp = new GetHospitalLocationHttp();

        innerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //InputStream is = requestGet(urlPath);
                hospitalInfo = getHospitalHttp.SendByHttp(dbHelper.getLocationLat(), dbHelper.getLocationLng(), 500, "의원");
                Log.d("MYTEST", "병원 정보 받아오는 쓰레드 진입");
                for (int i = 0; i < hospitalInfo[0].length; i++) {
                    Log.d("MYTEST", i + ", " + hospitalInfo[0][i] + ": " + hospitalInfo[1][i] + ", " + hospitalInfo[2][i]);
                }
                innerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setupHospitalFences();
                        notifyFencingHospitalResult();
                    }
                });
            }
        });

    }

    private void setupClient() {
        hApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("MYTEST", "hApiClient에 연결되었습니다.");
                        Intent fenceIntent = new Intent(FENCE_RECEIVER_ACTION);
                        mPendingIntent =
                                PendingIntent.getBroadcast(DetectHospitalService.this, 0, fenceIntent, 0);
                        // The broadcast receiver that will receive intents when a fence is triggered.
                        mFenceReceiver = new DetectHospitalService.HospitalFenceReceiver();
                        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
                        //setupVehicleFences();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        hApiClient.connect();
    }


    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {

            //토스트 띄우기
            Toast.makeText(DetectHospitalService.this, "DetectHospital 서비스 실행중", Toast.LENGTH_LONG).show();
        }
    }


    private void printMyLocationSnapshot() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MYTEST", "내 위치 권한 설정 안됐음");
            return;
        }

        getHospitalHttp = new GetHospitalLocationHttp();
        Awareness.SnapshotApi.getLocation(hApiClient)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        Location myLocation;
                        myLocation = locationResult.getLocation();
                        Log.d("MYTEST", "현재 내 위치 : " + myLocation.getLatitude() + " , " + myLocation.getLongitude());
                        dbHelper.updateLocation(myLocation.getLatitude(), myLocation.getLongitude());
                        innerThread.start();
                    }

                });

    }

    private void notifyFencingHospitalResult() {
        Intent intent2 = new Intent(DetectHospitalService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(DetectHospitalService.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        Notifi = new Notification.Builder(getApplicationContext())
                .setContentTitle("병원펜스 등록됨.")
                .setContentText("펜스가 설정되었습니다")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("알림!")
                .setContentIntent(pendingIntent)
                .build();

        //소리추가
        Notifi.defaults = Notification.DEFAULT_SOUND;

        //알림 소리를 한번만 내도록
        Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        //확인하면 자동으로 알림이 제거 되도록
        Notifi.flags = Notification.FLAG_AUTO_CANCEL;

        Notifi_M.notify(777, Notifi);
    }

    private void setupHospitalFences() {
        //AwarenessFence vehicleFence = DetectedActivityFence.starting(DetectedActivityFence.IN_VEHICLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MYTEST","지오펜싱 관련 권한이 없음");
            return;
        }

        AwarenessFence[] hospitalFence = new AwarenessFence[hospitalInfo[0].length];
        for (int i = 0; i < hospitalInfo[0].length; i++) {
            if(hospitalInfo[0][i]==null)
                break;

            hospitalFence[i] = LocationFence.in(
                    Double.parseDouble(hospitalInfo[1][i]), Double.parseDouble(hospitalInfo[2][i]), 70,10000);
        }
       ;
        // Register the fence to receive callbacks.
        Awareness.FenceApi.updateFences(
                hApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(FENCE_KEY, AwarenessFence.or(hospitalFence), mPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Log.d("MYTEST", "병원 펜스 성공적으로 등록됨");
                        } else {
                            Log.e("MYTEST", "에러. 교통수단 펜스 등록 안됨: " + status);
                        }
                    }
                });
    }


    private void notifyInsuranceResult(String fenceStateStr, int state) {
        Intent intent2 = new Intent(DetectHospitalService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(DetectHospitalService.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        Notifi = new Notification.Builder(getApplicationContext())
                .setContentTitle("병원 업무 내용")
                .setContentText(fenceStateStr)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("알림!")
                .setContentIntent(pendingIntent)
                .build();

        //소리추가
        Notifi.defaults = Notification.DEFAULT_SOUND;
        //알림 소리를 한번만 내도록
        Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
        //확인하면 자동으로 알림이 제거 되도록
        Notifi.flags = Notification.FLAG_AUTO_CANCEL;
        Notifi_M.notify(777, Notifi);
    }

    public class HospitalFenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
//                mLogFragment.getLogView()
//                        .println("Received an unsupported action in FenceReceiver: action="
//                                + intent.getAction());
                return;
            }
            // The state information for the given fence is em
            FenceState fenceState = FenceState.extract(intent);
            Log.d("MYTEST","HospitalFenceReceiver 동작");
            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                String fenceStateStr;
                int state; // 1:TRUE,  2:FALSE , 3:UNKNOWN
                switch (fenceState.getCurrentState()) {

                    case FenceState.TRUE:
                        state = 1;
                        fenceStateStr = "보험처리는 다음과 같이 처리하세요.";
                        break;
                    case FenceState.FALSE:
                        state = 2;
                        fenceStateStr = "병원에서 업무를 보세요.";
                        break;
                    case FenceState.UNKNOWN:
                        state = 3;
                        fenceStateStr = "unknown";
                        break;
                    default:
                        state = 4;
                        fenceStateStr = "unknown value";
                }
                Log.d("MYTEST", fenceStateStr);
                notifyInsuranceResult(fenceStateStr,state);
//
//                //펜스 쿼리 확인
//                queryFence(FENCE_KEY);
            }
        }
    }
}
