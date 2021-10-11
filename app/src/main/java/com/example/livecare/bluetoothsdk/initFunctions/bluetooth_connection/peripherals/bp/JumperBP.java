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

public class JumperBP {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicNotify;
    private BluetoothGattCharacteristic characteristicWrite;

    public JumperBP(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("fff0")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("fff2")) {
                        characteristicWrite = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("fff1")) {
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

                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).startsWith("fdfdfc")) {
                            //success response
                            calculateResults(HexUtil.formatHexString(data));
                        } else if (HexUtil.formatHexString(data).startsWith("fdfdfd")) {
                            //error response
                            startWrite(characteristicWrite);
                        }

                        if (HexUtil.formatHexString(data).equals("fdfd070d0a")) {
                            //turn off response
                            BleManager.getInstance().disconnect(bleDevice);
                        }
                    }
                });
    }

    private void calculateResults(String value) {
        String sys = new BigInteger(value.substring(6, 8), 16).toString();
        String dia = new BigInteger(value.substring(8, 10), 16).toString();
        String pul = new BigInteger(value.substring(10, 12), 16).toString();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("sys", sys);
        dataMap.put("dia", dia);
        dataMap.put("pulse", pul);
        dataMap.put("ahr", "");
        bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue);
        startWrite(characteristicWrite);
        BleManager.getInstance().disconnect(bleDevice);
    }

    private void startWrite(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("FDFDFE060D0A"),//turn Off device
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }
}
