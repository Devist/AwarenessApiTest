package com.example.prankster.sstest.Tracking;

/**
 * Created by prankster on 2016-09-05.
 */

import android.os.Handler;

public class DetectHospitalServiceThread extends Thread{
    Handler handler;
    boolean isRun = true;
    GetHospitalLocationHttp getHospitalHttp;

    public DetectHospitalServiceThread(Handler handler){
        this.handler = handler;
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run(){
        //반복적으로 수행할 작업을 한다.
        while(isRun){

            try{
                getHospitalHttp = new GetHospitalLocationHttp();
                handler.sendEmptyMessage( getHospitalHttp.SendByHttp("37.507263", "126.940130",300,"의원") );//쓰레드에 있는 핸들러에게 메세지를 보냄

                Thread.sleep(10000); //10초씩 쉰다.
            }catch (Exception e) {}
        }
    }
}