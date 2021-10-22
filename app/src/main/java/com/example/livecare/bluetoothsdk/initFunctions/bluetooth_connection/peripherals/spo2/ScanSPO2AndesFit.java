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
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScanSPO2AndesFit {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private long lastTime;
    private boolean valueFlag = false;

    public ScanSPO2AndesFit(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("cdeacb80-5235-4c07-8846-93a37ee6b86d")) {
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

    private void calculateResults(String value) {
        if (value.length() == 8 && value.substring(0, 2).equalsIgnoreCase("81")) {
            if (value.substring(2, 4).equalsIgnoreCase("ff")) {
                //to handle cases when spo2 may be removed from hand and put it back while is listening
                valueFlag = false;
            }

            String oxygen = new BigInteger(value.substring(4, 6), 16).toString();
            String pulseRate = new BigInteger(value.substring(2, 4), 16).toString();
            String pi = new BigInteger(value.substring(6, 8), 16).toString();
            double piConverted = (Integer.parseInt(pi) * 0.1);

            if (Integer.parseInt(oxygen) < 101 && Integer.parseInt(oxygen) > 35 && Integer.parseInt(pulseRate) < 255 && piConverted < 20) {

                if (!valueFlag) {
                    valueFlag = true;
                    lastTime = Calendar.getInstance().getTime().getTime();
                }
                //value measured 5 times is a requirement to get values with a delay so they can be more accurate
                if (Calendar.getInstance().getTime().getTime() - lastTime > 10000) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("oxygen", oxygen);
                    data.put("pulse", pulseRate);
                    data.put("pi", String.valueOf(piConverted));
                    bluetoothConnection.onDataReceived(data, TypeBleDevices.SpO2.stringValue, bleDevice.getMac(), bleDevice.getName());
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        }
    }
}
