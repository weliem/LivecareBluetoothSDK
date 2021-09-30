package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;

public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }

    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    public void init(Application app) {
        application = app;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();
    }

    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getExtras().getParcelable("bluetoothDevice"));
            Log.d(TAG, "onReceive: "+intent.getStringExtra("devicesOrigin"));
            Log.d(TAG, "onReceive: "+intent.getStringExtra("deviceName"));
        }
    };

    public void destroy(){
        Utils.stopTeleHealthService();
        if(application!=null){
            application.unregisterReceiver(bluetoothDeviceReceiver);
        }
    }
}
