package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.scale;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleReadCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AD_SCALE_UC_352BLE {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicIndicate;
    private BluetoothGattCharacteristic characteristicIndicate2;
    private BluetoothGattCharacteristic characteristicWrite;
    private Handler lastResultHandler;
    private Runnable lastResultRunnable;
    private String result = "";

    public AD_SCALE_UC_352BLE(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
        lastResultHandler = new Handler(Looper.getMainLooper());
        lastResultRunnable = () -> {
            BleManager.getInstance().disconnect(bleDevice);
            if (!result.equalsIgnoreCase("")) {
                calculateResults(result);
            }
        };
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;

        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().contains("00002a05")) {
                    characteristicIndicate2 = characteristic;
                }

                if (characteristic.getUuid().toString().contains("23434101")) {
                    characteristicIndicate = characteristic;
                }
                if (characteristic.getUuid().toString().contains("00002a08")) {
                    characteristicWrite = characteristic;
                }
            }
        }
        startIndicate2(characteristicIndicate2);
    }

    private void calculateResults(String value) {
        Map<String, Object> objectMap = new HashMap<>();
        if (value.length() == 20 ) {
            String hexWeight = value.substring(4, 6) + value.substring(2, 4);
            int weight = Integer.parseInt(hexWeight, 16);
            if(value.substring(0, 2).equalsIgnoreCase("03")){
                double weightLbs = weight * 0.1;
                objectMap.put("weight", weightLbs);
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
            }else if(value.substring(0, 2).equalsIgnoreCase("02")){
                float weightKg = (float) (weight * 0.220462);
                objectMap.put("weight", weightKg);
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.WS.stringValue);
            }else {
                objectMap.put("error", "error");
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue);
            }
        } else {
            objectMap.put("error", "error");
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue);
        }
    }

    private void startRead(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().read(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        setDateTimeSetting(characteristic);
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        BleManager.getInstance().disconnect(bleDevice);
                    }
                });
    }

    private void setDateTimeSetting(BluetoothGattCharacteristic characteristic) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) +1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        byte[] value = {(byte)(year & 0x0FF), (byte)(year >> 8), (byte)month, (byte)day, (byte)hour, (byte)min, (byte)sec};
        startWrite(characteristic, value);
    }

    private void startWrite(BluetoothGattCharacteristic characteristic, byte[] command) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                command,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        startIndicate(characteristicIndicate);
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }

    private void startWrite2(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes("0002"),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        startRead(characteristic);
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
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
                        lastResultHandler.postDelayed(lastResultRunnable,2000);
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        result = HexUtil.formatHexString(data);
                        lastResultHandler.removeCallbacks(lastResultRunnable);
                        lastResultHandler.postDelayed(lastResultRunnable,1000);
                    }
                });
    }

    private void startIndicate2(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().indicate(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleIndicateCallback() {

                    @Override
                    public void onIndicateSuccess() {
                        startWrite2(characteristicWrite);
                    }

                    @Override
                    public void onIndicateFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                    }
                });
    }

    public void onDestroy() {
        lastResultHandler.removeCallbacks(lastResultRunnable);
    }
}
