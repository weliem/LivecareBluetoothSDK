package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

public class NoninSpO2 {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private long lastTime = 0;

    public NoninSpO2(BluetoothConnection bluetoothConnection,BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    public void onConnectedSuccess(BluetoothGatt gatt) {
        lastTime = Calendar.getInstance().getTime().getTime();
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("0aad7ea0")) {
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

                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        if (formatHexString.length() == 20 && Calendar.getInstance().getTime().getTime() - lastTime > 20000) {
            String oxygen = new BigInteger(formatHexString.substring(14, 16), 16).toString();
            String pulseRate = new BigInteger(formatHexString.substring(18, 20), 16).toString();
            if (Integer.parseInt(oxygen) < 101 && Integer.parseInt(oxygen) > 0 && Integer.parseInt(pulseRate) < 255 && Integer.parseInt(pulseRate) > 0) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("oxygen", oxygen);
                objectMap.put("pulse", pulseRate);
                objectMap.put("pi", null);
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.SpO2.stringValue, bleDevice.getMac(), bleDevice.getName());
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }
}
