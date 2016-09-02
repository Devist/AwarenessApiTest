package com.example.prankster.sstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import static com.google.android.gms.wearable.DataMap.TAG;

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
