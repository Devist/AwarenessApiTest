package com.example.prankster.sstest.Tracking;

import android.util.Log;

import com.example.prankster.sstest.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by prankster on 2016. 4. 25..
 */
public class GetHospitalLocationHttp {

    // Thread로 웹서버에 접속
    /**
     * 서버에 검색 데이터를 요청하는 메소드
     * @param lat,lng,myRadius,name
     * @return
     */
    public int SendByHttp(String lat, String lng, int myRadius, String name) {
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
        HttpGet get = new HttpGet(URL+"?location="+lat + ","+ lng
                +"&radius="+ myRadius
                +"&name=" + name
                +"&key=" + "AIzaSyCHtm8IJLEqC_o9dxWI3dZkwtn6DsNaWcI");

        Log.d("MYTEST", get.toString());

        DefaultHttpClient client = new DefaultHttpClient();

        try {
             /*    검색할 문자열 서버에 전송       */
             /* 데이터 보낸 뒤 서버에서 데이터를 받아오는 과정 */
            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();
            String jsonString = EntityUtils.toString(resEntity);
            Log.d("json", jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            String code = jsonObject.getString("status");
            Log.d("json", code);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            for(int i = 0 ; i<jsonArray.length();i++){
                Log.d("json",i+"번째 : "+jsonArray.getJSONObject(i).getString("name"));
                Log.d("json",i+"번째 : "+jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                Log.d("json",i+"번째 : "+jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
            }

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MYTEST","ERROR: "+e.toString());
            client.getConnectionManager().shutdown();   // 연결 지연 종료
            return -1;
        }
    }

    private void setJson(HttpResponse response){

    }
}
