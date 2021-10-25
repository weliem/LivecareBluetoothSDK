package com.example.livecare.bluetoothsdk.initFunctions;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDataResult;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale.ScaleViatom;
import com.example.livecare.bluetoothsdk.initFunctions.data.model.AuthTokenModel;
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

    public void init(Application app, String key, String secret, BluetoothDataResult bluetoothDataResult) {
        application = app;
        this.bluetoothDataResult = bluetoothDataResult;
        IntentFilter filter = new IntentFilter();
        filter.addAction("update.ui.with.device");
        filter.addAction("update.scanning.stage");
        app.registerReceiver(bluetoothDeviceReceiver, filter);
        bluetoothConnection = new BluetoothConnection(this,bluetoothDataResult,app);
        if(PrefManager.getStringValue(auth_token).equals("")){
            authenticateUser(key,secret);
        }else {
            Utils.startTeleHealthService();
        }
    }

    private void authenticateUser(String key, String secret){
        String secret_key = key + ":" + secret;
        String authHeader = "Basic " + Base64.encodeToString(secret_key.getBytes(), Base64.NO_WRAP);


        Call<AuthTokenModel> call = APIClient.getData().authenticateUser(authHeader,"client_credentials",
                Settings.Secure.getString(application.getContentResolver(), Settings.Secure.ANDROID_ID));
        call.enqueue(new Callback<AuthTokenModel>() {
            @Override
            public void onResponse(@NonNull Call<AuthTokenModel> call, @NonNull Response<AuthTokenModel> response) {
                if(response.isSuccessful()){
                    bluetoothDataResult.authenticationStatus("On Success");
                    assert response.body() != null;
                    PrefManager.setStringValue(auth_token, response.body().getToken());
                    PrefManager.setStringValue(auth_refresh_token,response.body().getRefresh_token());
                    Utils.startTeleHealthService();
                }else {
                    bluetoothDataResult.authenticationStatus("On Error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthTokenModel> call, @NonNull Throwable t) {
                bluetoothDataResult.authenticationStatus(t.getMessage());
            }
        });
    }

    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction()!=null){
                if(intent.getAction().equals("update.ui.with.device")){
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
                } else {
                    bluetoothDataResult.onScanningStatus(intent.getStringExtra("onScan"));
                }
            }




        }
    };

    public void onDataReceived(Map<String, Object> objectMap, String deviceName, String mac, String name) {
        bluetoothDataResult.onDataReceived(objectMap, deviceName);
        bluetoothConnection.onDataReceived(objectMap, deviceName, mac, name);
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
