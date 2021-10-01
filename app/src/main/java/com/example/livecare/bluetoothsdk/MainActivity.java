package com.example.livecare.bluetoothsdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothDataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestForPermission();
    }

    public void checkAndRequestForPermission() {
        String[] permissions = new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (checkPermissions(permissions)) {
            Log.d(TAG, "checkAndRequestForPermission: accepted");
            initProcess();
        }
    }

    private void initProcess() {
        LiveCareMainClass.getInstance().init(getApplication(),new BluetoothDataResult() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect: ");
            }

            @Override
            public void OnConnectedSuccess(String deviceName) {
                Log.d(TAG, "OnConnectedSuccess: "+deviceName);
            }

            @Override
            public void OnConnectFail(String message) {
                Log.d(TAG, "OnConnectFail: "+message);
            }

            @Override
            public void onDisConnected(String deviceName) {
                Log.d(TAG, "onDisConnected: "+deviceName);
            }

            @Override
            public void onDataReceived(Map<String, Object> data, String deviceName) {
                switch (deviceName) {
                    case "SpO2":
                        Log.d(TAG, "onDataReceived: "+data.toString());
                        break;

                    case "BP":
                        Log.d(TAG, "onDataReceived: ");
                        break;
                }
            }
        });
    }

    private boolean checkPermissions(String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 10);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissionsList, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults);
        List<String> permissionsDenied = new ArrayList<>();
        int MULTIPLE_PERMISSIONS = 10;
        if (requestCode == MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (String per : permissionsList) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        permissionsDenied.add(per);
                    }
                }
            }
        }

        if (permissionsDenied.isEmpty()) {
            Log.d(TAG, "onRequestPermissionsResult: ");
            initProcess();
        } else {
            checkAndRequestForPermission();
        }
    }


    @Override
    protected void onDestroy() {
        LiveCareMainClass.getInstance().destroy();
        super.onDestroy();
    }
}
