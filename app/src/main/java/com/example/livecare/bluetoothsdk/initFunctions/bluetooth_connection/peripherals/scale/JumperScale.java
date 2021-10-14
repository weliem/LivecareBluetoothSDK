package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class JumperScale {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicNotify;
    private String valueResults = "";
    private int results = 0;
    private int lastDataRegistered = 0;
    private Handler handler;
    private Runnable runnable;
    private final int delay = 1000;

    public JumperScale(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("0000fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        characteristicNotify = characteristic;
                    }
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
                        checkIfDeviceIsSendingResults();
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        results++;
                        valueResults = HexUtil.formatHexString(data);
                    }
                });
    }

    private void calculateResults() {
        String result = new BigInteger(valueResults.substring(8, 10) + valueResults.substring(6, 8), 16).toString();
        double sendResult = (Double.parseDouble(result) * 0.01);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("weight", sendResult);
        bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
        BleManager.getInstance().disconnect(bleDevice);
    }

    private void checkIfDeviceIsSendingResults() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (lastDataRegistered != 0 && lastDataRegistered == results && (!valueResults.startsWith("00", 6) && !valueResults.startsWith("00", 8))) {
                    calculateResults();
                } else {
                    handler.postDelayed(this, delay);
                }
                lastDataRegistered = results;
            }
        };
        handler.postDelayed(runnable, delay);
    }

    public void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
