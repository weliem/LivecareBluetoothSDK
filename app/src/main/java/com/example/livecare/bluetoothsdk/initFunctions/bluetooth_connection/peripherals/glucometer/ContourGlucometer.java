package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkPairedDevices;

public class ContourGlucometer {

    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private String result;
    private BluetoothGattCharacteristic characteristicContext;
    private BluetoothGattCharacteristic characteristicRACP;
    private BluetoothGattCharacteristic characteristicMeasurement;
    private Map<String, Object> dataValue = new HashMap<>();

    public ContourGlucometer(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00002a18")) {
                    characteristicMeasurement = characteristic;
                }
                if (characteristic.getUuid().toString().contains("00002a34")) {
                    characteristicContext = characteristic;
                }

                if (characteristic.getUuid().toString().contains("00002a52")) {
                    characteristicRACP = characteristic;
                }
            }
        }

        if (checkPairedDevices(bleDevice.getMac())) {
            startNotify(characteristicMeasurement);
        } else {
            dataValue.put("error", "Error: Please pair the Glucometer");
            bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
            BleManager.getInstance().disconnect(bleDevice);
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
                        startNotifyContext(characteristicContext);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).startsWith("03")) {
                            if (HexUtil.formatHexString(data).startsWith("b0", 26)) {
                                result = new BigInteger(HexUtil.formatHexString(data).substring(24, 26), 16).toString();
                            } else {
                                int resultAbove255 = Integer.parseInt(new BigInteger(HexUtil.formatHexString(data).substring(24, 26), 16).toString()) + 256;
                                result = String.valueOf(resultAbove255);
                            }
                            calculateResults();
                        }
                    }
                });
    }

    private void startNotifyContext(BluetoothGattCharacteristic characteristic) {
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
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {}
                });
    }

    private void startIndicate(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {}

                    @Override
                    public void onIndicateFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {}
                });
    }

    private void calculateResults() {
        dataValue.put("bgValue", result);
        bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
        BleManager.getInstance().disconnect(bleDevice);
    }
}
