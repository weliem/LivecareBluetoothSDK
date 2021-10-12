package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDataResult;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ScaleViatom;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.Map;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_SMG4;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_VIATOM;

public class LiveCareMainClass {
    private String TAG = "LiveCareMainClass";
    private Application application;
    private BluetoothConnection bluetoothConnection;
    private ScaleViatom scaleViatom;
    private BluetoothDataResult bluetoothDataResult;

    public static LiveCareMainClass getInstance() {
        return LiveCareHolder.liveCareMainClass;
    }


    private static class LiveCareHolder {
        private static final LiveCareMainClass liveCareMainClass = new LiveCareMainClass();
    }

    public void init(Application app, BluetoothDataResult bluetoothDataResult) {
        Log.d(TAG, "init: ");
        application = app;
        this.bluetoothDataResult = bluetoothDataResult;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        Utils.startTeleHealthService();
        bluetoothConnection = new BluetoothConnection(this,bluetoothDataResult,app);
    }

    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String deviceName = intent.getStringExtra("deviceName");
            String devicesOrigin = intent.getStringExtra("devicesOrigin");
            BleDevice bleDevice = intent.getExtras().getParcelable("bluetoothDevice");

            switch (devicesOrigin) {
                case "2":
                    bluetoothConnection.addDeviceFromScanning(intent.getExtras().getParcelable("bluetoothDevice"),
                            intent.getStringExtra("devicesOrigin"),intent.getStringExtra("deviceName"));
                    break;

                case "3":
                    if(bleDevice.getName()!= null){
                        if (bleDevice.getName().equals(BLE_SCALE_VIATOM) || bleDevice.getName().equals(BLE_SCALE_SMG4)) {
                            scaleViatom = new ScaleViatom(LiveCareMainClass.this,  bleDevice);
                            break;
                        }
                    }
                    break;
            }
        }
    };

    public void onDataReceived(Map<String, Object> objectMap, String deviceName) {
        bluetoothDataResult.onDataReceived(objectMap, deviceName);
    }

    public void destroy(){
        Utils.stopTeleHealthService();
        bluetoothConnection.onDestroy();
        if(application!=null){
            application.unregisterReceiver(bluetoothDeviceReceiver);
        }
        if (scaleViatom != null) {
            scaleViatom.onDestroy();
            scaleViatom = null;
        }
    }
}
