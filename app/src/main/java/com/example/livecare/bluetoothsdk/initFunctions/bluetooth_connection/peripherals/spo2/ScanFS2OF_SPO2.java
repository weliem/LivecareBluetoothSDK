package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScanFS2OF_SPO2 {
    private final String TAG = "ScanFS2OF_SPO2";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private final String deviceName;
    private long lastTime = 0;

    public ScanFS2OF_SPO2(BluetoothConnection bluetoothConnection, String deviceName) {
        this.bluetoothConnection = bluetoothConnection;
        this.deviceName = deviceName;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        lastTime = Calendar.getInstance().getTime().getTime();
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("ffe4")) {
                    startNotify(characteristic);
                }
            }
        }
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        Log.d(TAG, "onNotifySuccess: ");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        Log.d(TAG, "onNotifyFailure: ");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        if (formatHexString.length() > 32 && Calendar.getInstance().getTime().getTime() - lastTime > 20000) {
            String pulseRate = new BigInteger(formatHexString.substring(22, 26), 16).toString();
            String oxygen = new BigInteger(formatHexString.substring(26, 28), 16).toString();
            String pi_result = new BigInteger(formatHexString.substring(28, 32), 16).toString();
            double pi = Integer.parseInt(pi_result) * 0.001;

            if (Integer.parseInt(oxygen) < 101 && Integer.parseInt(oxygen) > 0 && Integer.parseInt(pulseRate) < 301 && Integer.parseInt(pulseRate) > 0) {
                Map<String, Object> iHealthData = new HashMap<>();
                iHealthData.put("oxygen", oxygen);
                iHealthData.put("pulse", pulseRate);
                iHealthData.put("pi", String.valueOf(pi));
                Log.d(TAG, "calculateResults on data received: "+formatHexString);
                bluetoothConnection.onDataReceived(iHealthData, TypeBleDevices.SpO2.stringValue);
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }
}
