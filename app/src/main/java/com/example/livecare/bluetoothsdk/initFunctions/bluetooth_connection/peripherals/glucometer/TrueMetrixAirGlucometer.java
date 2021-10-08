package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.widget.Toast;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;

public class TrueMetrixAirGlucometer {
    private final String TAG = "TrueMetrixAirGlucometer";
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String result;
    private String dataHex = "";
    private String hexDateTime = "";
    private BluetoothGattCharacteristic characteristicRACP;
    private BluetoothGattCharacteristic characteristicMeasurement;
    private BluetoothGattCharacteristic characteristicCustom;

    public TrueMetrixAirGlucometer(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                if (service.getUuid().toString().equalsIgnoreCase("00001808-0000-1000-8000-00805f9b34fb")) {
                    if (characteristic.getUuid().toString().contains("00002a18")) {
                        characteristicMeasurement = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("00002a34")) {
                        characteristicCustom = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains("00002a52")) {
                        characteristicRACP = characteristic;
                    }
                }
            }
        }

        if (checkPairedDevices(bleDevice.getMac())) {
            startNotify(characteristicMeasurement);
        } else {
            if (Utils.isNotOtherBleDeviceConnected()) {
                bluetoothConnection.setPin(bleDevice.getDevice(), 5);
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
                        startNotify2(characteristicCustom);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).length() == 26) {
                            float x = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 10);
                            int intResult = Math.round(x * 100000);
                            result = String.valueOf(intResult);

                            dataHex = HexUtil.formatHexString(data);
                            hexDateTime = new BigInteger(HexUtil.formatHexString(data).substring(0, 14), 16).toString();
                        }
                    }
                });
    }


    private void startNotify2(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().notify(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        startIndicate(characteristicRACP);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {

                    }
                });
    }


    private void startIndicate(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {
                        startWriteCommand(characteristicRACP, "0106");
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).equalsIgnoreCase("06000101")) {
                            calculateResults();
                        }
                        if (HexUtil.formatHexString(data).equalsIgnoreCase("06000201")) {
                            BleManager.getInstance().disconnect(bleDevice);
                        }
                        if (HexUtil.formatHexString(data).equalsIgnoreCase("06000106")) {
                            BleManager.getInstance().disconnect(bleDevice);
                        }
                    }
                });
    }


    private void calculateResults() {
        Map<String, Object> dataValue = new HashMap<>();
        dataValue.put("bgValue", result);   bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue);
        startWriteCommand(characteristicRACP, "0201");
        BleManager.getInstance().disconnect(bleDevice);
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

    public void startNotifyCustom() {
        startNotify(characteristicMeasurement);
    }
}
