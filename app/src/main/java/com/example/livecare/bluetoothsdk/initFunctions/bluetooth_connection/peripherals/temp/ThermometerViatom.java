package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp;

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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ThermometerViatom {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public ThermometerViatom(BluetoothConnection bluetoothConnection,BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("0000ffe1")) {
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
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        if (formatHexString.length() == 16) {
            if (!formatHexString.substring(12, 14).equalsIgnoreCase("04")) {
                String temp = new BigInteger(formatHexString.substring(8, 12), 16).toString();
                double temp_celsius;
                temp_celsius = Integer.parseInt(temp) * 0.01;
                double fahrenheit = 32 + (temp_celsius * 9 / 5);
                String valueToSend = new DecimalFormat("##.#").format(fahrenheit);

                String position;
                if (formatHexString.substring(12, 14).equalsIgnoreCase("03")) {
                    position = "Ear";
                } else {
                    position = "Forehead";
                }

                updateTemperatureToFireBase(valueToSend, position);
            }
        }
    }

    private void updateTemperatureToFireBase(String temp, String position) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("temperature", temp);
        objectMap.put("location", position);
        bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Temp.stringValue);
        BleManager.getInstance().disconnect(bleDevice);
    }
}
