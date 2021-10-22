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

public class VicksTemp {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public VicksTemp(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
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
        if(formatHexString!=null && formatHexString.length() == 12){
            Map<String, Object> objectMap = new HashMap<>();
            String newHex = formatHexString.substring(2,10);
            int numBitsReversed =  Integer.reverseBytes(Integer.parseInt(newHex,16));
            float f = Float.intBitsToFloat(numBitsReversed);
            if(f<30) {
                objectMap.put("error", "Error value");
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Temp.stringValue, bleDevice.getMac(), bleDevice.getName());
                return;
            }
            if(f > 30 && f < 50){
                f = 32 + (f * 9 / 5);
            }
            objectMap.put("temperature", f);
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Temp.stringValue, bleDevice.getMac(), bleDevice.getName());
        }
    }

}