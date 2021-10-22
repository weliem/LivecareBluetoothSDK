package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

public class BP3MW1 {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicWrite;
    private final StringBuilder stringBuilder = new StringBuilder();
    private int resultLength = 0;

    public BP3MW1(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("0000fff1")) {
                    startNotify(characteristic);
                }
                if (characteristic.getUuid().toString().contains("0000fff2")) {
                    characteristicWrite = characteristic;
                }
            }
        }
    }

    private void startWrite(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("4dff0008001501120e2d35ec"),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWrite(characteristicWrite);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String hexResult = HexUtil.formatHexString(data);
                        if (HexUtil.formatHexString(data).startsWith("4d3100")) {
                            String hexResultLength = new BigInteger(hexResult.substring(6, 8), 16).toString();
                            resultLength = (Integer.parseInt(hexResultLength) * 2) + 8;
                        }
                        calculateResults(hexResult, resultLength);
                    }
                });
    }

    private void calculateResults(String hexResult, int resultLength) {
        stringBuilder.append(hexResult);
        if (stringBuilder.length() == resultLength) {

            String result = stringBuilder.substring(stringBuilder.length() - 16, stringBuilder.length());
            if (!result.startsWith("00")) {

                String sys = new BigInteger(result.substring(0, 2), 16).toString();
                String dia = new BigInteger(result.substring(2, 4), 16).toString();
                String pul = new BigInteger(result.substring(4, 6), 16).toString();

                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("sys", sys);
                dataMap.put("dia", dia);
                dataMap.put("pulse", pul);
                dataMap.put("ahr", "");

                bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue, bleDevice.getMac(), bleDevice.getName());
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }
}
