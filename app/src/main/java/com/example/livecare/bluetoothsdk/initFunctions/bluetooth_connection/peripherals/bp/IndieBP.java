package com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.peripherals.bp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import com.example.livecare.bluetoothsdk.initFunctions.bluetooth_connection.BluetoothConnection;
import com.example.livecare.bluetoothsdk.initFunctions.enums.TypeBleDevices;
import com.example.livecare.bluetoothsdk.initFunctions.utils.Utils;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.BleManager;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleIndicateCallback;
import com.example.livecare.bluetoothsdk.livecarebluetoothsdk.callback.BleReadCallback;
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

public class IndieBP {
    private final BluetoothConnection bluetoothConnection;
    private BleDevice bleDevice;
    private BluetoothGattCharacteristic bpCharacteristic;
    private String resultSys = "";
    private String resultDia = "";
    private String resultPulse = "";

    public IndieBP(BluetoothConnection bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public void onConnectedSuccess(BleDevice device, BluetoothGatt gatt) {
        bleDevice = device;
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().toString().contains("00001805")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    startWriteCommand(characteristic, setDateTimeCommand());
                }
            } else if (service.getUuid().toString().contains("00001810")) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().toString().contains("00002a35")) {
                        bpCharacteristic = characteristic;
                    }
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
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        startRead(characteristic);
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {}
                });
    }


    private void startRead(BluetoothGattCharacteristic characteristic) {
        BleManager.getInstance().read(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        startIndicate(bpCharacteristic);
                    }

                    @Override
                    public void onReadFailure(BleException exception) {}
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
                    public void onCharacteristicChanged(byte[] data) {
                        calculateResults(HexUtil.formatHexString(data));
                    }
                });
    }

    private void calculateResults(String formatHexString) {
        String convertResultSys = formatHexString.substring(4, 6) + formatHexString.substring(2, 4);
        String convertResultDia = formatHexString.substring(8, 10) + formatHexString.substring(6, 8);
        String convertResultPulse = formatHexString.substring(30, 32) + formatHexString.substring(28, 30);

        resultSys = new BigInteger(convertResultSys, 16).toString();
        resultDia = new BigInteger(convertResultDia, 16).toString();
        resultPulse = new BigInteger(convertResultPulse, 16).toString();
    }

    private String setDateTimeCommand() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hourD = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        String sendYear1 = Integer.toHexString(year);
        String sendMonth = Integer.toHexString(month + 1);
        String sendDay = Integer.toHexString(day);
        String sendHour = Integer.toHexString(hourD);
        String sendMin = Integer.toHexString(min);
        String sendSec = Integer.toHexString(sec);
        String sendDayOfWeek = Integer.toHexString(dayOfWeek);
        String setTimeDate = Utils.convertBigEndian(sendYear1) + addZeroToHex(sendMonth) + addZeroToHex(sendDay) + addZeroToHex(sendHour) + addZeroToHex(sendMin) + addZeroToHex(sendSec)
                + addZeroToHex(sendDayOfWeek) + "0000";
        return setTimeDate.toUpperCase();
    }

    public void onDisConnected() {
        if (!resultSys.equalsIgnoreCase("")) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("sys", resultSys);
            objectMap.put("dia", resultDia);
            objectMap.put("pulse", resultPulse);
            objectMap.put("ahr", "");
            bluetoothConnection.onDataReceived(objectMap, TypeBleDevices.BP.stringValue);
        }
    }
}
