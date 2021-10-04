package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDataResult;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;

public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;
    private BluetoothConnection bluetoothConnection;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }

    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    public void init(Application app, BluetoothDataResult bluetoothDataResult) {
        Log.d(TAG, "init: ");
        application = app;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();
        bluetoothConnection = new BluetoothConnection(this,bluetoothDataResult,app);
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
        bluetoothConnection.onDestroy();
        if(application!=null){
            application.unregisterReceiver(bluetoothDeviceReceiver);
        }
    }
}
