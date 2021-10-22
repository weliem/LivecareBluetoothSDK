package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.wrist;

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

public class FitnessTrackerLintelek {
    private BleDevice bleDevice;
    private final BluetoothConnection bluetoothConnection;
    private BluetoothGattCharacteristic characteristicWrite;
    private Handler mHandler;

    public FitnessTrackerLintelek(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    public void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00000af7")) {
                    startNotify(characteristic);
                }
                if (characteristic.getUuid().toString().contains("00000af6")) {
                    characteristicWrite = characteristic;
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
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String formatHexString = HexUtil.formatHexString(data);
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(() -> startWriteCommand(characteristicWrite), 10000);

                        if(formatHexString.startsWith("02a0")){
                            calculateResult(formatHexString);
                        }
                    }
                });
    }

    private void calculateResult(String formatHexString) {
        String steps = new BigInteger(formatHexString.substring(6, 8) + formatHexString.substring(4, 6), 16).toString();
        String kCal = new BigInteger(formatHexString.substring(14, 16) + formatHexString.substring(12, 14), 16).toString();
        String hr = new BigInteger(formatHexString.substring(34, 38), 16).toString();

        if(!hr.equals("0")){
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("steps", steps);
            objectMap.put("kCal", kCal);
            objectMap.put("hr", hr);
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Fitness.stringValue, bleDevice.getMac(), bleDevice.getName());
            BleManager.getInstance().disconnect(bleDevice);
        }
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("02a0"),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
}
