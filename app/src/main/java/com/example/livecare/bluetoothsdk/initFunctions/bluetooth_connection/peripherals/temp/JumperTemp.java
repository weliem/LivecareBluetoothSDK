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

public class JumperTemp {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public JumperTemp(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
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
        if (!formatHexString.startsWith("e0")) {
            String temp_concatenated = new BigInteger(formatHexString.substring(4, 8), 16).toString();
            double temp_celsius;
            temp_celsius = Integer.parseInt(temp_concatenated) * 0.01;
            double fahrenheit = 32 + (temp_celsius * 9 / 5);
            String valueToSend = new DecimalFormat("##.#").format(fahrenheit);

            String position;
            if (new BigInteger(formatHexString.substring(2, 4), 16).toString().equals("33")) {
                position = "Forehead";
            } else {
                position = "Ear";
            }
            updateTemperatureToFireBase(valueToSend, position);
        }
    }

    private void updateTemperatureToFireBase(String temp, String position) {
        Map<String, Object> iHealthData = new HashMap<>();
        iHealthData.put("temperature", temp);
        iHealthData.put("location", position);
        bluetoothConnection.onDataReceived(iHealthData, TypeBleDevices.Temp.stringValue);
        BleManager.getInstance().disconnect(bleDevice);
    }
}
