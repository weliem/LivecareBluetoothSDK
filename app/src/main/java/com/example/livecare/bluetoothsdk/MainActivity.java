package com.example.livecare.bluetoothsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.LiveCareMainClass;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int MULTIPLE_PERMISSIONS = 10;

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
        if (checkPermissions(permissions, MULTIPLE_PERMISSIONS)) {
            LiveCareMainClass.getInstance().init(getApplication());
            Log.d(TAG, "checkAndRequestForPermission: accepted");
        }
    }

    private boolean checkPermissions(String[] permissions, int MULTIPLE_PERMISSIONS) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults);
        List<String> permissionsDenied = new ArrayList<>();
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (String per : permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied.add(per);
                        }
                    }
                }
            }
        }

        if (permissionsDenied.isEmpty()) {
            Log.d(TAG, "onRequestPermissionsResult: ");
            LiveCareMainClass.getInstance().init(getApplication());
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
