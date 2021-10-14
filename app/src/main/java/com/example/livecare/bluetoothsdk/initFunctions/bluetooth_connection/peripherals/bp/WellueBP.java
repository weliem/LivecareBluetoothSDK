package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.livecare.bluetoothsdk.initFunctions.utils.Utils.addZeroToHex;

public class WellueBP {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic characteristicWrite;
    private final String startBPCommand = "0240dc01a13c";

    public WellueBP(BluetoothConnection bluetoothConnection, BleDevice device, BluetoothGatt gatt) {
        this.bluetoothConnection = bluetoothConnection;
        bleDevice = device;
        onConnectedSuccess(gatt);
    }

    private void onConnectedSuccess(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    startNotify(characteristic);
                }
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    characteristicWrite = characteristic;
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
                    public void onNotifySuccess() {
                        startWrite(characteristicWrite, startBPCommand);
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {}

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        if (formatHexString.startsWith("0240dd0f")) {
            setTime();
            Map<String, Object> objectMap = new HashMap<>();
            if (!formatHexString.substring(10, 12).equalsIgnoreCase("ff")) {
                String sys = new BigInteger(formatHexString.substring(10, 14), 16).toString();
                String dia = new BigInteger(formatHexString.substring(14, 18), 16).toString();
                String pul = new BigInteger(formatHexString.substring(22, 26), 16).toString();


                objectMap.put("sys", sys);
                objectMap.put("dia", dia);
                objectMap.put("pulse", pul);
                objectMap.put("ahr", "");
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue);

            } else {
                objectMap.put("error", "Error value");
                bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue);
            }
        }

    }

    private void setTime() {
        String timeCommand = setDateTimeCommand();
        byte[] setTime = Utils.hexStringToByteArray(timeCommand);
        int xor = 0;
        for (byte b : setTime) {
            xor ^= b;
        }
        String setTimeCommand = "02" + timeCommand + Integer.toHexString(xor);
        startWrite(characteristicWrite, setTimeCommand);
    }

    private void startWrite(BluetoothGattCharacteristic characteristic, final String command) {
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

    private String setDateTimeCommand() {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        String year2 = String.valueOf(year).substring(2, 4);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hourD = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        String sendYear = Integer.toHexString(Integer.parseInt(year2));
        String sendMonth = Integer.toHexString(month + 1);
        String sendDay = Integer.toHexString(day);
        String sendHour = Integer.toHexString(hourD);
        String sendMin = Integer.toHexString(min);
        String sendSec = Integer.toHexString(sec);

        String setTimeDate = "40dc07B0" + addZeroToHex(sendYear) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec);
        return setTimeDate.toUpperCase();
    }
}
