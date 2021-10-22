package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.spo2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class ForaSpO2 {
    private final String turnOffDevice = "515000000000a3";
    private BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;

    public ForaSpO2(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("00001523")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startNotify(characteristic);
                }
            }
        }
    }

    private void startWriteCommand(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                    }
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
                        startWriteCommand(characteristic, Utils.setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).length() > 4) {
                            calculateSpO2Result(HexUtil.formatHexString(data), characteristic);
                            if (HexUtil.formatHexString(data).substring(0, 4).equalsIgnoreCase("5133")) {
                                startWriteCommand(characteristic, Utils.checkSum("514900000000a3"));
                            }
                            if (HexUtil.formatHexString(data).startsWith("5152")) { //delete memory
                                startWriteCommand(characteristic, Utils.checkSum(turnOffDevice));
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finishActivity(), 2000);
                            }
                        }
                    }
                });
    }

    private void calculateSpO2Result(String value, BluetoothGattCharacteristic characteristic) {
        if (value.startsWith("5149")) {
            String resultOxygen = new BigInteger(value.substring(4, 6), 16).toString();
            String resultPulseRate = new BigInteger(value.substring(10, 12), 16).toString();
            if (resultOxygen.equals("0") || resultPulseRate.equals("0")) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startWriteCommand(characteristic, Utils.checkSum("514900000000a3"));     //start live spo2
                }, 5000);
            } else {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("oxygen", resultOxygen);
                objectMap.put("pulse", resultPulseRate);
                objectMap.put("pi", null);
                startWriteCommand(characteristic, Utils.checkSum("515200000000a3"));
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.SpO2.stringValue, bleDevice.getMac(), bleDevice.getName());
            }
        }
    }



    private void finishActivity() {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }
}
