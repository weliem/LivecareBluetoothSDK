package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.emergency_button;

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
import java.util.HashMap;
import java.util.Map;

public class VAlertDevice {
    private BleDevice bleDevice;
    private final BluetoothConnection bluetoothConnection;
    private BluetoothGattCharacteristic characteristicNotify;
    private BluetoothGattCharacteristic characteristicWrite1;
    private BluetoothGattCharacteristic characteristicWrite2;
    private BluetoothGattCharacteristic characteristicWrite3;
    private BluetoothGattCharacteristic characteristicWrite5;
    private BluetoothGattCharacteristic characteristicWritefcc2;

    public VAlertDevice(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("fffffff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("fffffff1")) {
                        characteristicWrite1 = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("fffffff2")) {
                        characteristicWrite2 = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("fffffff3")) {
                        characteristicWrite3 = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("fffffff4")) {
                        characteristicNotify = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("fffffff5")) {
                        characteristicWrite5 = characteristic;
                    }
                }
            }
            if (service.getUuid().toString().contains("ffffccc0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("ffffccc2")) {
                        characteristicWritefcc2 = characteristic;
                    }
                }
            }
        }
        startWriteCommand(characteristicWrite5,"80BEF5ACFF");
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startWriteCommand(characteristicWrite1,"01");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        Map<String, Object> dataMap = new HashMap<>();
                        if(HexUtil.formatHexString(data).equals("03")){
                            startWriteLongPressCommand(characteristicWrite3,"01");
                            dataMap.put("help_request", true);
                            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.V_ALERT.stringValue, bleDevice.getMac(), bleDevice.getName());
                        }
                        if(HexUtil.formatHexString(data).equals("04")){
                            dataMap.put("fall_detection", true);
                            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.V_ALERT.stringValue, bleDevice.getMac(), bleDevice.getName());
                        }

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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        if(HexUtil.formatHexString(justWrite).equals("80bef5acff")){
                            startWriteCommand(characteristicWrite2,"02");
                        }
                        if(HexUtil.formatHexString(justWrite).equals("02")){
                            startNotify(characteristicNotify);
                        }
                        if(HexUtil.formatHexString(justWrite).equals("00")){
                            startWriteCommand(characteristicWritefcc2,"1003200301005802");
                        }
                        if(HexUtil.formatHexString(justWrite).equals("1003200301005802")){
                            startWriteCommand(characteristicWrite3,"01");
                        }
                        if(HexUtil.formatHexString(justWrite).equals("01")){
                            startWriteCommand(characteristicWrite2,"06");
                        }
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void startWriteLongPressCommand(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        if(HexUtil.formatHexString(justWrite).equals("01")){
                            startWriteLongPressCommand(characteristicWrite3,"00");
                        }
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
    public void disconnect(){
        BleManager.getInstance().disconnect(bleDevice);
    }
}
