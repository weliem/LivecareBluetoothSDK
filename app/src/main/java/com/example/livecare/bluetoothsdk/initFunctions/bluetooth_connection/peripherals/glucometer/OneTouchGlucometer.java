package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;

public class OneTouchGlucometer {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String dataHex = "";
    private BluetoothGattCharacteristic characteristicMeasurement;
    private BluetoothGattCharacteristic characteristicWrite;

    public OneTouchGlucometer(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    public void onConnectedSuccess( BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().equals("af9df7a1-e595-11e3-96b4-0002a5d5c51b")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("af9df7a3")) {
                        characteristicMeasurement = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("af9df7a2")) {
                        characteristicWrite = characteristic;
                    }
                }
            }
        }

        if (checkPairedDevices(bleDevice.getMac())) {
            startNotify(characteristicMeasurement);
        } else {
            if (Utils.isNotOtherBleDeviceConnected()) {
                bluetoothConnection.setPin(bleDevice.getDevice(), 7);
            } else {
                Utils.teleHealthScanBroadcastReceiver(true);
                BleManager.getInstance().disconnect(bleDevice);
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
                        Utils.teleHealthScanBroadcastReceiver(true);
                        startWrite(characteristicWrite);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        dataHex = HexUtil.formatHexString(data);
                        if (dataHex.length() == 40) {
                            String resultToSend = new BigInteger(HexUtil.formatHexString(data).substring(32, 34) +
                                    HexUtil.formatHexString(data).substring(30, 32), 16).toString();
                            calculateResults(resultToSend);
                        }
                    }
                });
    }

    private void calculateResults(String resultToSend) {
        Map<String, Object> dataValue = new HashMap<>();
        dataValue.put("bgValue", resultToSend);
        bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
        BleManager.getInstance().disconnect(bleDevice);
    }

    private void startWrite(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("01020c0004310200000003a955"),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    public void startNotifyCustom() {
        startNotify(characteristicMeasurement);
    }
}
