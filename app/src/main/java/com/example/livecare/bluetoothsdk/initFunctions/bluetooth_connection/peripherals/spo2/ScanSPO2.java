package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScanSPO2 {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private long lastTime;
    private boolean valueFlag = false;

    public ScanSPO2(BluetoothConnection bluetoothConnection,BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("49535343-fe7d-4ae5-8fa9-9fafd205e455")) {
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
                    public void onNotifySuccess() {}

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String value) {
        if (value.length() == 40) {
            if (value.substring(2, 4).equalsIgnoreCase("00")) {
                //to handle cases when spo2 may be removed from hand and put it back while is listening
                valueFlag = false;
            }

            String hexValue = value.substring(0, 40);
            byte[] data = new byte[hexValue.length() / 2];
            //Walk through the string and parse each octet;
            for (int i = 0; i < hexValue.length(); i += 2) {
                data[i / 2] = (byte) ((Character.digit(hexValue.charAt(i), 16) << 4) + Character.digit(hexValue.charAt(i + 1), 16));
            }
            //search package head
            byte[] result = new byte[5];
            for (int i = 0; i < data.length; i++) {
                if((data[i] & 0x80) > 0 ) {
                    if(i+4 <= data.length){
                        result[0] = data[i];
                        result[1] = data[i+1];
                        result[2] = data[i+2];
                        result[3] = data[i+3];
                        result[4] = data[i+4];
                        break;
                    }else {
                        result[0] = 0;
                        result[1] = 0;
                        result[2] = 0;
                        result[3] = 0;
                        result[4] = 0;
                    }
                }
            }

            int oxygen = result[4];
            int pulseRate = result[3] | ((result[2] & 0x40) << 1);
            int pi = result[0] & 0x0f;

            if (oxygen < 101 && oxygen > 50 && pulseRate < 255 && pulseRate > 0) {
                if (!valueFlag) {
                    valueFlag = true;
                    lastTime = Calendar.getInstance().getTime().getTime();
                }

                if (Calendar.getInstance().getTime().getTime() - lastTime > 10000) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("oxygen", String.valueOf(oxygen));
                    objectMap.put("pulse", String.valueOf(pulseRate));
                    objectMap.put("pi", String.valueOf(pi));
                    bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.SpO2.stringValue, bleDevice.getMac(), bleDevice.getName());
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        }
    }
}