package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDataResult;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ScaleViatom;
import com.example.livecare.bluetoothsdk.initFunctions.data.network.APIClient;
import com.example.livecare.bluetoothsdk.initFunctions.data.local.PrefManager;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_SMG4;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.auth_refresh_token;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.auth_token;

public class LiveCareMainClass {
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
        application = app;
        this.bluetoothDataResult = bluetoothDataResult;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        bluetoothConnection = new BluetoothConnection(this,bluetoothDataResult,app);
        if(PrefManager.getStringValue(auth_token).equals("")){
            authenticateUser();
        }else {
            Utils.startTeleHealthService();
        }
        Utils.startTeleHealthService();
    }

    private void authenticateUser(){
        Call<Object> call = APIClient.getData().authenticateUser("","");
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if(response.isSuccessful()){
                    bluetoothDataResult.authenticationOnSuccess();
                    PrefManager.setStringValue(auth_token,"value");
                    PrefManager.setStringValue(auth_refresh_token,"value");
                    Utils.startTeleHealthService();
                }else {
                    bluetoothDataResult.authenticationOnFailure(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                bluetoothDataResult.authenticationOnFailure(t.getMessage());
            }
        });
    }

    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String deviceName = intent.getStringExtra("deviceName");
            String devicesOrigin = intent.getStringExtra("devicesOrigin");
            BleDevice bleDevice = intent.getExtras().getParcelable("bluetoothDevice");

            switch (Objects.requireNonNull(devicesOrigin)) {
                case "2":
                    bluetoothConnection.addDeviceFromScanning(bleDevice, devicesOrigin,deviceName);
                    break;

                case "3":
                    if(Objects.requireNonNull(bleDevice).getName()!= null){
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
