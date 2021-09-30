package com.example.livecare.bluetoothsdk.initFunctions.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import com.example.livecare.bluetoothsdk.MyApplication;
import com.example.livecare.bluetoothsdk.initFunctions.service.TeleHealthService;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;

import static android.bluetooth.BluetoothProfile.GATT;

public class Utils {

    public static boolean isPlugged(Context context) {
        boolean isPlugged;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert intent != null;
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        return isPlugged;
    }

    public static void resetTeleHealthService() {
        stopTeleHealthService();
        new Handler().postDelayed(Utils::startTeleHealthService, 8000);
    }

    public static void stopTeleHealthService() {
        if (isMyServiceRunning(TeleHealthService.class)) {
            Intent intent = new Intent(MyApplication.getInstance(), TeleHealthService.class);
            MyApplication.getInstance().stopService(intent);
        }
    }

    public static void startTeleHealthService() {
        if (!isMyServiceRunning(TeleHealthService.class)) {
            Intent startIntentNotification = new Intent(MyApplication.getInstance(), TeleHealthService.class);
            MyApplication.getInstance().startService(startIntentNotification);
        }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnyBleDeviceConnected() {
        return !BleManager.getInstance().getBluetoothManager().getConnectedDevices(GATT).isEmpty() || Constants.ViatomScaleConnected;
    }

    public static boolean isAnyBleDeviceConnecting() {
        return BleManager.getInstance().getIsConnecting()/* || IHealthConnectedDevices.getInstance().getIsConnecting()*/;
    }

    /*public static boolean isNotForegroundBleDeviceConnected() {
        if(!BleManager.getInstance().getBluetoothManager().getConnectedDevices(GATT).isEmpty()) {
            if(BleManager.getInstance().getBluetoothManager().getConnectedDevices(GATT).size() > IHealthConnectedDevices.getInstance().getAllBackgroundBleMacList().size()){
                return false;
            }else {
                return !Constants.ViatomScaleConnected;
            }
        }else {
            return !Constants.ViatomScaleConnected;
        }
    }*/


}
