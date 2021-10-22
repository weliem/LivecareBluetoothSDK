package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.ecg;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.util.HashMap;
import java.util.Map;

public class ECGCardiBeat {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private Handler handler;
    private Runnable runnable;
    private StringBuilder stringBuilder = new StringBuilder();

    public ECGCardiBeat(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;

        handler = new Handler(Looper.getMainLooper());
        runnable = () -> BleManager.getInstance().disconnect(bleDevice);
        handler.postDelayed(runnable, 30000);

        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().equals("0000ffb1-0000-1000-8000-00805f9b34fb")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startNotify(characteristic);
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
                        stringBuilder.append(HexUtil.formatHexString(data));
                    }
                });
    }

    private void updateECGToFireBase(StringBuilder ecg) {
        Map<String, Object> dataValue = new HashMap<>();
        dataValue.put("ecg", ecg);
        bluetoothConnection.onDataReceived(dataValue, TypeBleDevices.ECG.stringValue, bleDevice.getMac(), bleDevice.getName());
        BleManager.getInstance().disconnect(bleDevice);
    }

    public void onDisConnected() {
        if (bleDevice != null) {
            updateECGToFireBase(stringBuilder);
        }

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
