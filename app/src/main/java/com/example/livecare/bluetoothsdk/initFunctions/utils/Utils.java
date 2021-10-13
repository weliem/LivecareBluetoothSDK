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
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import static android.bluetooth.BluetoothProfile.GATT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BC57;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_BEURER_BM67;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BLOOD_PRESSURE_TRANSTEK;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BP_WELLUE;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_BP;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_INDIE_HEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_BlOOD_PRESSURE_TNG_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_ONE_TOUCH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_TRUE_METRIX;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_BERRYMED;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FS2OF1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_FS2OF2;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_MASIMO;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_NONIN;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_PULSE_OXIMETER_TAI_DOC;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_RING_VIATOM;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_FORA;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_INDIE_HEALTH;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_INDIE_HEALTH_SMALL;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SCALE_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_SPIROMETER_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_AET_WD;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_ANDES_FIT;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_JUMPER1;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_TEMP_VICKS;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_FORA_IR20;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_UNAAN;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Constants.BLE_THERMOMETER_VIATOM;

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

    public static boolean isNotOtherBleDeviceConnected() {
        return BleManager.getInstance().getBluetoothManager().getConnectedDevices(GATT).size() == 1 && !Constants.ViatomScaleConnected;
    }

    public static void pair(BluetoothDevice device) {
        device.createBond();
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

    public static void teleHealthScanBroadcastReceiver(boolean startScan) {
        Intent local = new Intent();
        local.setAction("teleHealthScan.BroadcastReceiver");
        local.putExtra("startScan", startScan);
        MyApplication.getInstance().sendBroadcast(local);
    }

    public static void setTimeOnDisconnect(String deviceName) {
        if (deviceName == null) {
            return;
        }
        switch (deviceName) {
            case BLE_PULSE_OXIMETER_BERRYMED:
            case BLE_PULSE_OXIMETER_FS2OF1:
            case BLE_PULSE_OXIMETER_FS2OF2:
            case BLE_PULSE_OXIMETER_ANDES_FIT:
            case BLE_PULSE_OXIMETER_FORA:
            case BLE_PULSE_OXIMETER_MASIMO:
            case BLE_PULSE_OXIMETER_TAI_DOC:
                Constants.currentTimeForLastTelehealthServiceSpO2 = Calendar.getInstance().getTime().getTime();
                break;

            case BLE_SCALE_ANDES_FIT:
            case BLE_SCALE_INDIE_HEALTH:
            case BLE_SCALE_INDIE_HEALTH_SMALL:
            case BLE_SCALE_FORA:
            case BLE_SCALE_JUMPER:
                Constants.currentTimeForLastTelehealthServiceScale = Calendar.getInstance().getTime().getTime();
                break;

            case BLE_TEMP_ANDES_FIT:
            case BLE_TEMP_AET_WD:
            case BLE_THERMOMETER_FORA_IR20:
            case BLE_THERMOMETER_VIATOM:
            case BLE_THERMOMETER_UNAAN:
            case BLE_TEMP_VICKS:
                Constants.currentTimeForLastTelehealthServiceTEMP = Calendar.getInstance().getTime().getTime();
                break;

            case BLE_BlOOD_PRESSURE_BP:
            case BLE_BLOOD_PRESSURE_TRANSTEK:
            case BLE_BlOOD_PRESSURE_ANDES_FIT:
            case BLE_BlOOD_PRESSURE_TNG_FORA:
            case BLE_BlOOD_PRESSURE_INDIE_HEALTH:
            case BLE_BLOOD_PRESSURE_FORA:
            case BLE_BP_WELLUE:
            case BLE_BLOOD_PRESSURE_BEURER_BM67:
            case BLE_BLOOD_PRESSURE_BEURER_BC57:
            case BLE_BLOOD_PRESSURE_CVS:
            case BLE_BLOOD_PRESSURE_JUMPER:
                Constants.currentTimeForLastTelehealthServiceBP = Calendar.getInstance().getTime().getTime();
                break;

            case BLE_GLUCOMETER_TRUE_METRIX_AIR_CVS:
            case BLE_GLUCOMETER_TRUE_METRIX:
                Constants.currentTimeForLastTelehealthServiceGL = Calendar.getInstance().getTime().getTime();
                break;

            case BLE_SPIROMETER_ANDES_FIT:
                Constants.currentTimeForSpirometer = Calendar.getInstance().getTime().getTime();
                break;

            default:
                if (deviceName.contains(BLE_TEMP_JUMPER) || deviceName.contains(BLE_TEMP_JUMPER1)) {
                    Constants.currentTimeForLastTelehealthServiceTEMP = Calendar.getInstance().getTime().getTime();
                } else if (deviceName.contains(BLE_RING_VIATOM)) {
                    Constants.currentTimeForLastTelehealthServiceSpO2 = Calendar.getInstance().getTime().getTime();
                } else if (deviceName.contains(BLE_GLUCOMETER_ONE_TOUCH)) {
                    Constants.currentTimeForLastTelehealthServiceGL = Calendar.getInstance().getTime().getTime();
                } else if (deviceName.startsWith(BLE_PULSE_OXIMETER_NONIN)) {
                    Constants.currentTimeForLastTelehealthServiceSpO2 = Calendar.getInstance().getTime().getTime();
                }
        }
    }

    public static String checkSum(String checksum) {
        int sum = 0;
        char[] value = checksum.toCharArray();

        try {
            for (int i = 0; i < checksum.length(); i += 2) {
                String a = new StringBuilder().append("").append(value[i]).append(value[i + 1]).toString();
                sum += Integer.parseInt(a, 16);
            }
        } catch (Exception ignored) {

        }

        int summod = (sum % 256);
        return checksum + Integer.toHexString(summod & 0xFF);
    }

    public static String addZeroToHex(String str) {
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str;
    }

    public static String convertIntToHex(Calendar cal) {
        String yearFormat = String.valueOf(cal.get(Calendar.YEAR)).substring(2, 4);
        int year = Integer.parseInt(yearFormat);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int newDay = ((year & 0x7f) << 9) | ((month + 1) << 5) | day;
        newDay = (Integer.reverseBytes(newDay) >> 16) & 0xFFFF;
        String returnedValue = Integer.toHexString(newDay);
        if (returnedValue.length() != 4) {
            returnedValue = "0" + returnedValue;
        }
        return returnedValue;
    }

    public static String setDateTimeCommand() {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String sendHour = Integer.toHexString(cal.get(Calendar.HOUR_OF_DAY));
        String sendMin = Integer.toHexString(cal.get(Calendar.MINUTE));

        String setTimeDate = "5133" + convertIntToHex(cal) + addZeroToHex(sendMin) + addZeroToHex(sendHour) + "a3";
        return checkSum(setTimeDate);
    }

    public static  String convertBigEndian(String str) {
        if (str.length() == 3) {
            str = "0" + str;
        }
        return str.substring(2, 4) + str.substring(0, 2);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
