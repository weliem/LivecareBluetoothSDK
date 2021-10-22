package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class O2RingViatom {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicWrite;
    private int tryToGetResults = 0;

    public O2RingViatom(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("14839ac4")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("8b00ace7")) {
                        characteristicWrite = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("0734594a")) {
                        startNotify(characteristic);
                    }
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
                        startWriteCommand(characteristicWrite);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String value = HexUtil.formatHexString(data);
                        if (value.length() > 18) {
                            calculateSpO2Result(value);
                        }
                    }
                });
    }

    private void calculateSpO2Result(String value) {
        if (!value.startsWith("ffff", 14) && !value.startsWith("0000", 14)) {
            String resultOxygen = new BigInteger(value.substring(14, 16), 16).toString();
            String resultPulseRate = new BigInteger(value.substring(16, 18), 16).toString();
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("oxygen", resultOxygen);
            objectMap.put("pulse", resultPulseRate);
            objectMap.put("pi", null);
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.SpO2.stringValue, bleDevice.getMac(), bleDevice.getName());
            BleManager.getInstance().disconnect(bleDevice);
        } else {
            if (tryToGetResults < 5) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> startWriteCommand(characteristicWrite), 5000);
            } else {
                BleManager.getInstance().disconnect(bleDevice);
            }
            tryToGetResults++;
        }
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("aa17E8000000001B"),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

}
