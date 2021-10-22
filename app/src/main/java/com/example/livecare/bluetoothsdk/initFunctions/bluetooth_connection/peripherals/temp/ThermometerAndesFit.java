package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.temp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;

import java.util.HashMap;
import java.util.Map;

public class ThermometerAndesFit {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public ThermometerAndesFit(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("1809")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("00002a1c")) {
                        startIndicate(characteristic);
                    }
                }
            }
        }
    }

    private void startIndicate(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {

                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        byte[] data = new byte[formatHexString.length() / 2];

        for (int i = 0; i < formatHexString.length(); i += 2) {
            data[i / 2] = (byte) ((Character.digit(formatHexString.charAt(i), 16) << 4)
                    + Character.digit(formatHexString.charAt(i + 1), 16));
        }

        short oct1 = data[1];
        short oct2 = data[2];
        int test = (oct2 << 8) | (oct1 & 0xFF);

        double exponent = Math.pow(10, data[4]);
        double finalTempCelsius = exponent * test;

        double fahrenheit = 32 + (finalTempCelsius * 9 / 5);

        String position;
        if (formatHexString.endsWith("09")) {
            position = "Ear";
        } else {
            position = "Forehead";
        }

        updateTemperatureToFireBase(Double.toString(fahrenheit), position);
    }

    private void updateTemperatureToFireBase(String temp, String position) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("temperature", temp);
        dataMap.put("location", position);
        bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.Temp.stringValue, bleDevice.getMac(), bleDevice.getName());
        BleManager.getInstance().disconnect(bleDevice);
    }
}
