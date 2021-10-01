package com.example.livecare.bluetoothsdk.initFunctions.utils;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.MyApplication;
import com.example.livecare.bluetoothsdk.initFunctions.service.TeleHealthService;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import java.lang.reflect.Method;
import java.util.Set;
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
        new Handler(Looper.getMainLooper()).postDelayed(Utils::startTeleHealthService, 8000);
    }

    public static void stopTeleHealthService() {
        if (isMyServiceRunning()) {
            Intent intent = new Intent(MyApplication.getInstance(), TeleHealthService.class);
            MyApplication.getInstance().stopService(intent);
        }
    }

    public static void startTeleHealthService() {
        if (!isMyServiceRunning()) {
            Intent startIntentNotification = new Intent(MyApplication.getInstance(), TeleHealthService.class);
            MyApplication.getInstance().startService(startIntentNotification);
        }
    }

    private static boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TeleHealthService.class.getName().equals(service.service.getClassName())) {
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

    public static boolean checkPairedDevices(String deviceMac) {
        Set<BluetoothDevice> pairedDevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                if (device.getAddress().replaceAll("[:]", "").equals(deviceMac) || device.getAddress().equals(deviceMac)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public static void unPairDevice(BluetoothDevice deviceToBoUnpaired) {
        try {
            Method m = deviceToBoUnpaired.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(deviceToBoUnpaired, (Object[]) null);
        } catch (Exception ignored) {}
    }

}
