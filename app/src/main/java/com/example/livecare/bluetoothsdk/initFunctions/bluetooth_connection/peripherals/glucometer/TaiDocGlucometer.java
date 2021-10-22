package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.glucometer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleNotifyCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleWriteCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.data.BleDevice;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.exception.BleException;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.utils.HexUtil;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.addZeroToHex;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.checkSum;
import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.convertIntToHex;

public class TaiDocGlucometer {
    private final BluetoothConnection bluetoothConnection;
    private final String turnOffDevice = "515000000000a3";
    private final String readStorageData = "512600000001a3";
    private BleDevice bleDevice;

    public TaiDocGlucometer(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {}

                    @Override
                    public void onWriteFailure(final BleException exception) {}
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
                        startWriteCommand(characteristic, setDateTimeCommand());
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (HexUtil.formatHexString(data).length() > 4) {
                            calculateGlResult(HexUtil.formatHexString(data), characteristic);
                            if (HexUtil.formatHexString(data).substring(0, 4).equalsIgnoreCase("5133")) {//date and time was set
                                startWriteCommand(characteristic, checkSum(readStorageData));
                            }
                            if (HexUtil.formatHexString(data).startsWith("5152")) { //delete memory
                                startWriteCommand(characteristic, checkSum(turnOffDevice));
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finishActivity(), 2000);
                            }
                        }
                    }
                });
    }

    private void calculateGlResult(String value, BluetoothGattCharacteristic characteristic) {
        if (value.startsWith("5126")) {
            String result = new BigInteger(value.substring(6, 8) + value.substring(4, 6), 16).toString();
            if (!result.equalsIgnoreCase("0")) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("bgValue", result);
                String clearMemory = "515200000000a3";
                startWriteCommand(characteristic, checkSum(clearMemory));
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.Gl.stringValue, bleDevice.getMac(), bleDevice.getName());
            } else {
                startWriteCommand(characteristic, checkSum(turnOffDevice));
                new Handler(Looper.getMainLooper()).postDelayed(this::finishActivity, 2000);
            }
        }
    }

    private String setDateTimeCommand() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String sendHour = Integer.toHexString(cal.get(Calendar.HOUR_OF_DAY));
        String sendMin = Integer.toHexString(cal.get(Calendar.MINUTE));

        String setTimeDate = "5133" + convertIntToHex(cal) + addZeroToHex(sendMin) + addZeroToHex(sendHour) + "a3";
        return checkSum(setTimeDate);
    }

    private void finishActivity() {
        if (bleDevice != null) {
            BleManager.getInstance().disconnect(bleDevice);
        }
    }
}
