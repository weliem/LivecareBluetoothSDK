package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.livecare.bluetoothsdk.MyApplication;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.data.DataManager;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.DaggerLiveCareMainComponent;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.LiveCareMainComponent;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import javax.inject.Inject;

public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;
    private BluetoothConnection bluetoothConnection;

    @Inject
    DataManager mDataManager;

    private LiveCareMainComponent liveCareMainComponent;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }

    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    private LiveCareMainComponent getActivityComponent() {
        if (liveCareMainComponent == null) {
            liveCareMainComponent = DaggerLiveCareMainComponent.builder()
                    .applicationComponent(MyApplication.get(application).getNetComponent())
                    .build();
        }
        return liveCareMainComponent;
    }

    public void init(Application app) {
        Log.d(TAG, "init: ");
        application = app;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();
        getActivityComponent().inject(this);
        bluetoothConnection = new BluetoothConnection();
        //String token = mDataManager.getAccessToken();
        //mDataManager.setMessage();
    }

    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getExtras().getParcelable("bluetoothDevice"));
            Log.d(TAG, "onReceive: "+intent.getStringExtra("devicesOrigin"));
            Log.d(TAG, "onReceive: "+intent.getStringExtra("deviceName"));

            bluetoothConnection.addDeviceFromScanning(intent.getExtras().getParcelable("bluetoothDevice"),
                    intent.getStringExtra("devicesOrigin"),intent.getStringExtra("deviceName"));
        }
    };

    public void destroy(){
        Utils.stopTeleHealthService();
        if(application!=null){
            application.unregisterReceiver(bluetoothDeviceReceiver);
        }
    }
}
