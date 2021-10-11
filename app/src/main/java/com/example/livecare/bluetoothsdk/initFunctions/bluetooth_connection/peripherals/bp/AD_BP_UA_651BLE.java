package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

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
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AD_BP_UA_651BLE {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicIndicate;
    private Handler lastResultHandler;
    private Runnable lastResultRunnable;
    private String result = "";

    public AD_BP_UA_651BLE(BluetoothConnection bluetoothConnection) {
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
                if (characteristic.getUuid().toString().contains("00002a35")) {
                    characteristicIndicate = characteristic;
                }
                if (characteristic.getUuid().toString().contains("2a08")) {
                    startRead(characteristic);
                }
            }
        }
    }

    private void calculateResults(String value) {
        Map<String, Object> dataMap = new HashMap<>();
        if (value.length() == 36
                && !value.substring(2, 4).equalsIgnoreCase("ff")
                && !value.substring(2, 4).equalsIgnoreCase("07")
                && !value.substring(6, 8).equalsIgnoreCase("ff")
                && !value.substring(6, 8).equalsIgnoreCase("07")
                && !value.substring(28, 30).equalsIgnoreCase("ff")
                && !value.substring(28, 30).equalsIgnoreCase("07")
                && !value.substring(28, 30).equalsIgnoreCase("00")
                && !value.substring(28, 30).equalsIgnoreCase("08")) {
            String sys = new BigInteger(value.substring(2, 4), 16).toString();
            String dia = new BigInteger(value.substring(6, 8), 16).toString();
            String pul = new BigInteger(value.substring(28, 30), 16).toString();


            dataMap.put("sys", sys);
            dataMap.put("dia", dia);
            dataMap.put("pulse", pul);
            dataMap.put("ahr", "");
            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue);
        } else {
            dataMap.put("error", "error");
            bluetoothConnection.onDataReceived(dataMap, TypeBleDevices.BP.stringValue);
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
                    public void onIndicateFailure(final BleException exception) {
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        result = HexUtil.formatHexString(data);
                        lastResultHandler.removeCallbacks(lastResultRunnable);
                        lastResultHandler.postDelayed(lastResultRunnable,1000);
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
                    public void onWriteFailure(final BleException exception) {
                    }
                });
    }

    public void onDestroy() {
        lastResultHandler.removeCallbacks(lastResultRunnable);
    }
}
