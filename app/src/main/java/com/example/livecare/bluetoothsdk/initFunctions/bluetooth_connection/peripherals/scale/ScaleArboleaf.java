package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.util.HashMap;
import java.util.Map;

public class ScaleArboleaf {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicNotify;
    private BluetoothGattCharacteristic characteristicIndicate;
    private BluetoothGattCharacteristic characteristicWriteffe3;

    public ScaleArboleaf(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("0000ffe1")) {
                    characteristicNotify = characteristic;
                } else if (characteristic.getUuid().toString().contains("0000ffe2")) {
                    characteristicIndicate = characteristic;
                } else if (characteristic.getUuid().toString().contains("0000ffe3")) {
                    characteristicWriteffe3 = characteristic;
                }
            }
        }
        startNotify(characteristicNotify);
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startIndicate(characteristicIndicate);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResult(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResult(String formatHexString) {
        if (formatHexString.startsWith("100b15")) {
            String hexWeight = formatHexString.substring(6, 10);
            int weight = Integer.parseInt(hexWeight, 16);
            double weightLbs = (weight * 2.20462);
            if (formatHexString.startsWith("01", 10)) {
                startWriteCommand(characteristicWriteffe3, "1f05151049");
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("weight", weightLbs / 100);
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
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
                        startWriteCommand(characteristicWriteffe3, "1309150210aa21000e");
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        BleManager.getInstance().disconnect(bleDevice);
                    }
                });
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
}
