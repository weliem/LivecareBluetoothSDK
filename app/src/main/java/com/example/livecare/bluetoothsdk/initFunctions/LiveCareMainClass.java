package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDemo;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.BluetoothComponent;
import com.example.livecare.bluetoothsdk.initFunctions.di.component.DaggerBluetoothComponent;
import com.example.livecare.bluetoothsdk.initFunctions.di.module.BluetoothModule;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;

import javax.inject.Inject;

public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;

    private BluetoothComponent bluetoothComponent;

    @Inject
    BluetoothDemo bluetoothDemo;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }

    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    private BluetoothComponent getBluetoothComponent() {
        if (bluetoothComponent == null) {
            bluetoothComponent = DaggerBluetoothComponent.builder()
                    .bluetoothModule(new BluetoothModule(application))
                    .build();
        }
        return bluetoothComponent;
    }

    public void init(Application app) {
        Log.d(TAG, "init: ");
        application = app;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();

        getBluetoothComponent().inject(app);

        //bluetoothDemo.displayBle();
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
