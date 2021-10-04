package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

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

public class ScaleAndesFit {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public ScaleAndesFit(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("0000fff3")) {
                        startNotifyScale(characteristic);
                    }
                }
            }
        }
    }

    private void startNotifyScale(BluetoothGattCharacteristic characteristic) {
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
        if (formatHexString.length() == 14) {
            if (formatHexString.substring(8, 10).equalsIgnoreCase("05") || formatHexString.substring(8, 10).equalsIgnoreCase("07")) {// 00000328070002  808
                String weight = new BigInteger(formatHexString.substring(4, 8), 16).toString();
                double value = Integer.parseInt(weight) * 0.2205;
                new DecimalFormat("##.#").format(value);
                updateScaleResultToFireBase(new DecimalFormat("##.#").format(value));
            }
        }
    }

    private void updateScaleResultToFireBase(String weight) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("weight", weight);
        bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.WS.stringValue);
        BleManager.getInstance().disconnect(bleDevice);
    }

}
