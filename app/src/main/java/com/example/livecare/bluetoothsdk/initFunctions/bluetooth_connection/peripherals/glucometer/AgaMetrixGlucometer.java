package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;

public class AgaMetrixGlucometer {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String result;
    private boolean lastResult = false;
    private BluetoothGattCharacteristic characteristicRACP;
    private BluetoothGattCharacteristic characteristicMeasurement;
    private BluetoothGattCharacteristic characteristicFirst;

    public AgaMetrixGlucometer(BluetoothConnection bluetoothConnection) {
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
                    if (characteristic.getUuid().toString().contains("00002a52")) {
                        characteristicRACP = characteristic;
                    }
                }
                if (characteristic.getUuid().toString().equalsIgnoreCase("521cd437-67bb-4acc-9ff8-d2d39c5ca158")) {
                    characteristicFirst = characteristic;
                }
            }
        }

        if (checkPairedDevices(bleDevice.getMac())) {
            startWriteCommand(characteristicFirst, "033900008000001fddcd9e4c05b13eb30510852c");
        } else {
            if (Utils.isNotOtherBleDeviceConnected()) {
                bluetoothConnection.setPin(bleDevice.getDevice(), 6);
            } else {
                Utils.teleHealthScanBroadcastReceiver(true);
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }

    public void startNotifyCustom() {
        startWriteCommand(characteristicFirst, "033900008000001fddcd9e4c05b13eb30510852c");
    }

    private void startNotify(BluetoothGattCharacteristic characteristic) {
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
                        if (!lastResult) {
                            if (HexUtil.formatHexString(data).startsWith("b0", 26)) {
                                result = new BigInteger(HexUtil.formatHexString(data).substring(24, 26), 16).toString();
                            } else {
                                int resultAbove255 = Integer.parseInt(new BigInteger(HexUtil.formatHexString(data).substring(24, 26), 16).toString()) + 256;
                                result = String.valueOf(resultAbove255);
                            }
                            lastResult = true;
                        }
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
                        startWriteCommand(characteristicRACP, "0101");
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).equalsIgnoreCase("06000100")) {
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

    private void startWriteCommand(BluetoothGattCharacteristic characteristic, String command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(command),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        if (HexUtil.formatHexString(justWrite).equals("033900008000001fddcd9e4c05b13eb30510852c")) {
                            Utils.teleHealthScanBroadcastReceiver(true);
                            startNotify(characteristicMeasurement);
                        }
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                    }
                });
    }

    private void calculateResults() {
        Map<String, Object> dataValue = new HashMap<>();
        dataValue.put("bgValue", result);
        bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
        BleManager.getInstance().disconnect(bleDevice);
    }
}
