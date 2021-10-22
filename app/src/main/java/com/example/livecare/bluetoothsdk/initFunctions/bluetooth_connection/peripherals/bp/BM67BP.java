package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class BM67BP {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String resultSys = "";
    private String resultDia = "";
    private String resultPulse = "";

    public BM67BP(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00002a35")) {
                    startIndicate(characteristic);
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
        String convertResultSys = formatHexString.substring(4, 6) + formatHexString.substring(2, 4);
        String convertResultDia = formatHexString.substring(8, 10) + formatHexString.substring(6, 8);
        String convertResultPulse = formatHexString.substring(30, 32) + formatHexString.substring(28, 30);
        resultSys = new BigInteger(convertResultSys, 16).toString();
        resultDia = new BigInteger(convertResultDia, 16).toString();
        resultPulse = new BigInteger(convertResultPulse, 16).toString();
    }

    public void onDisConnected() {
        if (!resultSys.equalsIgnoreCase("")) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("sys", resultSys);
            objectMap.put("dia", resultDia);
            objectMap.put("pulse", resultPulse);
            objectMap.put("ahr", "");
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
        }
    }
}
